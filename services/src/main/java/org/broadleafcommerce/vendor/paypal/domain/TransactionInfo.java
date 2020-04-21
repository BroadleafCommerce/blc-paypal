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

public class TransactionInfo extends PayPalModel {

    private String paypal_account_id;

    private String transaction_id;

    private String paypal_reference_id;

    private String paypal_reference_id_type;

    private String transaction_event_code;

    private String transaction_initiation_date;

    private String transaction_updated_date;

    private Money transaction_amount;

    private String transaction_status;

    private String transaction_subject;

    private String transaction_note;

    private String payment_tracking_id;

    private String custom_field;

    public String getPaypal_account_id() {
        return this.paypal_account_id;
    }

    public TransactionInfo setPaypal_account_id(String paypal_account_id) {
        this.paypal_account_id = paypal_account_id;
        return this;
    }

    public String getTransaction_id() {
        return this.transaction_id;
    }

    public TransactionInfo setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
        return this;
    }

    public String getPaypal_reference_id() {
        return this.paypal_reference_id;
    }

    public TransactionInfo setPaypal_reference_id(String paypal_reference_id) {
        this.paypal_reference_id = paypal_reference_id;
        return this;
    }

    public String getPaypal_reference_id_type() {
        return this.paypal_reference_id_type;
    }

    public TransactionInfo setPaypal_reference_id_type(String paypal_reference_id_type) {
        this.paypal_reference_id_type = paypal_reference_id_type;
        return this;
    }

    public String getTransaction_event_code() {
        return this.transaction_event_code;
    }

    public TransactionInfo setTransaction_event_code(String transaction_event_code) {
        this.transaction_event_code = transaction_event_code;
        return this;
    }

    public String getTransaction_initiation_date() {
        return this.transaction_initiation_date;
    }

    public TransactionInfo setTransaction_initiation_date(String transaction_initiation_date) {
        this.transaction_initiation_date = transaction_initiation_date;
        return this;
    }

    public String getTransaction_updated_date() {
        return this.transaction_updated_date;
    }

    public TransactionInfo setTransaction_updated_date(String transaction_updated_date) {
        this.transaction_updated_date = transaction_updated_date;
        return this;
    }

    public Money getTransaction_amount() {
        return this.transaction_amount;
    }

    public TransactionInfo setTransaction_amount(Money transaction_amount) {
        this.transaction_amount = transaction_amount;
        return this;
    }

    public String getTransaction_status() {
        return this.transaction_status;
    }

    public TransactionInfo setTransaction_status(String transaction_status) {
        this.transaction_status = transaction_status;
        return this;
    }

    public String getTransaction_subject() {
        return this.transaction_subject;
    }

    public TransactionInfo setTransaction_subject(String transaction_subject) {
        this.transaction_subject = transaction_subject;
        return this;
    }

    public String getTransaction_note() {
        return this.transaction_note;
    }

    public TransactionInfo setTransaction_note(String transaction_note) {
        this.transaction_note = transaction_note;
        return this;
    }

    public String getPayment_tracking_id() {
        return this.payment_tracking_id;
    }

    public TransactionInfo setPayment_tracking_id(String payment_tracking_id) {
        this.payment_tracking_id = payment_tracking_id;
        return this;
    }

    public String getCustom_field() {
        return this.custom_field;
    }

    public TransactionInfo setCustom_field(String custom_field) {
        this.custom_field = custom_field;
        return this;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this)
            return true;
        if (!(o instanceof TransactionInfo))
            return false;
        final TransactionInfo other = (TransactionInfo) o;
        if (!other.canEqual((java.lang.Object) this))
            return false;
        if (!super.equals(o))
            return false;
        final java.lang.Object this$paypal_account_id = this.getPaypal_account_id();
        final java.lang.Object other$paypal_account_id = other.getPaypal_account_id();
        if (this$paypal_account_id == null ? other$paypal_account_id != null
                : !this$paypal_account_id.equals(other$paypal_account_id))
            return false;
        final java.lang.Object this$transaction_id = this.getTransaction_id();
        final java.lang.Object other$transaction_id = other.getTransaction_id();
        if (this$transaction_id == null ? other$transaction_id != null
                : !this$transaction_id.equals(other$transaction_id))
            return false;
        final java.lang.Object this$paypal_reference_id = this.getPaypal_reference_id();
        final java.lang.Object other$paypal_reference_id = other.getPaypal_reference_id();
        if (this$paypal_reference_id == null ? other$paypal_reference_id != null
                : !this$paypal_reference_id.equals(other$paypal_reference_id))
            return false;
        final java.lang.Object this$paypal_reference_id_type = this.getPaypal_reference_id_type();
        final java.lang.Object other$paypal_reference_id_type = other.getPaypal_reference_id_type();
        if (this$paypal_reference_id_type == null ? other$paypal_reference_id_type != null
                : !this$paypal_reference_id_type.equals(other$paypal_reference_id_type))
            return false;
        final java.lang.Object this$transaction_event_code = this.getTransaction_event_code();
        final java.lang.Object other$transaction_event_code = other.getTransaction_event_code();
        if (this$transaction_event_code == null ? other$transaction_event_code != null
                : !this$transaction_event_code.equals(other$transaction_event_code))
            return false;
        final java.lang.Object this$transaction_initiation_date =
                this.getTransaction_initiation_date();
        final java.lang.Object other$transaction_initiation_date =
                other.getTransaction_initiation_date();
        if (this$transaction_initiation_date == null ? other$transaction_initiation_date != null
                : !this$transaction_initiation_date.equals(other$transaction_initiation_date))
            return false;
        final java.lang.Object this$transaction_updated_date = this.getTransaction_updated_date();
        final java.lang.Object other$transaction_updated_date = other.getTransaction_updated_date();
        if (this$transaction_updated_date == null ? other$transaction_updated_date != null
                : !this$transaction_updated_date.equals(other$transaction_updated_date))
            return false;
        final java.lang.Object this$transaction_amount = this.getTransaction_amount();
        final java.lang.Object other$transaction_amount = other.getTransaction_amount();
        if (this$transaction_amount == null ? other$transaction_amount != null
                : !this$transaction_amount.equals(other$transaction_amount))
            return false;
        final java.lang.Object this$transaction_status = this.getTransaction_status();
        final java.lang.Object other$transaction_status = other.getTransaction_status();
        if (this$transaction_status == null ? other$transaction_status != null
                : !this$transaction_status.equals(other$transaction_status))
            return false;
        final java.lang.Object this$transaction_subject = this.getTransaction_subject();
        final java.lang.Object other$transaction_subject = other.getTransaction_subject();
        if (this$transaction_subject == null ? other$transaction_subject != null
                : !this$transaction_subject.equals(other$transaction_subject))
            return false;
        final java.lang.Object this$transaction_note = this.getTransaction_note();
        final java.lang.Object other$transaction_note = other.getTransaction_note();
        if (this$transaction_note == null ? other$transaction_note != null
                : !this$transaction_note.equals(other$transaction_note))
            return false;
        final java.lang.Object this$payment_tracking_id = this.getPayment_tracking_id();
        final java.lang.Object other$payment_tracking_id = other.getPayment_tracking_id();
        if (this$payment_tracking_id == null ? other$payment_tracking_id != null
                : !this$payment_tracking_id.equals(other$payment_tracking_id))
            return false;
        final java.lang.Object this$custom_field = this.getCustom_field();
        final java.lang.Object other$custom_field = other.getCustom_field();
        if (this$custom_field == null ? other$custom_field != null
                : !this$custom_field.equals(other$custom_field))
            return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof TransactionInfo;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + super.hashCode();
        final java.lang.Object $paypal_account_id = this.getPaypal_account_id();
        result = result * PRIME + ($paypal_account_id == null ? 43 : $paypal_account_id.hashCode());
        final java.lang.Object $transaction_id = this.getTransaction_id();
        result = result * PRIME + ($transaction_id == null ? 43 : $transaction_id.hashCode());
        final java.lang.Object $paypal_reference_id = this.getPaypal_reference_id();
        result = result * PRIME
                + ($paypal_reference_id == null ? 43 : $paypal_reference_id.hashCode());
        final java.lang.Object $paypal_reference_id_type = this.getPaypal_reference_id_type();
        result = result * PRIME
                + ($paypal_reference_id_type == null ? 43 : $paypal_reference_id_type.hashCode());
        final java.lang.Object $transaction_event_code = this.getTransaction_event_code();
        result = result * PRIME
                + ($transaction_event_code == null ? 43 : $transaction_event_code.hashCode());
        final java.lang.Object $transaction_initiation_date = this.getTransaction_initiation_date();
        result = result * PRIME + ($transaction_initiation_date == null ? 43
                : $transaction_initiation_date.hashCode());
        final java.lang.Object $transaction_updated_date = this.getTransaction_updated_date();
        result = result * PRIME
                + ($transaction_updated_date == null ? 43 : $transaction_updated_date.hashCode());
        final java.lang.Object $transaction_amount = this.getTransaction_amount();
        result = result * PRIME
                + ($transaction_amount == null ? 43 : $transaction_amount.hashCode());
        final java.lang.Object $transaction_status = this.getTransaction_status();
        result = result * PRIME
                + ($transaction_status == null ? 43 : $transaction_status.hashCode());
        final java.lang.Object $transaction_subject = this.getTransaction_subject();
        result = result * PRIME
                + ($transaction_subject == null ? 43 : $transaction_subject.hashCode());
        final java.lang.Object $transaction_note = this.getTransaction_note();
        result = result * PRIME + ($transaction_note == null ? 43 : $transaction_note.hashCode());
        final java.lang.Object $payment_tracking_id = this.getPayment_tracking_id();
        result = result * PRIME
                + ($payment_tracking_id == null ? 43 : $payment_tracking_id.hashCode());
        final java.lang.Object $custom_field = this.getCustom_field();
        result = result * PRIME + ($custom_field == null ? 43 : $custom_field.hashCode());
        return result;
    }

}
