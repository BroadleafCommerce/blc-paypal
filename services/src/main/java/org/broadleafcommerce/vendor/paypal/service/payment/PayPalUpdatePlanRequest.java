package org.broadleafcommerce.vendor.paypal.service.payment;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.paypal.api.payments.Patch;
import com.paypal.api.payments.Plan;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

import java.util.List;

public class PayPalUpdatePlanRequest extends PayPalRequest {

    protected Plan plan;
    protected List<Patch> patches;

    public PayPalUpdatePlanRequest(Plan plan, List<Patch> patches, APIContext apiContext) {
        super(apiContext);
        this.plan = plan;
        this.patches = patches;
    }

    @Override
    protected PayPalResponse executeInternal() throws PayPalRESTException {
        plan.update(apiContext, patches);
        return new PayPalUpdatePlanResponse();
    }

    @Override
    protected boolean isRequestValid() {
        boolean paymentValid = plan != null && StringUtils.isNotBlank(plan.getId());
        if (paymentValid && CollectionUtils.isNotEmpty(patches)) {
            for (Patch patch : patches) {
                if (patch == null || StringUtils.isBlank(patch.getPath())
                        || StringUtils.isBlank(patch.getOp())) {
                    return false;
                }
                if (patch.getOp() != "remove" && patch.getValue() == null) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
