/*
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2014 Broadleaf Commerce
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.broadleafcommerce.payment.service.gateway;

import org.broadleafcommerce.common.payment.PaymentGatewayType;
import org.broadleafcommerce.common.util.BLCSystemProperty;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalExpressPaymentGatewayType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalShippingDisplayType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalExpressConfiguration")
public class PayPalExpressConfigurationImpl implements PayPalExpressConfiguration {

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
    public Boolean getUseRelativeUrls() {
        return BLCSystemProperty.resolveBooleanSystemProperty("gateway.paypal.expressCheckout.useRelativeUrls");
    }

    @Override
    public String getReturnUrl() {
        return BLCSystemProperty.resolveSystemProperty("gateway.paypal.expressCheckout.returnUrl");
    }

    @Override
    public String getCancelUrl() {
        return BLCSystemProperty.resolveSystemProperty("gateway.paypal.expressCheckout.cancelUrl");
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
