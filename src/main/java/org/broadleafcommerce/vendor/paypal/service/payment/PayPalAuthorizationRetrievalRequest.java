/*-
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2023 Broadleaf Commerce
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
import com.paypal.payments.AuthorizationsGetRequest;
import org.apache.commons.lang3.StringUtils;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.vendor.paypal.service.PayPalClientProvider;

import java.io.IOException;

public class PayPalAuthorizationRetrievalRequest
        extends AbstractPayPalRequest<Authorization, AuthorizationsGetRequest> {

    private final String authId;

    public PayPalAuthorizationRetrievalRequest(PayPalClientProvider clientProvider,
                                               PaymentRequestDTO paymentRequest,
                                               String authId) {
        super(clientProvider, paymentRequest);
        this.authId = authId;
    }

    @Override
    protected AuthorizationsGetRequest buildRequest() {
        return new AuthorizationsGetRequest(getAuthId());
    }

    @Override
    protected AbstractPayPalResponse<Authorization> executeInternal() throws IOException {
        HttpResponse<Authorization> response = getClient().execute(getRequest());
        return new PayPalAuthorizationRetrievalResponse(response);
    }

    @Override
    protected boolean isValidInternal() {
        return StringUtils.isNotBlank(getAuthId());
    }

    protected String getAuthId() {
        return this.authId;
    }
}
