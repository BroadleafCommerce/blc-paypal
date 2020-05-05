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

import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutRetryPolicyClassifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;

import com.paypal.base.rest.PayPalRESTException;

@Disabled
public class PayPalCheckoutRetryPolicyClassifierTest {

    DefaultPayPalCheckoutRetryPolicyClassifier retryPolicyClassifier;

    @BeforeEach
    void setup() {
        if (retryPolicyClassifier == null) {
            RetryPolicy retryPolicy = new SimpleRetryPolicy();
            retryPolicyClassifier = new DefaultPayPalCheckoutRetryPolicyClassifier(retryPolicy);
        }
    }

    @Test
    void testUnknownErrorRetryPolicy() {
        Throwable throwable = new RuntimeException();

        RetryPolicy retryPolicy = retryPolicyClassifier.classify(throwable);

        assertThat(retryPolicy).isNotNull();
        assertThat(retryPolicy).isInstanceOf(NeverRetryPolicy.class);
    }

    @Test
    void testInvalidRequestRetryPolicy() {
        Throwable throwable = buildPayPalRESTException(400);

        RetryPolicy retryPolicy = retryPolicyClassifier.classify(throwable);

        assertThat(retryPolicy).isNotNull();
        assertThat(retryPolicy).isInstanceOf(NeverRetryPolicy.class);
    }

    @Test
    void testNetworkErrorRetryPolicy() {
        Throwable throwable = buildPayPalRESTException(408);

        RetryPolicy retryPolicy = retryPolicyClassifier.classify(throwable);

        assertThat(retryPolicy).isNotNull();
        assertThat(retryPolicy).isInstanceOf(SimpleRetryPolicy.class);
    }

    @Test
    void test5xxErrorRetryPolicy() {
        Throwable throwable = buildPayPalRESTException(503);

        RetryPolicy retryPolicy = retryPolicyClassifier.classify(throwable);

        assertThat(retryPolicy).isNotNull();
        assertThat(retryPolicy).isInstanceOf(SimpleRetryPolicy.class);
    }

    private Throwable buildPayPalRESTException(int httpResponseCode) {
        PayPalRESTException payPalRESTException = new PayPalRESTException("PayPal REST Exception!");
        payPalRESTException.setResponsecode(httpResponseCode);
        return payPalRESTException;
    }

}
