package org.broadleafcommerce.vendor.paypal.service.payment;

import com.paypal.api.payments.Plan;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

public class PayPalCreatePlanRequest extends PayPalRequest {

    protected Plan plan;

    public PayPalCreatePlanRequest(Plan plan, APIContext apiContext) {
        super(apiContext);
        this.plan = plan;
    }

    @Override
    protected PayPalResponse executeInternal() throws PayPalRESTException {
        return new PayPalCreatePlanResponse(plan.create(apiContext));
    }

    @Override
    protected boolean isRequestValid() {
        return (plan != null && plan.getType() != null);
    }

}
