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
package org.broadleafcommerce.payment.service.gateway;

import org.springframework.classify.Classifier;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;

import com.paypal.base.rest.PayPalRESTException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Implementation of a {@link Classifier} that is used to identify a {@link RetryPolicy} based on a
 * provided {@link Throwable}. If a RetryPolicy is not identified, then a {@link NeverRetryPolicy}
 * should be returned - ie only retry for specific identified errors.
 *
 * @author Chris Kittrell (ckittrell)
 */
@RequiredArgsConstructor
public class DefaultPayPalCheckoutRetryPolicyClassifier
        implements Classifier<Throwable, RetryPolicy> {

    @Getter(AccessLevel.PROTECTED)
    private final RetryPolicy retryPolicy;

    @Override
    public RetryPolicy classify(Throwable throwable) {
        if (isNetworkError(throwable) || is5xxError(throwable)) {
            return retryPolicy;
        }

        return new NeverRetryPolicy();
    }

    /**
     * Determines whether or not the given throwable coming from PayPal's SDK is their
     * representation of a network error
     *
     * @param throwable the throwable that is returned by PayPal's SDK
     * @return whether or not the given throwable represents a network error
     */
    protected boolean isNetworkError(Throwable throwable) {
        if (throwable instanceof PayPalRESTException) {
            PayPalRESTException restException = (PayPalRESTException) throwable;
            int httpResponseCode = restException.getResponsecode();

            return (408 == httpResponseCode);
        }

        return false;
    }

    /**
     * Determines whether or not the given throwable coming from PayPal's SDK is their
     * representation of a 5xx error
     *
     * @param throwable the throwable that is returned by PayPal's SDK
     * @return whether or not the given throwable represents a 5xx error
     */
    protected boolean is5xxError(Throwable throwable) {
        if (throwable instanceof PayPalRESTException) {
            PayPalRESTException restException = (PayPalRESTException) throwable;
            int httpResponseCode = restException.getResponsecode();

            return is5xxError(httpResponseCode);
        }

        return false;
    }

    private boolean is5xxError(int httpResponseCode) {
        return (httpResponseCode / 100) == 5;
    }

}
