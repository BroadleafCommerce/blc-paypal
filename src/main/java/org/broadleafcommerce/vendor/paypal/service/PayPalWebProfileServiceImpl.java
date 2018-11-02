/*
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2018 Broadleaf Commerce
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
package org.broadleafcommerce.vendor.paypal.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.payment.service.gateway.ExternalCallPayPalCheckoutService;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreateWebProfileRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreateWebProfileResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;

import com.paypal.api.payments.WebProfile;
import com.paypal.base.rest.APIContext;

import javax.annotation.Resource;

@Service("blPayPalWebProfileService")
public class PayPalWebProfileServiceImpl implements PayPalWebProfileService {

    private static final Log LOG = LogFactory.getLog(PayPalWebProfileServiceImpl.class);
    
    protected WebProfile webProfile;

    protected String beanProfileId;
    
    @Resource(name = "blExternalCallPayPalCheckoutService")
    protected ExternalCallPayPalCheckoutService externalCallService;

    @Lookup("blPayPalApiContext")
    protected APIContext getApiContext() {
        return null;
    }
    
    @Autowired(required = false)
    public PayPalWebProfileServiceImpl(WebProfile webProfile) {
        super();
        this.webProfile = webProfile;
        if (webProfile != null && StringUtils.isBlank(webProfile.getId()) && LOG.isWarnEnabled()) {
            LOG.warn("The WebProfile provided did not specify an id. Beware that this will result in creating new WebProfiles linked to your PayPal account on every server startup."
                + "To avoid this either set an id on the provided WebProfile bean or set the property gateway.paypal.checkout.rest.webProfileId."
                + "To obtain a WebProfile id either create a WebProfile or select an existing an id from you PayPal account following the instructions here https://developer.paypal.com/docs/api/payment-experience/v1/#web-profiles");
        }
    }

    public PayPalWebProfileServiceImpl() {
        super();
    }

    @Override
    public String getWebProfileId() {
        String profileId = getPropertyWebProfileId();
        if (StringUtils.isNotBlank(profileId)) {
            return profileId;
        }
        return getBeanWebProfileId();
    }

    protected String getBeanWebProfileId() {
        if (StringUtils.isNotBlank(beanProfileId)) {
            return beanProfileId;
        }
        if (webProfile == null) {
            return null;
        }
        if (StringUtils.isNotBlank(webProfile.getId())) {
            return webProfile.getId();
        }
        WebProfile profile = createWebProfile(webProfile);
        if (profile != null) {
            beanProfileId = profile.getId();
        }
        return beanProfileId;
    }

    protected WebProfile createWebProfile(WebProfile profile) {
        try {
            PayPalCreateWebProfileResponse response = (PayPalCreateWebProfileResponse) externalCallService.call(new PayPalCreateWebProfileRequest(profile, getApiContext()));
            return response.getWebProfile();
        } catch (PaymentException e) {
            LOG.error("Error retrieving WebProfile from PayPal", e);
        }
        return null;
    }

    protected String getPropertyWebProfileId() {
        return externalCallService.getConfiguration().getWebProfileId();
    }
}
