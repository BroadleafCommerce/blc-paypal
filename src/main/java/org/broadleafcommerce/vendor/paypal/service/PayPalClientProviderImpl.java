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
package org.broadleafcommerce.vendor.paypal.service;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpRequest;
import com.paypal.orders.Order;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutConfiguration;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalCheckoutEnvironmentType;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.util.Map;
import java.util.UUID;

/**
 * @author Nathan Moore (nathandmoore)
 */
@Service("blPayPalClientProvider")
public class PayPalClientProviderImpl implements PayPalClientProvider {

    private PayPalHttpClient client;

    @Resource(name = "blPayPalCheckoutConfiguration")
    protected PayPalCheckoutConfiguration configuration;

    @PostConstruct
    public void init() {
        this.client = configureClient(configuration);
    }

    private PayPalHttpClient configureClient(PayPalCheckoutConfiguration configuration) {
        PayPalEnvironment environment;

        if (PayPalCheckoutEnvironmentType.SANDBOX.type().equals(configuration.getEnvironment())) {
            environment = new PayPalEnvironment.Sandbox(configuration.getClientId(),
                    configuration.getClientSecret());
        } else {
            environment = new PayPalEnvironment.Live(configuration.getClientId(),
                    configuration.getClientSecret());
        }

        return new PayPalHttpClient(environment);
    }

    @Override
    public void configureRequest(HttpRequest<?> request,
            PaymentRequestDTO paymentRequest) {
        request.header(MessageConstants.BN, (MessageConstants.BNCODE));
        request.header(MessageConstants.HTTP_HEADER_REQUEST_ID,
                buildIdempotencyKey(paymentRequest));

        Map<String, Object> additionalFields = paymentRequest.getAdditionalFields();

        if (additionalFields.containsKey(MessageConstants.HTTP_HEADER_AUTH_ASSERTION)) {
            request.header(MessageConstants.HTTP_HEADER_AUTH_ASSERTION,
                    (String) additionalFields.get(MessageConstants.HTTP_HEADER_AUTH_ASSERTION));
        }

        if (additionalFields.containsKey(MessageConstants.HTTP_HEADER_CLIENT_METADATA_ID)) {
            request.header(MessageConstants.HTTP_HEADER_CLIENT_METADATA_ID,
                    (String) additionalFields.get(MessageConstants.HTTP_HEADER_CLIENT_METADATA_ID));
        }

        if (additionalFields.containsKey(MessageConstants.HTTP_HEADER_MOCK_RESPONSE)) {
            request.header(MessageConstants.HTTP_HEADER_MOCK_RESPONSE,
                    (String) additionalFields.get(MessageConstants.HTTP_HEADER_MOCK_RESPONSE));
        }
    }

    /**
     * Builds or gathers the idempotencyKey for the request. This value will guarantee that the
     * request cannot be processed twice & that if the request is made twice, that the second
     * response will be the same as the first response.
     * <p>
     * <blockquote> Note: this value must be unique for each transaction type. For example, if we
     * want to authorize & later capture a PayPal {@link Order}, the capture request's idempotency
     * key must be different than the authorization request's idempotency key.</blockquote>
     *
     * @param paymentRequest the request that will be sent to PayPal
     * @return the idempotency key
     */
    protected String buildIdempotencyKey(PaymentRequestDTO paymentRequest) {
        Map<String, Object> additionalFields = paymentRequest.getAdditionalFields();

        if (additionalFields.containsKey(MessageConstants.IDEMPOTENCY_KEY)) {
            return (String) additionalFields.get(MessageConstants.IDEMPOTENCY_KEY);
        } else {
            return UUID.randomUUID().toString();
        }
    }

    @Override
    public PayPalHttpClient getClient() {
        return client;
    }
}
