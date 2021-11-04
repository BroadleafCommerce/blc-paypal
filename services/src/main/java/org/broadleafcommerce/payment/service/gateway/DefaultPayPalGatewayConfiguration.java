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

import static com.broadleafcommerce.paymentgateway.domain.enums.DefaultGatewayFeatureType.MULTI_USE_PAYMENT_METHODS;
import static com.broadleafcommerce.paymentgateway.domain.enums.DefaultTransactionTypes.AUTHORIZE;
import static com.broadleafcommerce.paymentgateway.domain.enums.DefaultTransactionTypes.AUTHORIZE_AND_CAPTURE;
import static com.broadleafcommerce.paymentgateway.domain.enums.DefaultTransactionTypes.CAPTURE;
import static com.broadleafcommerce.paymentgateway.domain.enums.DefaultTransactionTypes.REFUND;
import static com.broadleafcommerce.paymentgateway.domain.enums.DefaultTransactionTypes.REVERSE_AUTH;
import static com.broadleafcommerce.paymentgateway.domain.enums.DefaultTransactionTypes.VOID;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.broadleafcommerce.paymentgateway.domain.enums.DefaultTransactionTypes;
import com.broadleafcommerce.paymentgateway.domain.enums.TransactionType;

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
     * Set the {@link TransactionType} that should be performed during checkout. Typically this will
     * be {@link DefaultTransactionTypes#AUTHORIZE_AND_CAPTURE} or
     * {@link DefaultTransactionTypes#AUTHORIZE}.
     */
    @Getter
    @Setter
    private String checkoutTransactionType = AUTHORIZE.name();

    /**
     * @see #getSupportedTransactionTypes()
     */
    @Getter
    private final Set<String> supportedTransactionTypes =
            new HashSet<>(Arrays.asList(AUTHORIZE.name(), CAPTURE.name(),
                    AUTHORIZE_AND_CAPTURE.name(), REFUND.name(), REVERSE_AUTH.name(), VOID.name()));

    /**
     * @see #getSupportedFeatures()
     */
    @Getter
    private final Set<String> supportedFeatures =
            new HashSet<>(Arrays.asList(MULTI_USE_PAYMENT_METHODS.name()));

    /**
     * @see #isCheckoutTransactionExternal()
     */
    @Getter
    private final boolean checkoutTransactionExternal = false;

    /**
     * @see #getGatewayType()
     */
    @Getter
    private final String gatewayType = PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT.name();

}
