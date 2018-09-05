package org.broadleafcommerce.vendor.paypal.service;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.broadleafcommerce.common.payment.PaymentTransactionType;
import org.broadleafcommerce.common.payment.PaymentType;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.core.order.domain.FulfillmentGroup;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.FulfillmentGroupService;
import org.broadleafcommerce.core.payment.domain.OrderPayment;
import org.broadleafcommerce.core.payment.domain.PaymentTransaction;
import org.broadleafcommerce.core.payment.service.OrderPaymentService;
import org.broadleafcommerce.payment.service.gateway.ExternalCallPayPalExpressService;
import org.broadleafcommerce.profile.core.domain.Address;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreatePaymentRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreatePaymentResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalPaymentInfoDTO;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalUpdatePaymentRequest;
import org.springframework.stereotype.Service;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Details;
import com.paypal.api.payments.ItemList;
import com.paypal.api.payments.Patch;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.ShippingAddress;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

@Service("blPayPalPaymentService")
public class PayPalPaymentServiceImpl implements PayPalPaymentService {

    @Resource(name = "blFulfillmentGroupService")
    protected FulfillmentGroupService fulfillmentGroupService;

    @Resource(name = "blOrderPaymentService")
    protected OrderPaymentService orderPaymentService;

    @Resource(name = "blPayPalApiContext")
    protected APIContext apiContext;

    @Resource(name = "blExternalCallPayPalExpressService")
    protected ExternalCallPayPalExpressService externalCallService;

    @Resource(name = "blPayPalWebProfileService")
    protected PayPalWebProfileService webProfileService;

    @Override
    public Payment createPayment(Order order, boolean performCheckoutOnReturn) throws PaymentException {

        // Set payer details
        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        // Set redirect URLs
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(externalCallService.getConfiguration().getCancelUrl());
        redirectUrls.setReturnUrl(externalCallService.getConfiguration().getReturnUrl());

        Amount amount = getPayPalAmountFromOrder(order);

        // Transaction information
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription("This is the payment transaction description.");
        transaction.setCustom(order.getId().toString() + "|" + performCheckoutOnReturn);
        ShippingAddress address = getPayPalShippingAddressFromOrder(order);
        if (address != null) {
            ItemList itemList = new ItemList();
            itemList.setShippingAddress(address);
            transaction.setItemList(itemList);
        }

        // Add transaction to a list
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        // Add payment details
        Payment payment = new Payment();
        payment.setIntent("authorize");
        payment.setPayer(payer);
        payment.setRedirectUrls(redirectUrls);
        payment.setTransactions(transactions);

        String profileId = webProfileService.getWebProfileId();
        if (StringUtils.isNotBlank(profileId)) {
            payment.setExperienceProfileId(profileId);
        }
        return createPayment(payment);
    }

    @Override
    public PayPalPaymentInfoDTO updatePaymentForFulfillment(Order order) throws PaymentException {
        List<OrderPayment> orderPayments = orderPaymentService.readPaymentsForOrder(order);
        OrderPayment payment = null;
        for (OrderPayment p : CollectionUtils.emptyIfNull(orderPayments)) {
            if (p.isActive() && PaymentType.THIRD_PARTY_ACCOUNT.equals(p.getType())) {
                payment = p;
                break;
            }
        }
        if (payment != null) {
            List<PaymentTransaction> transactions = payment.getTransactionsForType(PaymentTransactionType.UNCONFIRMED);
            if (CollectionUtils.isNotEmpty(transactions)) {
                PaymentTransaction transaction = transactions.get(0);
                String paymentId = transaction.getAdditionalFields().get(MessageConstants.PAYMENTID);
                String payerId = transaction.getAdditionalFields().get(MessageConstants.PAYERID);
                List<Patch> patches = new ArrayList<>();

                Patch amountPatch = new Patch();
                amountPatch.setOp("replace");
                amountPatch.setPath("/transactions/0/amount");
                Amount amount = getPayPalAmountFromOrder(order);
                amountPatch.setValue(amount);
                patches.add(amountPatch);

                FulfillmentGroup fg = fulfillmentGroupService.getFirstShippableFulfillmentGroup(order);
                if (fg != null && fg.getAddress() != null) {
                    Address address = fg.getAddress();
                    Patch shipToPatch = new Patch();
                    shipToPatch.setOp("replace");
                    shipToPatch.setPath("/transactions/0/item_list/shipping_address");
                    ShippingAddress shipAddress = getPayPalShippingAddressFromAddress(address);
                    shipToPatch.setValue(shipAddress);
                    patches.add(shipToPatch);
                }

                Patch customPatch = new Patch();
                customPatch.setPath("/transactions/0/custom");
                customPatch.setOp("replace");
                customPatch.setValue(order.getId().toString() + "|" + true);
                patches.add(customPatch);

                Payment paypalPayment = new Payment();
                paypalPayment.setId(paymentId);
                updatePayment(paypalPayment, patches);

                return new PayPalPaymentInfoDTO().setPaymentId(paymentId).setPayerId(payerId);
            }
        }
        return null;
    }

    protected Amount getPayPalAmountFromOrder(Order order) {
        Details details = new Details();

        details.setShipping(order.getTotalShipping().toString());
        details.setSubtotal(order.getSubTotal().toString());
        details.setTax(order.getTotalTax().toString());

        Amount amount = new Amount();
        amount.setCurrency(order.getCurrency().getCurrencyCode());
        amount.setTotal(order.getTotal().toString());
        amount.setDetails(details);
        return amount;
    }

    protected ShippingAddress getPayPalShippingAddressFromAddress(Address address) {
        ShippingAddress shipAddress = new ShippingAddress();
        shipAddress.setRecipientName(address.getFullName());
        shipAddress.setLine1(address.getAddressLine1());
        shipAddress.setLine2(address.getAddressLine2());
        shipAddress.setCity(address.getCity());
        shipAddress.setState(address.getStateProvinceRegion());
        shipAddress.setPostalCode(address.getPostalCode());
        shipAddress.setCountryCode(address.getIsoCountryAlpha2().getAlpha2());
        if (address.getPhonePrimary() != null) {
            shipAddress.setPhone(address.getPhonePrimary().getPhoneNumber());
        }
        return shipAddress;
    }

    protected ShippingAddress getPayPalShippingAddressFromOrder(Order order) {
        FulfillmentGroup fg = fulfillmentGroupService.getFirstShippableFulfillmentGroup(order);
        if (fg != null && fg.getAddress() != null) {
            return getPayPalShippingAddressFromAddress(fg.getAddress());
        }
        return null;
    }

    protected Payment createPayment(Payment payment) throws PaymentException {
        PayPalCreatePaymentResponse response = (PayPalCreatePaymentResponse) externalCallService.call(new PayPalCreatePaymentRequest(payment, apiContext));
        return response.getPayment();
    }

    protected void updatePayment(Payment payment, List<Patch> patches) throws PaymentException {
        externalCallService.call(new PayPalUpdatePaymentRequest(payment, patches, apiContext));
    }

}
