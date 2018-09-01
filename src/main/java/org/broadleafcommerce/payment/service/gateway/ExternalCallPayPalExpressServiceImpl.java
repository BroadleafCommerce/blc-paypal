/*
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2014 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.payment.service.gateway;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.payment.PaymentTransactionType;
import org.broadleafcommerce.common.payment.PaymentType;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.AbstractExternalPaymentGatewayCall;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.core.order.domain.FulfillmentGroup;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.FulfillmentGroupService;
import org.broadleafcommerce.core.payment.domain.OrderPayment;
import org.broadleafcommerce.core.payment.domain.PaymentTransaction;
import org.broadleafcommerce.core.payment.service.OrderPaymentService;
import org.broadleafcommerce.profile.core.domain.Address;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreatePaymentRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreatePaymentResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalPaymentInfoDTO;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalResponse;
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

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blExternalCallPayPalExpressService")
public class ExternalCallPayPalExpressServiceImpl extends AbstractExternalPaymentGatewayCall<PayPalRequest, PayPalResponse> implements ExternalCallPayPalExpressService {

    private static final Log LOG = LogFactory.getLog(ExternalCallPayPalExpressServiceImpl.class);

    @Resource(name = "blPayPalExpressConfiguration")
    protected PayPalExpressConfiguration configuration;

    @Resource(name = "blFulfillmentGroupService")
    protected FulfillmentGroupService fulfillmentGroupService;

    @Resource(name = "blOrderPaymentService")
    protected OrderPaymentService orderPaymentService;

    @Resource(name = "blPayPalApiContext")
    protected APIContext apiContext;

    @Override
    public PayPalExpressConfiguration getConfiguration() {
        return configuration;
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

    @Override
    public Payment createPayment(Order order, boolean performCheckoutOnReturn) throws PaymentException {

        // Set payer details
        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        // Set redirect URLs
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(configuration.getCancelUrl());
        redirectUrls.setReturnUrl(configuration.getReturnUrl());

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
        return createPayment(payment);
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

    @Override
    public void setCommonDetailsResponse(Payment response, PaymentResponseDTO responseDTO) {
        responseDTO.rawResponse(response.toJSON());
        
        if (CollectionUtils.isNotEmpty(response.getTransactions()) && 
            response.getTransactions().get(0) != null &&
            response.getTransactions().get(0).getItemList() != null) {
            ShippingAddress shippingAddress = response.getTransactions().get(0).getItemList().getShippingAddress();
            
            String shipPhone = shippingAddress.getPhone();
            String itemListPhone = response.getTransactions().get(0).getItemList().getShippingPhoneNumber();
            String payerPhone = response.getPayer().getPayerInfo().getPhone();
            String phone = "";
            if (shipPhone != null) {
                phone = shipPhone;
            } else if (itemListPhone != null) {
                phone = itemListPhone;
            } else if (payerPhone != null) {
                phone = payerPhone;
            }
            responseDTO.shipTo()
                .addressFullName(shippingAddress.getRecipientName())
                .addressLine1(shippingAddress.getLine1())
                .addressLine2(shippingAddress.getLine2())
                .addressCityLocality(shippingAddress.getCity())
                .addressStateRegion(shippingAddress.getState())
                .addressPostalCode(shippingAddress.getPostalCode())
                .addressCountryCode(shippingAddress.getCountryCode())
                .addressPhone(phone)
                .done();
            
            if (shippingAddress.getStatus() != null) {
                responseDTO.getShipTo().additionalFields(MessageConstants.ADDRESSSTATUS, shippingAddress.getStatus());
            }

            Transaction transaction = response.getTransactions().get(0);
            
            String itemTotal = "";
            String shippingDiscount = "";
            String shippingTotal = "";
            String totalTax = "";
            String total = "0.00";
            String currency = "USD";
            if (transaction.getAmount() != null && transaction.getAmount().getDetails() != null) {
                Details details = transaction.getAmount().getDetails();
                if (details.getSubtotal() != null) {
                    itemTotal = details.getSubtotal();
                }
                if (details.getShippingDiscount() != null) {
                    shippingDiscount = details.getShippingDiscount();
                }
                if (details.getShipping() != null) {
                    shippingTotal = details.getShipping();
                }
                if (details.getTax() != null) {
                    totalTax = details.getTax();
                }
            }
            if (transaction.getAmount() != null) {
                total = transaction.getAmount().getTotal();
                if (transaction.getAmount().getCurrency() != null) {
                    currency = transaction.getAmount().getCurrency();
                }
            }
            String[] customFields = transaction.getCustom().split("\\|");
            responseDTO.amount(new Money(total, currency))
                    .orderId(customFields[0])
                    .successful(true)
                    .valid(true)
                    .completeCheckoutOnCallback(Boolean.parseBoolean(customFields[1]))
                    .responseMap(MessageConstants.DETAILSPAYMENTALLOWEDMETHOD, response.getPayer().getPaymentMethod())
                    .responseMap(MessageConstants.DETAILSPAYMENTTRANSACTIONID, response.getId())
                    .responseMap(MessageConstants.DETAILSPAYMENTITEMTOTAL, itemTotal)
                    .responseMap(MessageConstants.DETAILSPAYMENTSHIPPINGDISCOUNT, shippingDiscount)
                    .responseMap(MessageConstants.DETAILSPAYMENTSHIPPINGTOTAL,shippingTotal)
                    .responseMap(MessageConstants.DETAILSPAYMENTTOTALTAX, totalTax);
            
            String payerStatus = response.getPayer().getStatus();
    
            responseDTO.customer()
                .firstName(response.getPayer().getPayerInfo().getFirstName())
                .lastName(response.getPayer().getPayerInfo().getLastName())
                .phone(response.getPayer().getPayerInfo().getPhone())
                .email(response.getPayer().getPayerInfo().getEmail())
                .done()
            .responseMap(MessageConstants.NOTE, response.getNoteToPayer())
            .responseMap(MessageConstants.PAYERSTATUS, payerStatus);
        }

    }

    protected Payment createPayment(Payment payment) throws PaymentException {
        PayPalCreatePaymentResponse response = (PayPalCreatePaymentResponse) call(new PayPalCreatePaymentRequest(payment, apiContext));
        return response.getPayment();
    }

    protected void updatePayment(Payment payment, List<Patch> patches) throws PaymentException {
        call(new PayPalUpdatePaymentRequest(payment, patches, apiContext));
    }

    @Override
    public String getServiceName() {
        return getClass().getName();
    }

    @Override
    public PayPalResponse call(PayPalRequest paymentRequest) throws PaymentException {
        return super.process(paymentRequest);
    }


    @Override
    public PayPalResponse communicateWithVendor(PayPalRequest paymentRequest) throws Exception {
        return paymentRequest.execute();
    }

    @Override
    public Integer getFailureReportingThreshold() {
        return configuration.getFailureReportingThreshold();
    }

}
