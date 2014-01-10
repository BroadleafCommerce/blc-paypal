/*
 * Copyright 2008-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.payment.service.gateway;

import org.broadleafcommerce.common.config.service.SystemPropertiesService;
import org.broadleafcommerce.common.payment.PaymentGatewayType;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalExpressPaymentGatewayType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalShippingDisplayType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalExpressConfiguration")
public class PayPalExpressConfigurationImpl implements PayPalExpressConfiguration {

    @Resource(name = "blSystemPropertiesService")
    protected SystemPropertiesService systemPropertiesService;

    @Value("${gateway.paypal.expressCheckout.serverUrl}")
    protected String serverUrl;

    @Value("${gateway.paypal.expressCheckout.userRedirectUrl}")
    protected String userRedirectUrl;

    @Value("${gateway.paypal.expressCheckout.libVersion}")
    protected String libVersion;

    @Value("${gateway.paypal.expressCheckout.password}")
    protected String password;

    @Value("${gateway.paypal.expressCheckout.user}")
    protected String user;

    @Value("${gateway.paypal.expressCheckout.signature}")
    protected String signature;

    @Value("${gateway.paypal.expressCheckout.useRelativeUrls}")
    protected String useRelativeUrls;

    @Value("${gateway.paypal.expressCheckout.returnUrl}")
    protected String returnUrl;

    @Value("${gateway.paypal.expressCheckout.cancelUrl}")
    protected String cancelUrl;

    protected int failureReportingThreshold = 1;

    protected boolean performAuthorizeAndCapture = true;

    @Override
    public String getServerUrl() {
        return systemPropertiesService.resolveSystemProperty("gateway.paypal.expressCheckout.serverUrl", serverUrl);
    }

    @Override
    public String getUserRedirectUrl() {
        return systemPropertiesService.resolveSystemProperty("gateway.paypal.expressCheckout.userRedirectUrl", userRedirectUrl);
    }

    @Override
    public String getLibVersion() {
        return systemPropertiesService.resolveSystemProperty("gateway.paypal.expressCheckout.libVersion", libVersion);
    }

    @Override
    public String getPassword() {
        return systemPropertiesService.resolveSystemProperty("gateway.paypal.expressCheckout.password", password);
    }

    @Override
    public String getUser() {
        return systemPropertiesService.resolveSystemProperty("gateway.paypal.expressCheckout.user", user);
    }

    @Override
    public String getSignature() {
        return systemPropertiesService.resolveSystemProperty("gateway.paypal.expressCheckout.signature", signature);
    }

    @Override
    public Boolean getUseRelativeUrls() {
        Boolean useRelative = false;
        if ("true".equalsIgnoreCase(
                systemPropertiesService.resolveSystemProperty("gateway.paypal.expressCheckout.useRelativeUrls",
                        useRelativeUrls))){
            useRelative = true;
        }
        return useRelative;
    }

    @Override
    public String getReturnUrl() {
        return systemPropertiesService.resolveSystemProperty("gateway.paypal.expressCheckout.returnUrl", returnUrl);
    }

    @Override
    public String getCancelUrl() {
        return systemPropertiesService.resolveSystemProperty("gateway.paypal.expressCheckout.cancelUrl", cancelUrl);
    }

    @Override
    public PayPalShippingDisplayType getShippingDisplayType() {
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
