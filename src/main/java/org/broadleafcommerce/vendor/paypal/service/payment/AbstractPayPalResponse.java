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
package org.broadleafcommerce.vendor.paypal.service.payment;

import com.paypal.http.HttpResponse;

/**
 * Represents a wrapper around the {@link HttpResponse} that results from an executed
 * {@link AbstractPayPalRequest}.
 *
 * @param <T> The type of the response body
 */
public abstract class AbstractPayPalResponse<T> implements PayPalResponse {

    /**
     * The {@link HttpResponse} provided by the PayPal APIs.
     */
    private final HttpResponse<T> response;

    /**
     * The content of {@link #response} for convenience.
     */
    private final T content;

    public AbstractPayPalResponse(HttpResponse<T> response) {
        this.response = response;
        this.content = response.result();
    }


    public HttpResponse<T> getResponse() {
        return response;
    }

    public T getContent() {
        return content;
    }
}
