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
package org.broadleafcommerce.payment.service.gateway;

import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalResponse;

/**
 * @author Elbert Bautista (elbertbautista)
 */
public interface ExternalCallPayPalCheckoutService {

    PayPalCheckoutConfiguration getConfiguration();

    /**
     * Makes a request to PayPal
     *
     * @param paymentRequest The payment request that should be executed. The operation that is
     *        executed is dependent on which implementation of {@link PayPalRequest} is sent
     * @param responseType The type of the response expected
     *
     * @return the respective {@link PayPalResponse} that corresponds to the given
     *         {@code responseType}
     *
     * @throws PaymentException if the {@link PayPalRequest} returns an exception once executed.
     *         This could be because the initial request is configured invalidly or because the
     *         PayPal APIs responded with an error.
     */
    <T extends PayPalResponse> T call(PayPalRequest paymentRequest, Class<T> responseType) throws PaymentException;
}
