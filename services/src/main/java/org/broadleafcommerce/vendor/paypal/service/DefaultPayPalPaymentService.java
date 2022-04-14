/*
 * Copyright (C) 2009 - 2020 Broadleaf Commerce
 *
 * Licensed under the Broadleaf End User License Agreement (EULA), Version 1.1 (the
 * "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt).
 *
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the
 * "Custom License") between you and Broadleaf Commerce. You may not use this file except in
 * compliance with the applicable license.
 *
 * NOTICE: All information contained herein is, and remains the property of Broadleaf Commerce, LLC
 * The intellectual and technical concepts contained herein are proprietary to Broadleaf Commerce,
 * LLC and may be covered by U.S. and Foreign Patents, patents in process, and are protected by
 * trade secret or copyright law. Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained from Broadleaf Commerce, LLC.
 */
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
import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultPayPalPaymentService implements PayPalPaymentService {

    private static final String ADD_OP_TYPE = "add";
    private static final String REPLACE_OP_TYPE = "replace";

    @Getter(AccessLevel.PROTECTED)
    private final PayPalCheckoutExternalCallService paypalCheckoutService;

    @Getter(AccessLevel.PROTECTED)
    private final PayPalCheckoutRestConfigurationProperties configProperties;

    @Getter(AccessLevel.PROTECTED)
    private final PayPalGatewayConfiguration gatewayConfiguration;

    @Getter(AccessLevel.PROTECTED)
    private final PayPalWebExperienceProfileService webExperienceProfileService;

    @Getter(AccessLevel.PROTECTED)
    private final boolean shouldPopulateShippingOnPaymentCreation;

    @Override
    public Payment createPayPalPayment(@lombok.NonNull PaymentRequest paymentRequest,
            boolean performCheckoutOnReturn,
            boolean capture)
            throws PaymentException {
        // Set payer details
        Payer payer = constructPayer(paymentRequest);

        // Set redirect URLs
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(configProperties.getCancelUrl(paymentRequest));
        redirectUrls.setReturnUrl(configProperties.getReturnUrl(paymentRequest));

        Amount amount = paypalCheckoutService.getPayPalAmountFromOrder(paymentRequest);

        // Transaction information
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription(configProperties.getPaymentDescription());
        transaction.setCustom(
                paymentRequest.getTransactionReferenceId() + "|" + performCheckoutOnReturn);

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
        payment.setIntent(getIntent(performCheckoutOnReturn, capture));
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
    public void updatePayPalPaymentForFulfillment(@lombok.NonNull PaymentRequest paymentRequest)
            throws PaymentException {
        String paymentId = (String) paymentRequest.getAdditionalField(MessageConstants.PAYMENTID);
        if (StringUtils.isBlank(paymentId)) {
            throw new PaymentException(
                    "Unable to update the current PayPal payment because no PayPal payment id was found on the order");
        }
        List<Patch> patches = new ArrayList<>();

        Patch amountPatch = new Patch(ADD_OP_TYPE, "/transactions/0/amount");
        Amount amount = paypalCheckoutService.getPayPalAmountFromOrder(paymentRequest);
        amountPatch.setValue(amount);
        patches.add(amountPatch);

        ItemList itemList = paypalCheckoutService.getPayPalItemList(paymentRequest, true);
        if (itemList != null) {
            Patch itemListPatch = new Patch(ADD_OP_TYPE, "/transactions/0/item_list");
            itemListPatch.setValue(itemList);
            patches.add(itemListPatch);
        }

        Patch customPatch = new Patch(ADD_OP_TYPE, "/transactions/0/custom");
        customPatch.setValue(paymentRequest.getOrderId() + "|" + true);
        patches.add(customPatch);

        Payment paypalPayment = new Payment();
        paypalPayment.setId(paymentId);
        updatePayment(paypalPayment, patches, paymentRequest);

    }

    @Override
    public void updatePaymentCustom(@lombok.NonNull String paymentId, @lombok.NonNull String custom)
            throws PaymentException {
        if (StringUtils.isBlank(paymentId)) {
            throw new PaymentException(
                    "Unable to update the current PayPal payment because no PayPal payment id was found on the order");
        }

        Patch customPatch = new Patch(ADD_OP_TYPE, "/transactions/0/custom");
        customPatch.setValue(custom);

        Payment payment = new Payment();
        payment.setId(paymentId);
        List<Patch> patches = Collections.singletonList(customPatch);

        PaymentRequest paymentRequest = new PaymentRequest();
        updatePayment(payment, patches, paymentRequest);
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

    @Deprecated
    public String getIntent(boolean performCheckoutOnReturn) {
        return getIntent(performCheckoutOnReturn, false);
    }

    protected String getIntent(boolean performCheckoutOnReturn, boolean capture) {
        if (capture) {
            return "sale";
        }

        return "authorize";
    }

}
