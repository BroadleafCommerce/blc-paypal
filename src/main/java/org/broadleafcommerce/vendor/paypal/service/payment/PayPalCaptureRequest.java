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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.paypal.api.payments.Authorization;
import com.paypal.api.payments.Capture;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;


public class PayPalCaptureRequest extends PayPalRequest {

    protected Authorization authorization;
    protected Capture capture;
    
    public PayPalCaptureRequest(Authorization authorization, Capture capture, APIContext apiContext) {
        super(apiContext);
        this.authorization = authorization;
        this.capture = capture;
    }
    
    @Override
    public PayPalResponse executeInternal() throws PayPalRESTException {
        return new PayPalCaptureResponse(authorization.capture(apiContext, capture));
    }

    @Override
    protected boolean isRequestValid() {
        return authorization != null && StringUtils.isNoneBlank(authorization.getId())
            && capture != null && capture.getAmount() != null && capture.getAmount().getDetails() == null
            && StringUtils.isNoneBlank(capture.getAmount().getCurrency()) && StringUtils.isNoneBlank(capture.getAmount().getTotal())
            && NumberUtils.isNumber(capture.getAmount().getTotal());
    }
    

}
