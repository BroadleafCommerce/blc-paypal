/*
 * Copyright 2008-2009 the original author or authors.
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
    protected String referenceNumber;
    protected String transactionId;
    protected String paymentMethod;
    protected String paymentRequestId;
    protected List<PayPalPaymentItemDetails> itemDetails = new ArrayList<PayPalPaymentItemDetails>();

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

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
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
        if (referenceNumber != null ? !referenceNumber.equals(that.referenceNumber) : that.referenceNumber != null)
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
        result = 31 * result + (referenceNumber != null ? referenceNumber.hashCode() : 0);
        result = 31 * result + (transactionId != null ? transactionId.hashCode() : 0);
        result = 31 * result + (paymentMethod != null ? paymentMethod.hashCode() : 0);
        result = 31 * result + (paymentRequestId != null ? paymentRequestId.hashCode() : 0);
        result = 31 * result + (itemDetails != null ? itemDetails.hashCode() : 0);
        return result;
    }
}
