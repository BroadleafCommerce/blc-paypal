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

import org.broadleafcommerce.vendor.paypal.api.AgreementToken;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalResponse;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.domain.PaymentResponse;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.ItemList;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.ShippingAddress;
import com.paypal.base.rest.APIContext;

/**
 * @author Elbert Bautista (elbertbautista)
 */
public interface PayPalCheckoutExternalCallService {

    PayPalCheckoutRestConfigurationProperties getConfigProperties();

    void setCommonDetailsResponse(AgreementToken response,
            PaymentResponse paymentResponse,
            PaymentRequest paymentRequest,
            boolean checkoutComplete);

    /**
     * Converts a PayPal payment into a PaymentResponse
     * 
     * @param response A PayPal payment that should be used to be converted into a PaymentResponse
     * @param responseDTO The response dto that should be used to copy information from the PayPal
     *        payment
     */
    void setCommonDetailsResponse(Payment response, PaymentResponse responseDTO);

    ShippingAddress getPayPalShippingAddress(PaymentRequest paymentRequest);

    ItemList getPayPalItemList(PaymentRequest paymentRequest,
            boolean shouldPopulateShipping);

    Amount getPayPalAmountFromOrder(PaymentRequest paymentRequest);

    /**
     * Makes a request to PayPal
     * 
     * @param paymentRequest The payment request that should be executed. The operation that is
     *        executed is depedent on which implementation of {@link PayPalRequest} is sent
     * @return the respective PayPalResponse that corresponds to the given PayPalRequest
     * @throws PaymentException
     */
    PayPalResponse call(PayPalRequest paymentRequest) throws PaymentException;

    APIContext constructAPIContext(PaymentRequest paymentRequest);
}
