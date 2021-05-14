/*-
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2021 Broadleaf Commerce
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

import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpRequest;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutConfiguration;

/**
 * Identifies a service that configures and provides access to a {@link PayPalHttpClient} with which
 * to execute requests based on {@link PayPalCheckoutConfiguration}. It also provides common
 * configuration for outgoing {@link HttpRequest PayPal HttpRequests}.
 *
 * @author Nathan Moore (nathandmoore)
 */
public interface PayPalClientProvider {

    /**
     * Returns the configured {@link PayPalHttpClient}.
     *
     * @return the configured {@link PayPalHttpClient}.
     */
    PayPalHttpClient getClient();

    /**
     * Configures the {@link HttpRequest PayPal request} based on the {@link PaymentRequestDTO}. This
     * will add the request ID and various headers common to all requests.
     *
     * @param request The {@link HttpRequest} to configure
     * @param paymentRequest The {@link PaymentRequestDTO} sent to the gateway
     */
    void configureRequest(HttpRequest<?> request, PaymentRequestDTO paymentRequest);
}
