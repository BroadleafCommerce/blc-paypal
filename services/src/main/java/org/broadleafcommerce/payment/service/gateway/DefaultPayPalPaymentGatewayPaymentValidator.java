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

import org.apache.commons.lang3.StringUtils;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.springframework.lang.Nullable;

import com.broadleafcommerce.paymentgateway.domain.PaymentValidationRequest;
import com.broadleafcommerce.paymentgateway.service.exception.InvalidPaymentConfigurationException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The default implementation of {@link PayPalPaymentGatewayPaymentValidator}.
 *
 * @author Dima Myroniuk (dmyroniuk)
 */
@RequiredArgsConstructor
public class DefaultPayPalPaymentGatewayPaymentValidator
        implements PayPalPaymentGatewayPaymentValidator {
    private static final String REQUIRED_PROPERTY_ERROR =
            "The payment gateway property \" %s \" is required!";

    @Getter(AccessLevel.PROTECTED)
    private final PayPalGatewayConfiguration gatewayConfiguration;

    @Override
    public void validatePayment(PaymentValidationRequest paymentValidationRequest,
            @Nullable String applicationId,
            @Nullable String tenantId) {
        String paymentId = paymentValidationRequest.getPaymentMethodProperties()
                .get(MessageConstants.PAYMENTID);

        if (StringUtils.isBlank(paymentId)) {
            throw new InvalidPaymentConfigurationException(
                    String.format(REQUIRED_PROPERTY_ERROR, MessageConstants.PAYMENTID));
        }
    }

    @Override
    public String getGatewayType() {
        return PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT.name();
    }
}
