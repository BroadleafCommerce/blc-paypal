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

package org.broadleafcommerce.vendor.paypal.service.payment.message.payment;

import org.broadleafcommerce.common.money.Money;

/**
 * 
 * @author jfischer
 *
 */
public class PayPalSummaryRequest implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Money subTotal;
    private Money totalTax;
    private Money totalShipping;
    private Money grandTotal;

    public Money getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(Money grandTotal) {
        this.grandTotal = grandTotal;
    }

    public Money getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(Money subTotal) {
        this.subTotal = subTotal;
    }

    public Money getTotalShipping() {
        return totalShipping;
    }

    public void setTotalShipping(Money totalShipping) {
        this.totalShipping = totalShipping;
    }

    public Money getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(Money totalTax) {
        this.totalTax = totalTax;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PayPalSummaryRequest)) return false;

        PayPalSummaryRequest that = (PayPalSummaryRequest) o;

        if (grandTotal != null ? !grandTotal.equals(that.grandTotal) : that.grandTotal != null) return false;
        if (subTotal != null ? !subTotal.equals(that.subTotal) : that.subTotal != null) return false;
        if (totalShipping != null ? !totalShipping.equals(that.totalShipping) : that.totalShipping != null)
            return false;
        if (totalTax != null ? !totalTax.equals(that.totalTax) : that.totalTax != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = subTotal != null ? subTotal.hashCode() : 0;
        result = 31 * result + (totalTax != null ? totalTax.hashCode() : 0);
        result = 31 * result + (totalShipping != null ? totalShipping.hashCode() : 0);
        result = 31 * result + (grandTotal != null ? grandTotal.hashCode() : 0);
        return result;
    }
}
