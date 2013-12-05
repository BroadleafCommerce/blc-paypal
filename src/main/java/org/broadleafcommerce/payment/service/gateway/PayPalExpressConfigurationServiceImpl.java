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

import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalShippingDisplayType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalExpressConfigurationService")
public class PayPalExpressConfigurationServiceImpl implements PayPalExpressConfigurationService {

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

    @Value("${gateway.paypal.expressCheckout.completeCheckoutOnCallback}")
    protected String completeCheckoutOnCallback;

    @Override
    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public String getUserRedirectUrl() {
        return userRedirectUrl;
    }

    @Override
    public String getLibVersion() {
        return libVersion;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getSignature() {
        return signature;
    }

    @Override
    public Boolean getUseRelativeUrls() {
        Boolean useRelative = false;
        if ("true".equalsIgnoreCase(useRelativeUrls)){
            useRelative = true;
        }
        return useRelative;
    }

    @Override
    public String getReturnUrl() {
        return returnUrl;
    }

    @Override
    public String getCancelUrl() {
        return cancelUrl;
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
    public Boolean completeCheckoutOnCallback() {
        Boolean complete = false;
        if ("true".equalsIgnoreCase(completeCheckoutOnCallback)){
            complete = true;
        }
        return complete;
    }

    @Override
    public Boolean handlesAuthorize() {
        return true;
    }

    @Override
    public Boolean handlesCapture() {
        return true;
    }

    @Override
    public Boolean handlesAuthorizeAndCapture() {
        return true;
    }

    @Override
    public Boolean handlesReverseAuthorize() {
        return true;
    }

    @Override
    public Boolean handlesVoid() {
        return true;
    }

    @Override
    public Boolean handlesRefund() {
        return true;
    }

    @Override
    public Boolean handlesPartialCapture() {
        return false;
    }

    @Override
    public Boolean handlesMultipleShipment() {
        return false;
    }

    @Override
    public Boolean handlesTransactionConfirmation() {
        return true;
    }

    @Override
    public Boolean handlesRecurringPayment() {
        return false;
    }

    @Override
    public Boolean handlesSavedCustomerPayment() {
        return false;
    }

    @Override
    public Integer getFailureReportingThreshold() {
        return 1;
    }
}
