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
package org.broadleafcommerce.vendor.paypal.service.payment;

import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpRequest;
import com.paypal.orders.Order;
import com.paypal.orders.OrdersCreateRequest;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.vendor.paypal.service.PayPalClientProvider;
import org.broadleafcommerce.vendor.paypal.service.exception.PayPalInvalidRequestStateException;
import org.broadleafcommerce.vendor.paypal.service.exception.PayPalRESTException;

import java.io.IOException;

/**
 * Represents a request to make against the PayPal REST APIs. It should encapsulate everything
 * necessary to make the request and return a response.
 *
 * @param <RES> The response content type such as {@link Order}
 * @param <REQ> The request type such as {@link OrdersCreateRequest}
 */
public abstract class AbstractPayPalRequest<RES, REQ extends HttpRequest<RES>>
        implements PayPalRequest {

    /**
     * The {@link PayPalClientProvider} used to configure and execute requests.
     */
    private final PayPalClientProvider clientProvider;

    private final PaymentRequestDTO paymentRequest;

    /**
     * The {@link HttpRequest} that is built to facilitate executing request.
     */
    private REQ request;

    /**
     * Whether the request has been executed.
     */
    private boolean executed = false;

    public AbstractPayPalRequest(PayPalClientProvider clientProvider,
                                 PaymentRequestDTO paymentRequest) {
        this.clientProvider = clientProvider;
        this.paymentRequest = paymentRequest;
    }

    @Override
    public PayPalResponse execute() {
        if (request == null) {
            configureRequest(paymentRequest);
        }

        if (isValid()) {
            setExecuted(true);

            try {
                return executeInternal();
            } catch (IOException e) {
                throw new PayPalRESTException(e);
            }
        }

        throw new PayPalInvalidRequestStateException("Request state is invalid");
    }

    /**
     * Whether {@code this} is configured correctly and can execute the request. Relies upon
     * {@link #isValidInternal()} and whether the request has already been {@link #executed}.
     *
     * @return Whether {@code this} is configured correctly and can execute the request.
     */
    protected boolean isValid() {
        return isValidInternal() && !executed;
    }

    /**
     * Configures {@link #request} with content and headers. Defers to {@link #buildRequest()} for
     * most of the work.
     */
    protected void configureRequest(PaymentRequestDTO paymentRequest) {
        this.request = buildRequest();
        getClientProvider().configureRequest(request, paymentRequest);
    }

    /**
     * Convenience method for getting the {@link PayPalClientProvider#getClient()} from
     * {@link #clientProvider}.
     *
     * @return {@code clientProvider.client}
     */
    protected PayPalHttpClient getClient() {
        return getClientProvider().getClient();
    }

    /**
     * Method to build out {@link #request} and provide it the implementation-specific configuration
     * such as headers and request body.
     *
     * @return The configured request
     */
    protected abstract REQ buildRequest();

    /**
     * Executes the configured {@link #request} using the {@link #getClient() PayPalHttpClient}.
     *
     * @return The response from PayPal.
     * @throws IOException Thrown if the request execution fails.
     */
    protected abstract AbstractPayPalResponse<RES> executeInternal() throws IOException;

    /**
     * Implementation specific determination of whether the request can be executed.
     *
     * @return Whether the request can be executed or is in an erroneous state.
     */
    protected abstract boolean isValidInternal();

    protected PayPalClientProvider getClientProvider() {
        return this.clientProvider;
    }

    protected PaymentRequestDTO getPaymentRequest() {
        return this.paymentRequest;
    }

    protected REQ getRequest() {
        return this.request;
    }

    protected boolean isExecuted() {
        return this.executed;
    }

    private void setExecuted(boolean executed) {
        this.executed = executed;
    }
}
