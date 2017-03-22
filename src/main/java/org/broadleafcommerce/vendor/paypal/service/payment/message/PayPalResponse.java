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
package org.broadleafcommerce.vendor.paypal.service.payment.message;

import java.io.Serializable;

/**
 * @author Jeff Fischer
 */
public abstract class PayPalResponse implements Serializable {

    protected String responseToken;
    protected String rawResponse;

    public String getResponseToken() {
        return responseToken;
    }

    public void setResponseToken(String responseToken) {
        this.responseToken = responseToken;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PayPalResponse)) return false;

        PayPalResponse that = (PayPalResponse) o;

        if (rawResponse != null ? !rawResponse.equals(that.rawResponse) : that.rawResponse != null)
            return false;
        if (responseToken != null ? !responseToken.equals(that.responseToken) : that.responseToken != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = rawResponse != null ? rawResponse.hashCode() : 0;
        result = 31 * result + (responseToken != null ? responseToken.hashCode() : 0);
        return result;
    }
}
