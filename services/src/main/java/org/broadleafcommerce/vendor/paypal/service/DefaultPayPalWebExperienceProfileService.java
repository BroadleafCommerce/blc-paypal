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
package org.broadleafcommerce.vendor.paypal.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutExternalCallService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutRestConfigurationProperties;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreateWebProfileRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreateWebProfileResponse;
import org.springframework.lang.Nullable;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.paypal.api.payments.WebProfile;

import lombok.AccessLevel;
import lombok.Getter;

public class DefaultPayPalWebExperienceProfileService implements PayPalWebExperienceProfileService {

    private static final Log LOG =
            LogFactory.getLog(DefaultPayPalWebExperienceProfileService.class);

    @Getter(AccessLevel.PROTECTED)
    private final PayPalCheckoutExternalCallService paypalCheckoutService;

    @Getter(AccessLevel.PROTECTED)
    private final PayPalCheckoutRestConfigurationProperties configProperties;

    @Getter(AccessLevel.PROTECTED)
    private final WebProfile webProfile;

    private String beanProfileId;

    public DefaultPayPalWebExperienceProfileService(
            PayPalCheckoutExternalCallService paypalCheckoutService,
            PayPalCheckoutRestConfigurationProperties configProperties,
            @Nullable WebProfile webProfile) {
        super();
        this.paypalCheckoutService = paypalCheckoutService;
        this.configProperties = configProperties;
        this.webProfile = webProfile;
        if (webProfile != null && StringUtils.isBlank(webProfile.getId()) && LOG.isWarnEnabled()) {
            LOG.warn(
                    "The WebProfile provided did not specify an id. Beware that this will result in creating new WebProfiles linked to your PayPal account on every server startup."
                            + "To avoid this either set an id on the provided WebProfile bean or set the property broadleaf.paypalcheckout.rest.webProfileId."
                            + "To obtain a WebProfile id either create a WebProfile or select an existing an id from you PayPal account following the instructions here https://developer.paypal.com/docs/api/payment-experience/v1/#web-profiles");
        }
    }

    @Override
    @Nullable
    public String getWebExperienceProfileId(PaymentRequest paymentRequest) {
        String profileId = getPropertyWebProfileId();
        if (StringUtils.isNotBlank(profileId)) {
            return profileId;
        }
        return getBeanWebProfileId(paymentRequest);
    }

    @Nullable
    protected String getBeanWebProfileId(PaymentRequest paymentRequest) {
        if (StringUtils.isNotBlank(beanProfileId)) {
            return beanProfileId;
        }
        if (webProfile == null) {
            return null;
        }
        if (StringUtils.isNotBlank(webProfile.getId())) {
            return webProfile.getId();
        }
        WebProfile profile = createWebProfile(webProfile, paymentRequest);
        if (profile != null) {
            beanProfileId = profile.getId();
        }
        return beanProfileId;
    }

    @Nullable
    protected WebProfile createWebProfile(WebProfile profile, PaymentRequest paymentRequest) {
        try {
            PayPalCreateWebProfileResponse response =
                    (PayPalCreateWebProfileResponse) paypalCheckoutService.call(
                            new PayPalCreateWebProfileRequest(profile,
                                    paypalCheckoutService.constructAPIContext(paymentRequest)));
            return response.getWebProfile();
        } catch (PaymentException e) {
            LOG.error("Error retrieving WebProfile from PayPal", e);
        }
        return null;
    }

    protected String getPropertyWebProfileId() {
        return configProperties.getWebProfileId();
    }
}
