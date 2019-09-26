package org.broadleafcommerce.vendor.paypal.service.payment;

import com.paypal.api.payments.WebProfile;

public class PayPalCreateWebProfileResponse implements PayPalResponse {

    protected WebProfile webProfile;

    public PayPalCreateWebProfileResponse(WebProfile webProfile) {
        this.webProfile = webProfile;
    }

    public WebProfile getWebProfile() {
        return webProfile;
    }
}
