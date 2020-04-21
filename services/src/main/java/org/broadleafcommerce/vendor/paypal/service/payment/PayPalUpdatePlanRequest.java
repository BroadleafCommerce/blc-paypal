/*
 * Copyright (C) 2009 - 2020 Broadleaf Commerce
 *
 * Licensed under the Broadleaf End User License Agreement (EULA), Version 1.1 (the
 * "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt).
 *
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the
 * "Custom License") between you and Broadleaf Commerce. You may not use this file except in
 * compliance with the applicable license.
 *
 * NOTICE: All information contained herein is, and remains the property of Broadleaf Commerce, LLC
 * The intellectual and technical concepts contained herein are proprietary to Broadleaf Commerce,
 * LLC and may be covered by U.S. and Foreign Patents, patents in process, and are protected by
 * trade secret or copyright law. Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained from Broadleaf Commerce, LLC.
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
