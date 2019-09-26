package org.broadleafcommerce.vendor.paypal.service;

import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.paypal.api.payments.Payment;

public interface PayPalPaymentService {

    /**
     * Creates a PayPal payment
     * 
     * @param performCheckoutOnReturn Indicates if we should start checkout after the user has
     *        authorized the payment
     * 
     * @return The new payment
     * @throws PaymentException
     */
    Payment createPayPalPaymentForCurrentOrder(boolean performCheckoutOnReturn)
            throws PaymentException;

    /**
     * Updates the PayPal payment to be in sync with the order. This method should be used when
     * fulfillment or pricing information changes after Payment creation. For creating payment use
     * {@link #createPayPalPaymentForCurrentOrder(boolean)}
     * 
     * @throws PaymentException
     */
    void updatePayPalPaymentForFulfillment() throws PaymentException;

    String getPayPalPaymentIdFromCurrentOrder() throws PaymentException;

    String getPayPalPayerIdFromCurrentOrder() throws PaymentException;

    void setPayPalPaymentIdOnCurrentOrder(String paymentId) throws PaymentException;

    void setPayPalPayerIdOnCurrentOrder(String payerId) throws PaymentException;

}
