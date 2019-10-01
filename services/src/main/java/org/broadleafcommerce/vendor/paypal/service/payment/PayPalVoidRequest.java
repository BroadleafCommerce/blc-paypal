package org.broadleafcommerce.vendor.paypal.service.payment;

import org.apache.commons.lang3.StringUtils;

import com.paypal.api.payments.Authorization;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;


public class PayPalVoidRequest extends PayPalRequest {

    protected Authorization authorization;

    public PayPalVoidRequest(Authorization authorization, APIContext apiContext) {
        super(apiContext);
        this.authorization = authorization;
    }

    @Override
    protected PayPalResponse executeInternal() throws PayPalRESTException {
        return new PayPalVoidResponse(authorization.doVoid(apiContext));
    }

    @Override
    protected boolean isRequestValid() {
        return authorization != null && StringUtils.isNoneBlank(authorization.getId());
    }

}
