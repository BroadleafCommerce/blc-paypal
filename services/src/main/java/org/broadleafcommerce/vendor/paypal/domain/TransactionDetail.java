/*
 * Copyright (C) 2009 - 2020 Broadleaf Commerce
 *
 * Licensed under the Broadleaf End User License Agreement (EULA), Version 1.1 (the
 * "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt).
 *
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the
 * "Custom License") between you and Broadleaf Commerce. You may not use this file except in
 * compliance with the applicable license.
 *
 * NOTICE: All information contained herein is, and remains the property of Broadleaf Commerce, LLC
 * The intellectual and technical concepts contained herein are proprietary to Broadleaf Commerce,
 * LLC and may be covered by U.S. and Foreign Patents, patents in process, and are protected by
 * trade secret or copyright law. Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained from Broadleaf Commerce, LLC.
 */
package org.broadleafcommerce.vendor.paypal.domain;

import com.paypal.base.rest.PayPalModel;

public class TransactionDetail extends PayPalModel {

    private TransactionInfo transaction_info;

    private PayerInfo payer_info;

    public PayerInfo getPayer_info() {
        return this.payer_info;
    }

    public TransactionDetail setPayer_info(PayerInfo payer_info) {
        this.payer_info = payer_info;
        return this;
    }

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
        if (o == this)
            return true;
        if (!(o instanceof TransactionDetail))
            return false;
        final TransactionDetail other = (TransactionDetail) o;
        if (!other.canEqual((java.lang.Object) this))
            return false;
        if (!super.equals(o))
            return false;
        final java.lang.Object this$transaction_info = this.getTransaction_info();
        final java.lang.Object other$transaction_info = other.getTransaction_info();
        if (this$transaction_info == null ? other$transaction_info != null
                : !this$transaction_info.equals(other$transaction_info))
            return false;
        final java.lang.Object this$payer_info = this.getPayer_info();
        final java.lang.Object other$payer_info = other.getPayer_info();
        if (this$payer_info == null ? other$payer_info != null
                : !this$payer_info.equals(other$payer_info))
            return false;
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
        final java.lang.Object $payer_info = this.getPayer_info();
        result = result * PRIME + ($payer_info == null ? 43 : $payer_info.hashCode());
        return result;
    }



}
