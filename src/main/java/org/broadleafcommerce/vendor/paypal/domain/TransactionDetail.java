/*-
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2019 Broadleaf Commerce
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
package org.broadleafcommerce.vendor.paypal.domain;

import com.paypal.base.rest.PayPalModel;

public class TransactionDetail extends PayPalModel{

    private TransactionInfo transaction_info;

    public TransactionInfo getTransaction_info() {
        return this.transaction_info;
    }

    public TransactionDetail setTransaction_info(TransactionInfo transaction_info) {
        this.transaction_info = transaction_info;
        return this;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof TransactionDetail)) return false;
        final TransactionDetail other = (TransactionDetail) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (!super.equals(o)) return false;
        final java.lang.Object this$transaction_info = this.getTransaction_info();
        final java.lang.Object other$transaction_info = other.getTransaction_info();
        if (this$transaction_info == null ? other$transaction_info != null : !this$transaction_info.equals(other$transaction_info)) return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof TransactionDetail;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + super.hashCode();
        final java.lang.Object $transaction_info = this.getTransaction_info();
        result = result * PRIME + ($transaction_info == null ? 43 : $transaction_info.hashCode());
        return result;
    }



}
