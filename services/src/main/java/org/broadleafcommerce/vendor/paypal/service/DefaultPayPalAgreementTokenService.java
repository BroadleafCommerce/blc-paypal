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

import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutExternalCallService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutRestConfigurationProperties;
import org.broadleafcommerce.vendor.paypal.api.AgreementToken;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreateAgreementTokenRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreateAgreementTokenResponse;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.paypal.api.payments.MerchantPreferences;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Plan;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultPayPalAgreementTokenService implements PayPalAgreementTokenService {

    private final PayPalCheckoutExternalCallService paypalCheckoutService;

    /**
     * To support PayPal Reference Transactions and Billing Agreement Tokens
     * {@see https://developer.paypal.com/docs/limited-release/reference-transactions}
     *
     * @param performCheckoutOnReturn
     * @return
     * @throws PaymentException
     */
    @Override
    public AgreementToken createPayPalAgreementToken(PaymentRequest paymentRequest,
            boolean performCheckoutOnReturn)
            throws PaymentException {
        // Create Agreement Token
        String agreementDescription = constructAgreementDescription(paymentRequest);
        Payer payer = constructPayer(paymentRequest);
        Plan plan = constructPlan(paymentRequest, performCheckoutOnReturn);
        AgreementToken agreementToken = new AgreementToken(agreementDescription, payer, plan);

        return createAgreementToken(agreementToken, paymentRequest);
    }

    protected Plan constructPlan(PaymentRequest paymentRequest, boolean performCheckoutOnReturn) {
        Plan plan = new Plan();
        plan.setType(MessageConstants.PLAN_TYPE_MERCHANTINITIATEDBILLING);

        PayPalCheckoutRestConfigurationProperties configProperties =
                paypalCheckoutService.getConfigProperties();

        // Set up merchant preferences
        MerchantPreferences merchantPreferences = new MerchantPreferences();
        merchantPreferences.setCancelUrl(configProperties.getCancelUrl(paymentRequest));
        String returnUrl = configProperties.getReturnUrl(paymentRequest);
        if (performCheckoutOnReturn) {
            returnUrl += "?" + MessageConstants.CHECKOUT_COMPLETE + "=true";
        }
        merchantPreferences.setReturnUrl(returnUrl);
        merchantPreferences
                .setAcceptedPaymentType(MessageConstants.MERCHANTPREF_ACCEPTEDPAYMENTTYPE_INSTANT);
        plan.setMerchantPreferences(merchantPreferences);
        return plan;
    }

    protected Payer constructPayer(PaymentRequest paymentRequest) {
        Payer payer = new Payer();
        payer.setPaymentMethod(MessageConstants.PAYER_PAYMENTMETHOD_PAYPAL);
        return payer;
    }

    protected AgreementToken createAgreementToken(AgreementToken agreementToken,
            PaymentRequest paymentRequest) throws PaymentException {
        PayPalCreateAgreementTokenResponse response =
                (PayPalCreateAgreementTokenResponse) paypalCheckoutService.call(
                        new PayPalCreateAgreementTokenRequest(agreementToken,
                                paypalCheckoutService.constructAPIContext(paymentRequest)));
        return response.getAgreementToken();
    }

    protected String constructAgreementDescription(PaymentRequest paymentRequest) {
        return paypalCheckoutService.getConfigProperties().getPaymentDescription();
    }

}
