/*
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2014 Broadleaf Commerce
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
package org.broadleafcommerce.payment.service.gateway;

import org.broadleafcommerce.common.payment.PaymentGatewayType;
import org.broadleafcommerce.common.payment.service.AbstractPaymentGatewayConfiguration;
import org.broadleafcommerce.common.util.BLCSystemProperty;
import org.broadleafcommerce.common.web.BaseUrlResolver;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalExpressPaymentGatewayType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalShippingDisplayType;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalExpressConfiguration")
public class PayPalExpressConfigurationImpl extends AbstractPaymentGatewayConfiguration implements PayPalExpressConfiguration {

    @Resource(name = "blBaseUrlResolver")
    protected BaseUrlResolver urlResolver;

    protected int failureReportingThreshold = 1;

    protected boolean performAuthorizeAndCapture = true;

    @Override
    public String getServerUrl() {
        return BLCSystemProperty.resolveSystemProperty("gateway.paypal.expressCheckout.serverUrl");
    }

    @Override
    public String getUserRedirectUrl() {
        return BLCSystemProperty.resolveSystemProperty("gateway.paypal.expressCheckout.userRedirectUrl");
    }

    @Override
    public String getLibVersion() {
        return BLCSystemProperty.resolveSystemProperty("gateway.paypal.expressCheckout.libVersion");
    }

    @Override
    public String getPassword() {
        return BLCSystemProperty.resolveSystemProperty("gateway.paypal.expressCheckout.password");
    }

    @Override
    public String getUser() {
        return BLCSystemProperty.resolveSystemProperty("gateway.paypal.expressCheckout.user");
    }

    @Override
    public String getSignature() {
        return BLCSystemProperty.resolveSystemProperty("gateway.paypal.expressCheckout.signature");
    }

    @Override
    public String getReturnUrl() {
        String url = BLCSystemProperty.resolveSystemProperty("gateway.paypal.expressCheckout.returnUrl");
        try {
            URI u = new URI(url);
            if (u.isAbsolute()) {
                return url;
            } else {
                String baseUrl = urlResolver.getSiteBaseUrl();
                return baseUrl + url;
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("The value for 'gateway.paypal.expressCheckout.returnUrl' is not valid.", e);
        }
    }

    @Override
    public String getCancelUrl() {
        String url = BLCSystemProperty.resolveSystemProperty("gateway.paypal.expressCheckout.cancelUrl");
        try {
            URI u = new URI(url);
            if (u.isAbsolute()) {
                return url;
            } else {
                String baseUrl = urlResolver.getSiteBaseUrl();
                return baseUrl + url;
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("The value for 'gateway.paypal.expressCheckout.cancelUrl' is not valid.", e);
        }
    }

    @Override
    public PayPalShippingDisplayType getShippingDisplayType() {
        String shippingType = BLCSystemProperty.resolveSystemProperty("gateway.paypal.expressCheckout.shippingDisplayType");

        PayPalShippingDisplayType displayType = PayPalShippingDisplayType.getInstance(shippingType);
        if (displayType != null) {
            return displayType;
        }

        return PayPalShippingDisplayType.NO_DISPLAY;
    }

    @Override
    public String getTotalType() {
        return MessageConstants.TOTAL;
    }

    @Override
    public Map<String, String> getAdditionalConfig() {
        Map<String, String> additionalConfigs = new HashMap<String, String>();
        additionalConfigs.put("HDRBORDERCOLOR", "FFFFFF");
        additionalConfigs.put("HDRBACKCOLOR", "FFFFFF");
        additionalConfigs.put("PAYFLOWCOLOR", "FFFFFF");
        return additionalConfigs;
    }
    
    @Override
    public Map<String, String> getAdditionalCustomFields() {
        // intentionally unimplemented, used as an extension point
        return new HashMap<String, String>();
    }

    @Override
    public boolean handlesAuthorize() {
        return true;
    }

    @Override
    public boolean handlesCapture() {
        return true;
    }

    @Override
    public boolean handlesAuthorizeAndCapture() {
        return true;
    }

    @Override
    public boolean handlesReverseAuthorize() {
        return true;
    }

    @Override
    public boolean handlesVoid() {
        return true;
    }

    @Override
    public boolean handlesRefund() {
        return true;
    }

    @Override
    public boolean handlesPartialCapture() {
        return false;
    }

    @Override
    public boolean handlesMultipleShipment() {
        return false;
    }

    @Override
    public boolean handlesRecurringPayment() {
        return false;
    }

    @Override
    public boolean handlesSavedCustomerPayment() {
        return false;
    }

    @Override
    public boolean isPerformAuthorizeAndCapture() {
        return performAuthorizeAndCapture;
    }

    @Override
    public void setPerformAuthorizeAndCapture(boolean performAuthorizeAndCapture) {
        this.performAuthorizeAndCapture = performAuthorizeAndCapture;
    }

    @Override
    public int getFailureReportingThreshold() {
        return failureReportingThreshold;
    }

    @Override
    public void setFailureReportingThreshold(int failureReportingThreshold) {
        this.failureReportingThreshold = failureReportingThreshold;
    }

    @Override
    public boolean handlesMultiplePayments() {
        return false;
    }

    @Override
    public PaymentGatewayType getGatewayType() {
        return PayPalExpressPaymentGatewayType.PAYPAL_EXPRESS;
    }
}
