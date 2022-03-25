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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.domain.PaymentResponse;
import com.broadleafcommerce.paymentgateway.domain.enums.DefaultTransactionTypes;
import com.broadleafcommerce.paymentgateway.service.PaymentGatewayTransactionService;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;

import lombok.RequiredArgsConstructor;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@RequiredArgsConstructor
public class DefaultPayPalCheckoutHostedService implements PayPalCheckoutHostedService {

    protected static final Log LOG = LogFactory.getLog(DefaultPayPalCheckoutHostedService.class);

    private final PaymentGatewayTransactionService transactionService;

    @Override
    public PaymentResponse requestHostedEndpoint(PaymentRequest paymentRequest, boolean capture)
            throws PaymentException {

        PaymentResponse responseDTO;
        if (capture) {
            responseDTO = transactionService.authorizeAndCapture(paymentRequest);
            responseDTO.transactionType(DefaultTransactionTypes.AUTHORIZE_AND_CAPTURE);
        } else {
            responseDTO = transactionService.authorize(paymentRequest);
            responseDTO.transactionType(DefaultTransactionTypes.AUTHORIZE);
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("Request to PayPal Checkout Hosted Endpoint with raw response: " +
                    responseDTO.getRawResponse());
        }

        return responseDTO;

    }

    @Override
    public String getGatewayType() {
        return PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT.name();
    }

}
