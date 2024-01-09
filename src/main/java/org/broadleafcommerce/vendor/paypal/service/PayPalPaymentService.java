/*-
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2024 Broadleaf Commerce
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
package org.broadleafcommerce.vendor.paypal.service;

import com.paypal.orders.Order;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;

public interface PayPalPaymentService {

    /**
     * Creates a PayPal {@link Order}
     * @param performCheckoutOnReturn Indicates if we should start checkout after the user has authorized the payment
     * 
     * @return The new {@link Order}
     * @throws PaymentException
     */
    Order createPayPalOrderForCurrentOrder(boolean performCheckoutOnReturn) throws PaymentException;

    /**
     * Updates the PayPal {@link Order} to be in sync with the Broadleaf order. This method should be used when fulfillment or pricing information changes
     * after {@link Order} creation. For creating payment use {@link #createPayPalOrderForCurrentOrder(boolean)}
     * 
     * @throws PaymentException
     */
    void updatePayPalOrderForFulfillment() throws PaymentException;

    PaymentRequestDTO getPaymentRequestForCurrentOrder() throws PaymentException;

    String getPayPalOrderIdFromCurrentOrder() throws PaymentException;

    String getPayPalPayerIdFromCurrentOrder() throws PaymentException;

    void setPayPalOrderIdOnCurrentOrder(String orderId) throws PaymentException;

    void setPayPalPayerIdOnCurrentOrder(String payerId) throws PaymentException;

}
