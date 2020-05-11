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
package org.broadleaf.payment.service.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutRetryPolicyClassifier;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutTransactionService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutExternalCallService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutRestConfigurationProperties;
import org.broadleafcommerce.vendor.paypal.service.PayPalPaymentService;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.classify.Classifier;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.domain.PaymentResponse;
import com.broadleafcommerce.paymentgateway.domain.enums.DefaultTransactionFailureTypes;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.paypal.api.payments.Error;
import com.paypal.base.rest.PayPalRESTException;

@ExtendWith(MockitoExtension.class)
public class PayPalCheckoutTransactionServiceTest {

    DefaultPayPalCheckoutTransactionService transactionService;

    @Mock
    PayPalCheckoutExternalCallService externalCallService;

    @Mock
    PayPalPaymentService payPalPaymentService;

    @Mock
    PayPalCheckoutRestConfigurationProperties configProperties;

    @BeforeEach
    void setup() {
        if (transactionService == null) {
            RetryTemplate retryTemplate = new RetryTemplate();

            ExceptionClassifierRetryPolicy retryPolicy = new ExceptionClassifierRetryPolicy();
            Classifier<Throwable, RetryPolicy> classifier =
                    new DefaultPayPalCheckoutRetryPolicyClassifier(new SimpleRetryPolicy());
            retryPolicy.setExceptionClassifier(classifier);
            retryTemplate.setRetryPolicy(retryPolicy);

            FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
            retryTemplate.setBackOffPolicy(backOffPolicy);

            transactionService = new DefaultPayPalCheckoutTransactionService(
                    externalCallService,
                    payPalPaymentService,
                    configProperties,
                    retryTemplate);

            PayPalRESTException payPalRESTException =
                    new PayPalRESTException("Network error!");
            payPalRESTException.setResponsecode(408);
            payPalRESTException.setDetails(new Error());
            when(externalCallService.call(any()))
                    .thenThrow(new PaymentException(payPalRESTException));
        }
    }

    @Test
    void testTransactionRetryForNetworkError() {
        PaymentRequest paymentRequest = new PaymentRequest();

        PaymentResponse response = transactionService.authorize(paymentRequest);

        assertThat(response.isSuccessful()).isFalse();
        assertThat(response.getFailureType())
                .isEqualTo(DefaultTransactionFailureTypes.NETWORK_ERROR.name());
        verify(externalCallService, times(3)).call(any(PayPalRequest.class));
    }

}
