package org.broadleafcommerce.vendor.paypal.service;

import org.apache.commons.lang3.StringUtils;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutExternalCallService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutRestConfigurationProperties;
import org.broadleafcommerce.payment.service.gateway.PayPalGatewayConfiguration;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreatePaymentRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreatePaymentResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalUpdatePaymentRequest;

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
    private static final String REPLACE_OP_TYPE = "replace";

    private final PayPalCheckoutExternalCallService paypalCheckoutService;
    private final PayPalGatewayConfiguration gatewayConfiguration;
    private final PayPalWebExperienceProfileService webExperienceProfileService;
    private final boolean shouldPopulateShippingOnPaymentCreation;

    @Override
    public Payment createPayPalPayment(@NonNull PaymentRequest paymentRequest,
            boolean performCheckoutOnReturn)
            throws PaymentException {
        // Set payer details
        Payer payer = constructPayer(paymentRequest);

        PayPalCheckoutRestConfigurationProperties configProperties =
                paypalCheckoutService.getConfigProperties();

        // Set redirect URLs
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(configProperties.getCancelUrl(paymentRequest));
        redirectUrls.setReturnUrl(configProperties.getReturnUrl(paymentRequest));

        Amount amount = paypalCheckoutService.getPayPalAmountFromOrder(paymentRequest);

        // Transaction information
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription(configProperties.getPaymentDescription());
        transaction.setCustom(paymentRequest.getOrderId() + "|" + performCheckoutOnReturn);

        ItemList itemList = paypalCheckoutService.getPayPalItemList(paymentRequest,
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

        String webExperienceProfileId =
                webExperienceProfileService.getWebExperienceProfileId(paymentRequest);
        if (StringUtils.isNotBlank(webExperienceProfileId)) {
            payment.setExperienceProfileId(webExperienceProfileId);
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

        Patch amountPatch = new Patch(REPLACE_OP_TYPE, "/transactions/0/amount");
        Amount amount = paypalCheckoutService.getPayPalAmountFromOrder(paymentRequest);
        amountPatch.setValue(amount);
        patches.add(amountPatch);

        ItemList itemList = paypalCheckoutService.getPayPalItemList(paymentRequest, true);
        if (itemList != null) {
            Patch shipToPatch = new Patch(REPLACE_OP_TYPE, "/transactions/0/item_list");
            shipToPatch.setValue(itemList);
            patches.add(shipToPatch);
        }

        Patch customPatch = new Patch(REPLACE_OP_TYPE, "/transactions/0/custom");
        customPatch.setValue(paymentRequest.getOrderId() + "|" + true);
        patches.add(customPatch);

        Payment paypalPayment = new Payment();
        paypalPayment.setId(paymentId);
        updatePayment(paypalPayment, patches, paymentRequest);

    }

    protected Payment createPayment(Payment payment, PaymentRequest paymentRequest)
            throws PaymentException {
        PayPalCreatePaymentResponse response =
                (PayPalCreatePaymentResponse) paypalCheckoutService.call(
                        new PayPalCreatePaymentRequest(payment,
                                paypalCheckoutService.constructAPIContext(paymentRequest)));
        return response.getPayment();
    }

    protected void updatePayment(Payment payment,
            List<Patch> patches,
            PaymentRequest paymentRequest) throws PaymentException {
        paypalCheckoutService.call(
                new PayPalUpdatePaymentRequest(payment,
                        patches,
                        paypalCheckoutService.constructAPIContext(paymentRequest)));
    }

    public String getIntent(boolean performCheckoutOnReturn) {
        if (gatewayConfiguration.isPerformAuthorizeAndCapture()) {
            return "sale";
        }

        return "authorize";
    }

}
