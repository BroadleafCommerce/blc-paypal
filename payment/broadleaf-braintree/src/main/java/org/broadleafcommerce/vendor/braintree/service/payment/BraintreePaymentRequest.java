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

package org.broadleafcommerce.vendor.braintree.service.payment;

import java.io.Serializable;
import java.util.List;

import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.vendor.braintree.service.payment.type.BraintreeMethodType;
import org.broadleafcommerce.vendor.braintree.service.payment.type.BraintreeRefundType;

/**
 *
 * @author jfischer
 *
 */
public class BraintreePaymentRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String currency;
    protected String payerID;
    protected String queryString;
    protected BraintreeMethodType methodType;
    protected String referenceNumber;
    protected String transactionID;
    protected BraintreeRefundType refundType;
    protected Money payAmount;

    public BraintreeMethodType getMethodType() {
        return methodType;
    }

    public void setMethodType(BraintreeMethodType methodType) {
        this.methodType = methodType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getPayerID() {
        return payerID;
    }

    public void setPayerID(String payerID) {
        this.payerID = payerID;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public BraintreeRefundType getRefundType() {
        return refundType;
    }

    public void setRefundType(BraintreeRefundType refundType) {
        this.refundType = refundType;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BraintreePaymentRequest)) return false;
        if (!super.equals(o)) return false;

        BraintreePaymentRequest request = (BraintreePaymentRequest) o;

        if (currency != null ? !currency.equals(request.currency) : request.currency != null) return false;
        if (payerID != null ? !payerID.equals(request.payerID) : request.payerID != null) return false;
        if (referenceNumber != null ? !referenceNumber.equals(request.referenceNumber) : request.referenceNumber != null)
            return false;
        if (refundType != null ? !refundType.equals(request.refundType) : request.refundType != null) return false;
        if (queryString != null ? !queryString.equals(request.queryString) : request.queryString != null) return false;
        if (transactionID != null ? !transactionID.equals(request.transactionID) : request.transactionID != null)
            return false;
        if (methodType != null ? !methodType.equals(request.methodType) : request.methodType != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (payerID != null ? payerID.hashCode() : 0);
        result = 31 * result + (queryString != null ? queryString.hashCode() : 0);
        result = 31 * result + (methodType != null ? methodType.hashCode() : 0);
        result = 31 * result + (referenceNumber != null ? referenceNumber.hashCode() : 0);
        result = 31 * result + (transactionID != null ? transactionID.hashCode() : 0);
        result = 31 * result + (refundType != null ? refundType.hashCode() : 0);
        return result;
    }

    public Money getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(Money payAmount) {
        this.payAmount = payAmount;
    }
}
