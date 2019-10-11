package org.broadleafcommerce.vendor.paypal.service.payment;

import com.paypal.api.payments.Plan;

public class PayPalCreatePlanResponse implements PayPalResponse {

    protected Plan plan;

    public PayPalCreatePlanResponse(Plan plan) {
        this.plan = plan;
    }

    public Plan getPlan() {
        return plan;
    }
}
