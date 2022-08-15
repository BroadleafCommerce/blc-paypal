/*-
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2022 Broadleaf Commerce
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

import org.broadleafcommerce.common.vendor.service.exception.PaymentException;

import com.paypal.api.payments.Payment;

public interface PayPalPaymentService {

    /**
     * Creates a PayPal payment
     * @param performCheckoutOnReturn Indicates if we should start checkout after the user has authorized the payment
     * 
     * @return The new payment
     * @throws PaymentException
     */
    Payment createPayPalPaymentForCurrentOrder(boolean performCheckoutOnReturn) throws PaymentException;

    /**
     * Updates the PayPal payment to be in sync with the order. This method should be used when fulfillment or pricing information changes
     * after Payment creation. For creating payment use {@link #createPayPalPaymentForCurrentOrder(boolean)}
     * 
     * @throws PaymentException
     */
    void updatePayPalPaymentForFulfillment() throws PaymentException;

    String getPayPalPaymentIdFromCurrentOrder() throws PaymentException;

    String getPayPalPayerIdFromCurrentOrder() throws PaymentException;

    void setPayPalPaymentIdOnCurrentOrder(String paymentId) throws PaymentException;

    void setPayPalPayerIdOnCurrentOrder(String payerId) throws PaymentException;

}
