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
package org.broadleafcommerce.vendor.paypal.service.autoconfigure;

import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutExternalCallService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutRestConfigurationProperties;
import org.broadleafcommerce.payment.service.gateway.PayPalGatewayConfiguration;
import org.broadleafcommerce.vendor.paypal.service.DefaultPayPalAgreementTokenService;
import org.broadleafcommerce.vendor.paypal.service.DefaultPayPalBillingAgreementService;
import org.broadleafcommerce.vendor.paypal.service.DefaultPayPalPaymentService;
import org.broadleafcommerce.vendor.paypal.service.DefaultPayPalWebExperienceProfileService;
import org.broadleafcommerce.vendor.paypal.service.PayPalAgreementTokenService;
import org.broadleafcommerce.vendor.paypal.service.PayPalBillingAgreementService;
import org.broadleafcommerce.vendor.paypal.service.PayPalPaymentService;
import org.broadleafcommerce.vendor.paypal.service.PayPalWebExperienceProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.paypal.api.payments.WebProfile;

@Configuration
public class PayPalVendorServiceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PayPalAgreementTokenService payPalAgreementTokenService(
            PayPalCheckoutExternalCallService paypalCheckoutService,
            PayPalCheckoutRestConfigurationProperties configurationProperties) {
        return new DefaultPayPalAgreementTokenService(paypalCheckoutService,
                configurationProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalBillingAgreementService payPalBillingAgreementService(
            PayPalCheckoutExternalCallService paypalCheckoutService,
            PayPalCheckoutRestConfigurationProperties configurationProperties) {
        return new DefaultPayPalBillingAgreementService(paypalCheckoutService,
                configurationProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalPaymentService payPalPaymentService(
            PayPalCheckoutExternalCallService paypalCheckoutService,
            PayPalCheckoutRestConfigurationProperties configurationProperties,
            PayPalGatewayConfiguration gatewayConfiguration,
            PayPalWebExperienceProfileService webExperienceProfileService,
            PayPalCheckoutRestConfigurationProperties properties) {
        return new DefaultPayPalPaymentService(paypalCheckoutService,
                configurationProperties,
                gatewayConfiguration,
                webExperienceProfileService,
                properties.shouldPopulateShippingOnCreatePayment());
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalWebExperienceProfileService payPalWebProfileService(
            PayPalCheckoutExternalCallService paypalCheckoutService,
            PayPalCheckoutRestConfigurationProperties configurationProperties,
            @Autowired(required = false) WebProfile webProfile) {
        return new DefaultPayPalWebExperienceProfileService(paypalCheckoutService,
                configurationProperties,
                webProfile);
    }

}
