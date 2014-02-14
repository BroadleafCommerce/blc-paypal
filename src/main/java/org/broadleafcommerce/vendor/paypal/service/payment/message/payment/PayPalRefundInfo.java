/*
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2014 Broadleaf Commerce
 * %%
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
 * #L%
 */
package org.broadleafcommerce.vendor.paypal.service.payment.message.payment;

import java.io.Serializable;

import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalRefundPendingReasonType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalRefundStatusType;

/**
 * @author Jeff Fischer
 */
public class PayPalRefundInfo implements Serializable {
    
    protected String refundTransactionId;
    protected Money feeRefundAmount;
    protected Money grossRefundAmount;
    protected Money netRefundAmount;
    protected Money totalRefundAmount;
    protected String refundInfo;
    protected String currencyCode;
    protected PayPalRefundStatusType refundStatusType;
    protected PayPalRefundPendingReasonType pendingReasonType;

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Money getFeeRefundAmount() {
        return feeRefundAmount;
    }

    public void setFeeRefundAmount(Money feeRefundAmount) {
        this.feeRefundAmount = feeRefundAmount;
    }

    public Money getGrossRefundAmount() {
        return grossRefundAmount;
    }

    public void setGrossRefundAmount(Money grossRefundAmount) {
        this.grossRefundAmount = grossRefundAmount;
    }

    public Money getNetRefundAmount() {
        return netRefundAmount;
    }

    public void setNetRefundAmount(Money netRefundAmount) {
        this.netRefundAmount = netRefundAmount;
    }

    public PayPalRefundPendingReasonType getPendingReasonType() {
        return pendingReasonType;
    }

    public void setPendingReasonType(PayPalRefundPendingReasonType pendingReasonType) {
        this.pendingReasonType = pendingReasonType;
    }

    public String getRefundInfo() {
        return refundInfo;
    }

    public void setRefundInfo(String refundInfo) {
        this.refundInfo = refundInfo;
    }

    public PayPalRefundStatusType getRefundStatusType() {
        return refundStatusType;
    }

    public void setRefundStatusType(PayPalRefundStatusType refundStatusType) {
        this.refundStatusType = refundStatusType;
    }

    public String getRefundTransactionId() {
        return refundTransactionId;
    }

    public void setRefundTransactionId(String refundTransactionId) {
        this.refundTransactionId = refundTransactionId;
    }

    public Money getTotalRefundAmount() {
        return totalRefundAmount;
    }

    public void setTotalRefundAmount(Money totalRefundAmount) {
        this.totalRefundAmount = totalRefundAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PayPalRefundInfo)) return false;

        PayPalRefundInfo that = (PayPalRefundInfo) o;

        if (currencyCode != null ? !currencyCode.equals(that.currencyCode) : that.currencyCode != null) return false;
        if (feeRefundAmount != null ? !feeRefundAmount.equals(that.feeRefundAmount) : that.feeRefundAmount != null)
            return false;
        if (grossRefundAmount != null ? !grossRefundAmount.equals(that.grossRefundAmount) : that.grossRefundAmount != null)
            return false;
        if (netRefundAmount != null ? !netRefundAmount.equals(that.netRefundAmount) : that.netRefundAmount != null)
            return false;
        if (pendingReasonType != null ? !pendingReasonType.equals(that.pendingReasonType) : that.pendingReasonType != null)
            return false;
        if (refundInfo != null ? !refundInfo.equals(that.refundInfo) : that.refundInfo != null) return false;
        if (refundStatusType != null ? !refundStatusType.equals(that.refundStatusType) : that.refundStatusType != null)
            return false;
        if (refundTransactionId != null ? !refundTransactionId.equals(that.refundTransactionId) : that.refundTransactionId != null)
            return false;
        if (totalRefundAmount != null ? !totalRefundAmount.equals(that.totalRefundAmount) : that.totalRefundAmount != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = refundTransactionId != null ? refundTransactionId.hashCode() : 0;
        result = 31 * result + (feeRefundAmount != null ? feeRefundAmount.hashCode() : 0);
        result = 31 * result + (grossRefundAmount != null ? grossRefundAmount.hashCode() : 0);
        result = 31 * result + (netRefundAmount != null ? netRefundAmount.hashCode() : 0);
        result = 31 * result + (totalRefundAmount != null ? totalRefundAmount.hashCode() : 0);
        result = 31 * result + (refundInfo != null ? refundInfo.hashCode() : 0);
        result = 31 * result + (currencyCode != null ? currencyCode.hashCode() : 0);
        result = 31 * result + (refundStatusType != null ? refundStatusType.hashCode() : 0);
        result = 31 * result + (pendingReasonType != null ? pendingReasonType.hashCode() : 0);
        return result;
    }
}
