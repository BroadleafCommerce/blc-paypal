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

package org.broadleafcommerce.vendor.paypal.service.payment.message;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jeff Fischer
 */
public class PayPalParamsRequest implements Serializable {
    
    private String returnUrl;
    private String cancelUrl;
    private Map<String, String> additionalParams = new HashMap<String, String>();

    public Map<String, String> getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(Map<String, String> additionalParams) {
        this.additionalParams = additionalParams;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PayPalParamsRequest that = (PayPalParamsRequest) o;

        if (additionalParams != null ? !additionalParams.equals(that.additionalParams) : that.additionalParams != null)
            return false;
        if (cancelUrl != null ? !cancelUrl.equals(that.cancelUrl) : that.cancelUrl != null) return false;
        if (returnUrl != null ? !returnUrl.equals(that.returnUrl) : that.returnUrl != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = returnUrl != null ? returnUrl.hashCode() : 0;
        result = 31 * result + (cancelUrl != null ? cancelUrl.hashCode() : 0);
        result = 31 * result + (additionalParams != null ? additionalParams.hashCode() : 0);
        return result;
    }
}
