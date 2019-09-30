package org.broadleafcommerce.vendor.paypal.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.payment.service.gateway.PayPalExternalCallService;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreateWebProfileRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreateWebProfileResponse;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.paypal.api.payments.WebProfile;

public class DefaultPayPalWebProfileService implements PayPalWebProfileService {

    private static final Log LOG = LogFactory.getLog(DefaultPayPalWebProfileService.class);

    private final PayPalExternalCallService externalCallService;
    private final WebProfile webProfile;

    private String beanProfileId;

    public DefaultPayPalWebProfileService(PayPalExternalCallService externalCallService,
                                          WebProfile webProfile) {
        super();
        this.externalCallService = externalCallService;
        this.webProfile = webProfile;
        if (webProfile != null && StringUtils.isBlank(webProfile.getId()) && LOG.isWarnEnabled()) {
            LOG.warn(
                    "The WebProfile provided did not specify an id. Beware that this will result in creating new WebProfiles linked to your PayPal account on every server startup."
                            + "To avoid this either set an id on the provided WebProfile bean or set the property gateway.paypal.checkout.rest.webProfileId."
                            + "To obtain a WebProfile id either create a WebProfile or select an existing an id from you PayPal account following the instructions here https://developer.paypal.com/docs/api/payment-experience/v1/#web-profiles");
        }
    }

    @Override
    public String getWebProfileId(PaymentRequest paymentRequest) {
        String profileId = getPropertyWebProfileId();
        if (StringUtils.isNotBlank(profileId)) {
            return profileId;
        }
        return getBeanWebProfileId(paymentRequest);
    }

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

    protected WebProfile createWebProfile(WebProfile profile, PaymentRequest paymentRequest) {
        try {
            PayPalCreateWebProfileResponse response =
                    (PayPalCreateWebProfileResponse) externalCallService.call(
                            new PayPalCreateWebProfileRequest(profile,
                                    externalCallService.constructAPIContext(paymentRequest)));
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
