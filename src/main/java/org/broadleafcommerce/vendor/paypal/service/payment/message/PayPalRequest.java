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

import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalMethodType;

/**
 * @author Jeff Fischer
 */
public abstract class PayPalRequest implements Serializable {

    protected PayPalMethodType secondaryMethodType;
    protected PayPalMethodType methodType;

    public PayPalMethodType getMethodType() {
        return methodType;
    }

    public void setMethodType(PayPalMethodType methodType) {
        this.methodType = methodType;
    }

    public PayPalMethodType getSecondaryMethodType() {
        return secondaryMethodType;
    }

    public void setSecondaryMethodType(PayPalMethodType secondaryMethodType) {
        this.secondaryMethodType = secondaryMethodType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PayPalRequest)) return false;

        PayPalRequest that = (PayPalRequest) o;

        if (methodType != null ? !methodType.equals(that.methodType) : that.methodType != null) return false;
        if (secondaryMethodType != null ? !secondaryMethodType.equals(that.secondaryMethodType) : that.secondaryMethodType != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = secondaryMethodType != null ? secondaryMethodType.hashCode() : 0;
        result = 31 * result + (methodType != null ? methodType.hashCode() : 0);
        return result;
    }
}
