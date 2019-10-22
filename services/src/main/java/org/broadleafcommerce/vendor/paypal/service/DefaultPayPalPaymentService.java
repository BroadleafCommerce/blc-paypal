package org.broadleafcommerce.vendor.paypal.service;

import org.apache.commons.lang3.StringUtils;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutExternalCallService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutRestConfigurationProperties;
import org.broadleafcommerce.payment.service.gateway.PayPalGatewayConfiguration;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreatePaymentRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreatePaymentResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalUpdatePaymentRequest;
import org.springframework.beans.factory.annotation.Value;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultPayPalPaymentService implements PayPalPaymentService {

    private final PayPalCheckoutExternalCallService externalCallService;
    private final PayPalGatewayConfiguration gatewayConfiguration;
    private final PayPalWebProfileService webProfileService;

    @Value("${broadleaf.paypal.checkout.rest.populate.shipping.create.payment:true}")
    protected boolean shouldPopulateShippingOnPaymentCreation;

    @Override
    public Payment createPayPalPayment(@NonNull PaymentRequest paymentRequest,
            boolean performCheckoutOnReturn)
            throws PaymentException {
        // Set payer details
        Payer payer = constructPayer(paymentRequest);

        PayPalCheckoutRestConfigurationProperties configProperties =
                externalCallService.getConfigProperties();

        // Set redirect URLs
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(configProperties.getCancelUrl(paymentRequest));
        redirectUrls.setReturnUrl(configProperties.getReturnUrl(paymentRequest));

        Amount amount = externalCallService.getPayPalAmountFromOrder(paymentRequest);

        // Transaction information
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription(configProperties.getPaymentDescription());
        transaction.setCustom(paymentRequest.getOrderId() + "|" + performCheckoutOnReturn);

        ItemList itemList = externalCallService.getPayPalItemList(paymentRequest,
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
    public void updatePayPalPaymentForFulfillment(@NonNull PaymentRequest paymentRequest)
            throws PaymentException {
        String paymentId = (String) paymentRequest.getAdditionalField(MessageConstants.PAYMENTID);
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

        ItemList itemList = externalCallService.getPayPalItemList(paymentRequest, true);
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

    public String getIntent(boolean performCheckoutOnReturn) {
        if (gatewayConfiguration.isPerformAuthorizeAndCapture()) {
            return "sale";
        }

        return "authorize";
    }

}
