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
package org.broadleafcommerce.payment.service.gateway.autoconfigure;

import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutExternalCallService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutHostedService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutReportingService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutRetryPolicyClassifier;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutRollbackService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutTransactionService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalGatewayConfiguration;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalPaymentGatewayPaymentValidator;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalSyncTransactionService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutExternalCallService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutHostedService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutReportingService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutRestConfigurationProperties;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutRollbackService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutTransactionService;
import org.broadleafcommerce.payment.service.gateway.PayPalGatewayConfiguration;
import org.broadleafcommerce.payment.service.gateway.PayPalPaymentGatewayPaymentValidator;
import org.broadleafcommerce.payment.service.gateway.PayPalSyncTransactionService;
import org.broadleafcommerce.vendor.paypal.service.PayPalPaymentService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import com.broadleafcommerce.paymentgateway.util.PaymentResponseUtil;

@Configuration
@EnableConfigurationProperties({PayPalCheckoutRestConfigurationProperties.class,
        DefaultPayPalGatewayConfiguration.class})
public class PayPalServiceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PayPalCheckoutExternalCallService payPalCheckoutExternalCallService(
            PayPalCheckoutRestConfigurationProperties configProperties,
            PayPalGatewayConfiguration gatewayConfiguration) {
        return new DefaultPayPalCheckoutExternalCallService(configProperties,
                gatewayConfiguration);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalSyncTransactionService payPalSyncTransactionService(
            PayPalCheckoutExternalCallService paypalCheckoutService) {
        return new DefaultPayPalSyncTransactionService(paypalCheckoutService);
    }

    /**
     * Retries 3 times with a second between each attempt when a network or 5xx error is encountered
     */
    @Bean
    @ConditionalOnMissingBean(name = "payPalCheckoutExternalCallRetryTemplate")
    public RetryTemplate payPalCheckoutExternalCallRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        ExceptionClassifierRetryPolicy retryPolicy = new ExceptionClassifierRetryPolicy();
        Classifier<Throwable, RetryPolicy> classifier =
                new DefaultPayPalCheckoutRetryPolicyClassifier(new SimpleRetryPolicy());
        retryPolicy.setExceptionClassifier(classifier);
        retryTemplate.setRetryPolicy(retryPolicy);

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalCheckoutTransactionService payPalCheckoutTransactionService(
            PayPalCheckoutExternalCallService paypalCheckoutService,
            PayPalPaymentService payPalPaymentService,
            PayPalCheckoutRestConfigurationProperties configProperties,
            @Qualifier("payPalCheckoutExternalCallRetryTemplate") RetryTemplate retryTemplate,
            PaymentResponseUtil paymentResponseUtil) {
        return new DefaultPayPalCheckoutTransactionService(paypalCheckoutService,
                payPalPaymentService,
                configProperties,
                retryTemplate,
                paymentResponseUtil);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalCheckoutRollbackService payPalCheckoutRollbackService(
            PayPalCheckoutTransactionService transactionService) {
        return new DefaultPayPalCheckoutRollbackService(transactionService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalCheckoutHostedService payPalCheckoutHostedService(
            PayPalCheckoutTransactionService transactionService) {
        return new DefaultPayPalCheckoutHostedService(transactionService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalCheckoutReportingService payPalCheckoutReportingService(
            PayPalCheckoutExternalCallService paypalCheckoutService) {
        return new DefaultPayPalCheckoutReportingService(paypalCheckoutService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalPaymentGatewayPaymentValidator payPalPaymentGatewayPaymentValidator(
            PayPalGatewayConfiguration gatewayConfiguration) {
        return new DefaultPayPalPaymentGatewayPaymentValidator(gatewayConfiguration);
    }

}
