/*-
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2022 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
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
                if (patch == null || StringUtils.isBlank(patch.getPath()) || StringUtils.isBlank(patch.getOp())) {
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

