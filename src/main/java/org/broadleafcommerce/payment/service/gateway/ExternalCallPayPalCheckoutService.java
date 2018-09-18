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
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalResponse;

import com.paypal.api.payments.Payment;

/**
 * @author Elbert Bautista (elbertbautista)
 */
public interface ExternalCallPayPalCheckoutService {

    PayPalCheckoutConfiguration getConfiguration();

    /**
     * Converts a PayPal payment into a PaymentResponseDTO
     * 
     * @param response A PayPal payment that should be used to be converted into a PaymentResponseDTO
     * @param responseDTO The response dto that should be used to copy information from the PayPal payment
     */
    void setCommonDetailsResponse(Payment response, PaymentResponseDTO responseDTO);

    /**
     * Makes a request to PayPal
     * 
     * @param paymentRequest The payment request that should be executed. The operation that is executed is depedent on which implementation of {@link PayPalRequest} is sent
     * @return the respective PayPalResponse that corresponds to the given PayPalRequest
     * @throws PaymentException
     */
    PayPalResponse call(PayPalRequest paymentRequest) throws PaymentException;

}
