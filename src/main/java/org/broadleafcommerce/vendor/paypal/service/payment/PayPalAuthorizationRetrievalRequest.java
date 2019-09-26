package org.broadleafcommerce.vendor.paypal.service.payment;

import org.apache.commons.lang3.StringUtils;

import com.paypal.api.payments.Authorization;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;


public class PayPalAuthorizationRetrievalRequest extends PayPalRequest {

    protected String authId;

    public PayPalAuthorizationRetrievalRequest(String authId, APIContext apiContext) {
        super(apiContext);
        this.authId = authId;
    }

    @Override
    protected PayPalResponse executeInternal() throws PayPalRESTException {
        return new PayPalAuthorizationRetrievalResponse(Authorization.get(apiContext, authId));
    }

    @Override
    protected boolean isRequestValid() {
        return StringUtils.isNoneBlank(authId);
    }

}
