package org.broadleafcommerce.vendor.paypal.service;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.paypal.api.payments.Payment;

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
     */
    Payment createPayPalPayment(PaymentRequest paymentRequest, boolean performCheckoutOnReturn)
            throws PaymentException;

    /**
     * Updates the PayPal payment to be in sync with the order. This method should be used when
     * fulfillment or pricing information changes after Payment creation. For creating payment use
     * {@link #createPayPalPayment(PaymentRequest, boolean)}
     * 
     * @throws PaymentException
     * @param paymentRequest
     */
    void updatePayPalPaymentForFulfillment(PaymentRequest paymentRequest) throws PaymentException;

}
