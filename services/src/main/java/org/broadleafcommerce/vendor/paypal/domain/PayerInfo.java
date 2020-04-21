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

public class PayerInfo extends PayPalModel {

    private String account_id;

    private String email_address;

    private Name payer_name;

    public String getAccount_id() {
        return this.account_id;
    }

    public PayerInfo setAccount_id(String account_id) {
        this.account_id = account_id;
        return this;
    }

    public String getEmail_address() {
        return this.email_address;
    }

    public PayerInfo setEmail_address(String email_address) {
        this.email_address = email_address;
        return this;
    }

    public Name getPayer_name() {
        return this.payer_name;
    }

    public PayerInfo setPayer_name(Name payer_name) {
        this.payer_name = payer_name;
        return this;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this)
            return true;
        if (!(o instanceof PayerInfo))
            return false;
        final PayerInfo other = (PayerInfo) o;
        if (!other.canEqual((java.lang.Object) this))
            return false;
        if (!super.equals(o))
            return false;
        final java.lang.Object this$account_id = this.getAccount_id();
        final java.lang.Object other$account_id = other.getAccount_id();
        if (this$account_id == null ? other$account_id != null
                : !this$account_id.equals(other$account_id))
            return false;
        final java.lang.Object this$email_address = this.getEmail_address();
        final java.lang.Object other$email_address = other.getEmail_address();
        if (this$email_address == null ? other$email_address != null
                : !this$email_address.equals(other$email_address))
            return false;
        final java.lang.Object this$payer_name = this.getPayer_name();
        final java.lang.Object other$payer_name = other.getPayer_name();
        if (this$payer_name == null ? other$payer_name != null
                : !this$payer_name.equals(other$payer_name))
            return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof PayerInfo;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + super.hashCode();
        final java.lang.Object $account_id = this.getAccount_id();
        result = result * PRIME + ($account_id == null ? 43 : $account_id.hashCode());
        final java.lang.Object $email_address = this.getEmail_address();
        result = result * PRIME + ($email_address == null ? 43 : $email_address.hashCode());
        final java.lang.Object $payer_name = this.getPayer_name();
        result = result * PRIME + ($payer_name == null ? 43 : $payer_name.hashCode());
        return result;
    }

}
