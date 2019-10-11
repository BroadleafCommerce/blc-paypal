package org.broadleafcommerce.vendor.paypal.service.payment;

import com.paypal.api.payments.WebProfile;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;


public class PayPalCreateWebProfileRequest extends PayPalRequest {

    protected WebProfile webProfile;

    public PayPalCreateWebProfileRequest(WebProfile webProfile, APIContext apiContext) {
        super(apiContext);
        this.webProfile = webProfile;
    }

    @Override
    protected PayPalResponse executeInternal() throws PayPalRESTException {
        return new PayPalCreateWebProfileResponse(webProfile.create(apiContext));
    }

    @Override
    protected boolean isRequestValid() {
        return webProfile != null;
    }

}
