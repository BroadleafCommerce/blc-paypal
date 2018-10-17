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

import org.broadleafcommerce.common.config.service.SystemPropertiesService;
import org.broadleafcommerce.common.payment.PaymentGatewayType;
import org.broadleafcommerce.common.payment.service.AbstractPaymentGatewayConfiguration;
import org.broadleafcommerce.common.web.BaseUrlResolver;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCheckoutPaymentGatewayType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalShippingDisplayType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalCheckoutConfiguration")
public class PayPalCheckoutConfigurationImpl extends AbstractPaymentGatewayConfiguration implements PayPalCheckoutConfiguration {

    @Resource(name = "blBaseUrlResolver")
    protected BaseUrlResolver urlResolver;

    @Autowired
    protected SystemPropertiesService propertiesService;

    protected int failureReportingThreshold = 1;

    protected boolean performAuthorizeAndCapture = true;

    @Override
    public String getReturnUrl() {
        String url = propertiesService.resolveSystemProperty("gateway.paypal.checkout.rest.returnUrl");
        try {
            URI u = new URI(url);
            if (u.isAbsolute()) {
                return url;
            } else {
                String baseUrl = urlResolver.getSiteBaseUrl();
                return baseUrl + url;
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("The value for 'gateway.paypal.checkout.rest.returnUrl' is not valid.", e);
        }
    }

    @Override
    public String getCancelUrl() {
        String url = propertiesService.resolveSystemProperty("gateway.paypal.checkout.rest.cancelUrl");
        try {
            URI u = new URI(url);
            if (u.isAbsolute()) {
                return url;
            } else {
                String baseUrl = urlResolver.getSiteBaseUrl();
                return baseUrl + url;
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("The value for 'gateway.paypal.checkout.rest.cancelUrl' is not valid.", e);
        }
    }

    @Override
    public String getWebProfileId() {
        return propertiesService.resolveSystemProperty("gateway.paypal.checkout.rest.webProfileId");
    }

    @Override
    public String getPaymentDescription() {
        return propertiesService.resolveSystemProperty("gateway.paypal.checkout.rest.description");
    }

    @Override
    public String getSmartPaymentEnvironment() {
        return propertiesService.resolveSystemProperty("gateway.paypal.smart.button.env");
    }

    @Override
    public PayPalShippingDisplayType getShippingDisplayType() {
        String shippingType = propertiesService.resolveSystemProperty("gateway.paypal.expressCheckout.shippingDisplayType");

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
        Map<String, String> additionalConfigs = new HashMap<>();
        additionalConfigs.put("HDRBORDERCOLOR", "FFFFFF");
        additionalConfigs.put("HDRBACKCOLOR", "FFFFFF");
        additionalConfigs.put("PAYFLOWCOLOR", "FFFFFF");
        return additionalConfigs;
    }
    
    @Override
    public Map<String, String> getAdditionalCustomFields() {
        // intentionally unimplemented, used as an extension point
        return new HashMap<>();
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
        return PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT;
    }

}
