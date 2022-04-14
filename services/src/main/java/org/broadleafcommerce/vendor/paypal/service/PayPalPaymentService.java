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

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.paypal.api.payments.Payment;

import lombok.NonNull;

public interface PayPalPaymentService {

    /**
     * Creates a PayPal payment
     *
     * @param paymentRequest
     * @param performCheckoutOnReturn Indicates if we should start checkout after the user has
     *        authorized the payment
     *
     * @return The new payment
     * @throws PaymentException
     * @deprecated in favour of {@link #createPayPalPayment(PaymentRequest, boolean, boolean)}
     */
    @Deprecated
    default Payment createPayPalPayment(PaymentRequest paymentRequest,
            boolean performCheckoutOnReturn)
            throws PaymentException {
        return createPayPalPayment(paymentRequest, performCheckoutOnReturn, false);
    }

    /**
     * Creates a PayPal payment
     *
     * @param paymentRequest
     * @param performCheckoutOnReturn Indicates if we should start checkout after the user has
     *        authorized the payment
     * @param capture whether or not capture the payment
     * @return The new payment
     * @throws PaymentException
     */
    Payment createPayPalPayment(PaymentRequest paymentRequest,
            boolean performCheckoutOnReturn,
            boolean capture)
            throws PaymentException;

    /**
     * Updates the PayPal payment to be in sync with the order. This method should be used when
     * fulfillment or pricing information changes after Payment creation. For creating payment use
     * {@link #createPayPalPayment(PaymentRequest, boolean, boolean)}
     * 
     * @throws PaymentException
     * @param paymentRequest
     */
    void updatePayPalPaymentForFulfillment(@NonNull PaymentRequest paymentRequest)
            throws PaymentException;

    /**
     * Updates the PayPal payment to include the provided custom value.
     *
     * @param paymentId the primary identifier of the PayPal Payment
     * @param custom the value that is to be stored on the PayPal Payment object
     * @throws PaymentException thrown if the request to update the Payment fails
     */
    void updatePaymentCustom(String paymentId, String custom) throws PaymentException;

}
