/*
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.payment.service.gateway;

import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalPaymentInfoDTO;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalResponse;

import com.paypal.api.payments.Payment;

/**
 * @author Elbert Bautista (elbertbautista)
 */
public interface ExternalCallPayPalExpressService {

    PayPalExpressConfiguration getConfiguration();

    /**
     * Converts a PayPal payment into a PaymentResponseDTO
     * 
     * @param response A PayPal payment that should be used to be converted into a PaymentResponseDTO
     * @param responseDTO The response dto that should be used to copy information from the PayPal payment
     */
    void setCommonDetailsResponse(Payment response, PaymentResponseDTO responseDTO);

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

    PayPalResponse call(PayPalRequest paymentRequest) throws PaymentException;
    /**
     * Makes a request to PayPal
     * 
     * @param paymentRequest The payment request that should be executed. The operation that is executed is depedent on which implementation of {@link PayPalRequest} is sent
     * @return the respective PayPalResponse that corresponds to the given PayPalRequest
     * @throws PaymentException
     */

}
