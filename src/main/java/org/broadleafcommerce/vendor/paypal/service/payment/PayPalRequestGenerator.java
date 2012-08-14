/*
 * Copyright 2008-2009 the original author or authors.
 *
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
 */

package org.broadleafcommerce.vendor.paypal.service.payment;

import org.apache.commons.httpclient.NameValuePair;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalRequest;

import java.util.List;
import java.util.Map;

/**
 * @author Jeff Fischer
 */
public interface PayPalRequestGenerator {

    List<NameValuePair> buildRequest(PayPalRequest paymentRequest);

    Map<String, String> getAdditionalConfig();

    void setAdditionalConfig(Map<String, String> additionalConfig);

    String getCancelUrl();

    void setCancelUrl(String cancelUrl);

    String getLibVersion();

    void setLibVersion(String libVersion);

    String getPassword();

    void setPassword(String password);

    String getReturnUrl();

    void setReturnUrl(String returnUrl);

    String getSignature();

    void setSignature(String signature);

    String getUser();

    void setUser(String user);

    String getShippingDisplayType();

    void setShippingDisplayType(String shippingDisplayType);

}
