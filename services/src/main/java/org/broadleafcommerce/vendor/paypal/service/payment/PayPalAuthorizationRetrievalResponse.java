package org.broadleafcommerce.vendor.paypal.service.payment;

import com.paypal.api.payments.Authorization;

public class PayPalAuthorizationRetrievalResponse implements PayPalResponse {

    protected Authorization authorization;

    public PayPalAuthorizationRetrievalResponse(Authorization authorization) {
        this.authorization = authorization;
    }

    public Authorization getAuthorization() {
        return authorization;
    }
}
