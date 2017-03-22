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
package org.broadleafcommerce.vendor.paypal.service.payment.message.details;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.broadleafcommerce.common.money.Money;

/**
 * @author Jeff Fischer
 */
public class PayPalPaymentDetails implements Serializable {

    protected Money amount;
    protected String currencyCode;
    protected Money itemTotal;
    protected Money shippingTotal;
    protected Money shippingDiscount;
    protected Money totalTax;
    protected String orderId;
    protected String transactionId;
    protected String paymentMethod;
    protected String paymentRequestId;
    protected List<PayPalPaymentItemDetails> itemDetails = new ArrayList<PayPalPaymentItemDetails>();
    protected boolean completeCheckoutOnCallback = true;

    public Money getAmount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public List<PayPalPaymentItemDetails> getItemDetails() {
        return itemDetails;
    }

    public void setItemDetails(List<PayPalPaymentItemDetails> itemDetails) {
        this.itemDetails = itemDetails;
    }

    public Money getItemTotal() {
        return itemTotal;
    }

    public void setItemTotal(Money itemTotal) {
        this.itemTotal = itemTotal;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentRequestId() {
        return paymentRequestId;
    }

    public void setPaymentRequestId(String paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Money getShippingDiscount() {
        return shippingDiscount;
    }

    public void setShippingDiscount(Money shippingDiscount) {
        this.shippingDiscount = shippingDiscount;
    }

    public Money getShippingTotal() {
        return shippingTotal;
    }

    public void setShippingTotal(Money shippingTotal) {
        this.shippingTotal = shippingTotal;
    }

    public Money getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(Money totalTax) {
        this.totalTax = totalTax;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public boolean isCompleteCheckoutOnCallback() {
        return completeCheckoutOnCallback;
    }

    public void setCompleteCheckoutOnCallback(boolean completeCheckoutOnCallback) {
        this.completeCheckoutOnCallback = completeCheckoutOnCallback;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PayPalPaymentDetails)) return false;

        PayPalPaymentDetails that = (PayPalPaymentDetails) o;

        if (amount != null ? !amount.equals(that.amount) : that.amount != null) return false;
        if (currencyCode != null ? !currencyCode.equals(that.currencyCode) : that.currencyCode != null) return false;
        if (itemDetails != null ? !itemDetails.equals(that.itemDetails) : that.itemDetails != null) return false;
        if (itemTotal != null ? !itemTotal.equals(that.itemTotal) : that.itemTotal != null) return false;
        if (paymentMethod != null ? !paymentMethod.equals(that.paymentMethod) : that.paymentMethod != null)
            return false;
        if (paymentRequestId != null ? !paymentRequestId.equals(that.paymentRequestId) : that.paymentRequestId != null)
            return false;
        if (orderId != null ? !orderId.equals(that.orderId) : that.orderId != null)
            return false;
        if (shippingDiscount != null ? !shippingDiscount.equals(that.shippingDiscount) : that.shippingDiscount != null)
            return false;
        if (shippingTotal != null ? !shippingTotal.equals(that.shippingTotal) : that.shippingTotal != null)
            return false;
        if (totalTax != null ? !totalTax.equals(that.totalTax) : that.totalTax != null) return false;
        if (transactionId != null ? !transactionId.equals(that.transactionId) : that.transactionId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = amount != null ? amount.hashCode() : 0;
        result = 31 * result + (currencyCode != null ? currencyCode.hashCode() : 0);
        result = 31 * result + (itemTotal != null ? itemTotal.hashCode() : 0);
        result = 31 * result + (shippingTotal != null ? shippingTotal.hashCode() : 0);
        result = 31 * result + (shippingDiscount != null ? shippingDiscount.hashCode() : 0);
        result = 31 * result + (totalTax != null ? totalTax.hashCode() : 0);
        result = 31 * result + (orderId != null ? orderId.hashCode() : 0);
        result = 31 * result + (transactionId != null ? transactionId.hashCode() : 0);
        result = 31 * result + (paymentMethod != null ? paymentMethod.hashCode() : 0);
        result = 31 * result + (paymentRequestId != null ? paymentRequestId.hashCode() : 0);
        result = 31 * result + (itemDetails != null ? itemDetails.hashCode() : 0);
        return result;
    }
}
