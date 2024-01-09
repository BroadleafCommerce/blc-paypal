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
import com.paypal.payments.Authorization;
import com.paypal.payments.AuthorizationsReauthorizeRequest;
import com.paypal.payments.Money;
import com.paypal.payments.ReauthorizeRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.vendor.paypal.service.PayPalClientProvider;

import java.io.IOException;

/**
 * @author Nathan Moore (nathandmoore)
 */
public class PayPalReAuthorizeRequest
        extends AbstractPayPalRequest<Authorization, AuthorizationsReauthorizeRequest> {

    private final String authId;

    private final ReauthorizeRequest reauthorizeRequest;

    public PayPalReAuthorizeRequest(PayPalClientProvider clientProvider,
                                    PaymentRequestDTO paymentRequest, String authId,
                                    ReauthorizeRequest reauthorizeRequest) {
        super(clientProvider, paymentRequest);
        this.authId = authId;
        this.reauthorizeRequest = reauthorizeRequest;
    }

    @Override
    protected AuthorizationsReauthorizeRequest buildRequest() {
        AuthorizationsReauthorizeRequest request =
                new AuthorizationsReauthorizeRequest(getAuthId());
        request.requestBody(getReauthorizeRequest());
        return request;
    }

    @Override
    protected AbstractPayPalResponse<Authorization> executeInternal() throws IOException {
        HttpResponse<Authorization> response = getClient().execute(getRequest());
        return new PayPalReAuthorizeResponse(response);
    }

    @Override
    protected boolean isValidInternal() {
        ReauthorizeRequest request = getReauthorizeRequest();

        if (request == null) {
            return false;
        }

        Money amt = request.amount();
        return amt != null && StringUtils.isNoneBlank(getAuthId(), amt.currencyCode(), amt.value())
                && NumberUtils.isCreatable(amt.value());
    }

    protected String getAuthId() {
        return this.authId;
    }

    protected ReauthorizeRequest getReauthorizeRequest() {
        return this.reauthorizeRequest;
    }
}
