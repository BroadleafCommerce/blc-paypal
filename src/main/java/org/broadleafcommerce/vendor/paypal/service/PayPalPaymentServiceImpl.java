package org.broadleafcommerce.vendor.paypal.service;

import org.apache.commons.lang3.StringUtils;
import org.broadleafcommerce.payment.service.gateway.ExternalCallPayPalCheckoutService;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreatePaymentRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreatePaymentResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalUpdatePaymentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.service.CurrentOrderPaymentRequestService;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.ItemList;
import com.paypal.api.payments.Patch;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

@Service("blPayPalPaymentService")
public class PayPalPaymentServiceImpl implements PayPalPaymentService {

    @Resource(name = "blExternalCallPayPalCheckoutService")
    protected ExternalCallPayPalCheckoutService externalCallService;

    @Resource(name = "blPayPalWebProfileService")
    protected PayPalWebProfileService webProfileService;

    @Autowired(required = false)
    protected CurrentOrderPaymentRequestService currentOrderPaymentRequestService;

    @Value("${gateway.paypal.checkout.rest.populate.shipping.create.payment:true}")
    protected boolean shouldPopulateShippingOnPaymentCreation;

    @Override
    public Payment createPayPalPaymentForCurrentOrder(boolean performCheckoutOnReturn)
            throws PaymentException {
        PaymentRequest paymentRequest = getPaymentRequestForCurrentOrder();

        // Set payer details
        Payer payer = constructPayer(paymentRequest);

        // Set redirect URLs
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(externalCallService.getConfiguration().getCancelUrl());
        redirectUrls.setReturnUrl(externalCallService.getConfiguration().getReturnUrl());

        Amount amount = externalCallService.getPayPalAmountFromOrder(paymentRequest);

        // Transaction information
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription(externalCallService.getConfiguration().getPaymentDescription());
        transaction.setCustom(paymentRequest.getOrderId() + "|" + performCheckoutOnReturn);

        ItemList itemList = externalCallService.getPayPalItemListFromOrder(paymentRequest,
                shouldPopulateShippingOnPaymentCreation);
        if (itemList != null) {
            transaction.setItemList(itemList);
        }

        // Add transaction to a list
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        // Add payment details
        Payment payment = new Payment();
        payment.setIntent(getIntent(performCheckoutOnReturn));
        payment.setPayer(payer);
        payment.setRedirectUrls(redirectUrls);
        payment.setTransactions(transactions);

        String profileId = webProfileService.getWebProfileId(paymentRequest);
        if (StringUtils.isNotBlank(profileId)) {
            payment.setExperienceProfileId(profileId);
        }
        return createPayment(payment, paymentRequest);
    }

    protected Payer constructPayer(PaymentRequest paymentRequest) {
        Payer payer = new Payer();
        payer.setPaymentMethod(MessageConstants.PAYER_PAYMENTMETHOD_PAYPAL);
        return payer;
    }

    @Override
    public void updatePayPalPaymentForFulfillment() throws PaymentException {
        PaymentRequest paymentRequest = getPaymentRequestForCurrentOrder();
        String paymentId = getPayPalPaymentIdFromCurrentOrder();
        if (paymentRequest == null) {
            throw new PaymentException(
                    "Unable to update the current PayPal payment because the PaymentRequestDTO was null");
        }
        if (StringUtils.isBlank(paymentId)) {
            throw new PaymentException(
                    "Unable to update the current PayPal payment because no PayPal payment id was found on the order");
        }
        List<Patch> patches = new ArrayList<>();

        Patch amountPatch = new Patch();
        amountPatch.setOp("replace");
        amountPatch.setPath("/transactions/0/amount");
        Amount amount = externalCallService.getPayPalAmountFromOrder(paymentRequest);
        amountPatch.setValue(amount);
        patches.add(amountPatch);

        ItemList itemList = externalCallService.getPayPalItemListFromOrder(paymentRequest, true);
        if (itemList != null) {
            Patch shipToPatch = new Patch();
            shipToPatch.setOp("replace");
            shipToPatch.setPath("/transactions/0/item_list");
            shipToPatch.setValue(itemList);
            patches.add(shipToPatch);
        }

        Patch customPatch = new Patch();
        customPatch.setPath("/transactions/0/custom");
        customPatch.setOp("replace");
        customPatch.setValue(paymentRequest.getOrderId() + "|" + true);
        patches.add(customPatch);

        Payment paypalPayment = new Payment();
        paypalPayment.setId(paymentId);
        updatePayment(paypalPayment, patches, paymentRequest);

    }

    protected Payment createPayment(Payment payment, PaymentRequest paymentRequest)
            throws PaymentException {
        PayPalCreatePaymentResponse response =
                (PayPalCreatePaymentResponse) externalCallService.call(
                        new PayPalCreatePaymentRequest(payment,
                                externalCallService.constructAPIContext(paymentRequest)));
        return response.getPayment();
    }

    protected void updatePayment(Payment payment,
            List<Patch> patches,
            PaymentRequest paymentRequest) throws PaymentException {
        externalCallService.call(
                new PayPalUpdatePaymentRequest(payment,
                        patches,
                        externalCallService.constructAPIContext(paymentRequest)));
    }

    protected PaymentRequest getPaymentRequestForCurrentOrder() throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            return currentOrderPaymentRequestService.getPaymentRequestFromCurrentOrder();
        } else {
            throw new PaymentException("Unable to get PaymentRequestDTO for current order");
        }
    }

    @Override
    public String getPayPalPaymentIdFromCurrentOrder() throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            return currentOrderPaymentRequestService
                    .retrieveOrderAttributeFromCurrentOrder(MessageConstants.PAYMENTID);
        } else {
            throw new PaymentException("Unable to retrieve PayPal payment id for current order");
        }
    }

    @Override
    public String getPayPalPayerIdFromCurrentOrder() throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            return currentOrderPaymentRequestService
                    .retrieveOrderAttributeFromCurrentOrder(MessageConstants.PAYERID);
        } else {
            throw new PaymentException("Unable to retrieve PayPal payer id for current order");
        }
    }

    @Override
    public void setPayPalPaymentIdOnCurrentOrder(String paymentId) throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            currentOrderPaymentRequestService
                    .addOrderAttributeToCurrentOrder(MessageConstants.PAYMENTID, paymentId);
        } else {
            throw new PaymentException("Unable to set PayPal payment id on current order");
        }
    }

    @Override
    public void setPayPalPayerIdOnCurrentOrder(String payerId) throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            currentOrderPaymentRequestService
                    .addOrderAttributeToCurrentOrder(MessageConstants.PAYERID, payerId);
        } else {
            throw new PaymentException("Unable to set PayPal payer id on current order");
        }
    }

    public String getIntent(boolean performCheckoutOnReturn) {
        if (externalCallService.getConfiguration().isPerformAuthorizeAndCapture()) {
            return "sale";
        }

        return "authorize";
    }

}
