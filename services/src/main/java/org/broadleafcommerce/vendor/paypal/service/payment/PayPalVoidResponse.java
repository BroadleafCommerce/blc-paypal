package org.broadleafcommerce.vendor.paypal.service.payment;

import com.paypal.api.payments.Authorization;

public class PayPalVoidResponse implements PayPalResponse {

    protected Authorization voidedAuthorization;

    public PayPalVoidResponse(Authorization voidedAuthorization) {
        this.voidedAuthorization = voidedAuthorization;
    }

    public Authorization getVoidedAuthorization() {
        return voidedAuthorization;
    }
}
