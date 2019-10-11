package org.broadleafcommerce.vendor.paypal.service.payment;

import org.apache.commons.lang3.StringUtils;

import com.paypal.api.payments.Agreement;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

public class PayPalExecuteBillingAgreementRequest extends PayPalRequest {

    protected String token;

    public PayPalExecuteBillingAgreementRequest(String token, APIContext apiContext) {
        super(apiContext);
        this.token = token;
    }

    @Override
    protected PayPalResponse executeInternal() throws PayPalRESTException {
        return new PayPalExecuteBillingAgreementResponse(Agreement.execute(apiContext, token));
    }

    @Override
    protected boolean isRequestValid() {
        return StringUtils.isNoneBlank(token);
    }

}
