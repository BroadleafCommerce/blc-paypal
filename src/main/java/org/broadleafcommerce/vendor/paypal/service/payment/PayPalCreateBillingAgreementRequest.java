package org.broadleafcommerce.vendor.paypal.service.payment;

import com.paypal.api.payments.Agreement;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

public class PayPalCreateBillingAgreementRequest extends PayPalRequest {

    protected Agreement agreement;

    public PayPalCreateBillingAgreementRequest(Agreement agreement, APIContext apiContext) {
        super(apiContext);
        this.agreement = agreement;
    }

    @Override
    protected PayPalResponse executeInternal() throws PayPalRESTException {
        try {
            return new PayPalCreateBillingAgreementResponse(agreement.create(apiContext));
        } catch (Exception e) {
            throw new PayPalRESTException(e);
        }
    }

    @Override
    protected boolean isRequestValid() {
        return (agreement != null && agreement.getPayer() != null && agreement.getPlan() != null);
    }

}
