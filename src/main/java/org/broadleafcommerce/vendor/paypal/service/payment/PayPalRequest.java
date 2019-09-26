package org.broadleafcommerce.vendor.paypal.service.payment;

import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

public abstract class PayPalRequest {

    protected Boolean executed = false;
    protected APIContext apiContext;

    public PayPalRequest(APIContext apiContext) {
        this.apiContext = apiContext;
    }

    public PayPalResponse execute() throws PayPalRESTException {
        if (isValid()) {
            executed = true;
            return executeInternal();
        }
        throw new RuntimeException();
    }

    protected boolean isValid() {
        return apiContext != null && isRequestValid() && !executed;
    }

    protected abstract PayPalResponse executeInternal() throws PayPalRESTException;

    protected abstract boolean isRequestValid();
}
