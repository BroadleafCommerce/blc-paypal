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
