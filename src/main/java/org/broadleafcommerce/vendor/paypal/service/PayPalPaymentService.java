package org.broadleafcommerce.vendor.paypal.service;

import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalPaymentInfoDTO;

import com.paypal.api.payments.Payment;

public interface PayPalPaymentService {

    /**
     * Creates a PayPal payment
     * 
     * @param order The order the payment is being created for
     * @param performCheckoutOnReturn Indicates if we should start checkout after the user has authorized the payment
     * @return The new payment
     * @throws PaymentException
     */
    Payment createPayment(Order order, boolean performCheckoutOnReturn) throws PaymentException;

    /**
     * Updates the PayPal payment to be in sync with the order. This method should be used when fulfillment or pricing information changes
     * after Payment creation. For creating payment use {@link #createPayment(Order, boolean)}
     * 
     * @param order The order that should be used to update the payment. The payment that updated is retrieved from the order's OrderPayments
     * @return
     * @throws PaymentException
     */
    PayPalPaymentInfoDTO updatePaymentForFulfillment(Order order) throws PaymentException;

}
