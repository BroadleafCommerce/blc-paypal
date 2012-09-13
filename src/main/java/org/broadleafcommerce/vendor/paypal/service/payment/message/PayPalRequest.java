/*
 * Copyright 2008-2012 the original author or authors.
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
