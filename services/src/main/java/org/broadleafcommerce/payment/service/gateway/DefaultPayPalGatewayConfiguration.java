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

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.broadleafcommerce.paymentgateway.domain.enums.DefaultGatewayFeatureType;
import com.broadleafcommerce.paymentgateway.domain.enums.DefaultTransactionTypes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Elbert Bautista (elbertbautista)
 * @author Chris Kittrell (ckittrell)
 */
@ConfigurationProperties("broadleaf.paypal-checkout.gateway.v1")
public class DefaultPayPalGatewayConfiguration implements PayPalGatewayConfiguration {

    /**
     * Determines how many times a transaction failure is reported. Default is once.
     */
    @Getter
    @Setter
    private int failureReportingThreshold = 1;

    /**
     * @see #getSupportedTransactionTypes()
     */
    @Getter
    private final Set<String> supportedTransactionTypes =
            new HashSet<>(Arrays.asList(DefaultTransactionTypes.AUTHORIZE.name(),
                    DefaultTransactionTypes.CAPTURE.name(),
                    DefaultTransactionTypes.AUTHORIZE_AND_CAPTURE.name(),
                    DefaultTransactionTypes.REFUND.name(),
                    DefaultTransactionTypes.REVERSE_AUTH.name()));

    /**
     * @see #getSupportedFeatures()
     */
    @Getter
    private final Set<String> supportedFeatures =
            new HashSet<>(
                    Arrays.asList(DefaultGatewayFeatureType.MULTI_USE_PAYMENT_METHODS.name()));

    /**
     * @see #getGatewayType()
     */
    @Getter
    private final String gatewayType = PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT.name();

}
