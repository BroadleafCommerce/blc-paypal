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

package org.broadleafcommerce.vendor.paypal.service.payment.message.payment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalHoldDecisionType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalPaymentStatusType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalPaymentType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalPendingReasonType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalReasonCodeType;

/**
 * @author Jeff Fischer
 */
public class PayPalPaymentInfo implements Serializable {
    
    protected String transactionId;
    protected PayPalPaymentType paymentType;
    protected Date orderTime;
    protected Money totalAmount;
    protected String currencyCode;
    protected Money feeAmount;
    protected Money settleAmount;
    protected Money taxAmount;
    protected BigDecimal exchangeRate;
    protected PayPalPaymentStatusType paymentStatusType;
    protected PayPalPendingReasonType pendingReasonType;
    protected PayPalReasonCodeType reasonCodeType;
    protected PayPalHoldDecisionType holdDecisionType;
    protected String paymentRequestId;
    protected String parentTransactionId;
    protected String receiptId;

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public Money getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(Money feeAmount) {
        this.feeAmount = feeAmount;
    }

    public PayPalHoldDecisionType getHoldDecisionType() {
        return holdDecisionType;
    }

    public void setHoldDecisionType(PayPalHoldDecisionType holdDecisionType) {
        this.holdDecisionType = holdDecisionType;
    }

    public Date getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    public String getPaymentRequestId() {
        return paymentRequestId;
    }

    public void setPaymentRequestId(String paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
    }

    public PayPalPaymentStatusType getPaymentStatusType() {
        return paymentStatusType;
    }

    public void setPaymentStatusType(PayPalPaymentStatusType paymentStatusType) {
        this.paymentStatusType = paymentStatusType;
    }

    public PayPalPaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PayPalPaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public PayPalPendingReasonType getPendingReasonType() {
        return pendingReasonType;
    }

    public void setPendingReasonType(PayPalPendingReasonType pendingReasonType) {
        this.pendingReasonType = pendingReasonType;
    }

    public PayPalReasonCodeType getReasonCodeType() {
        return reasonCodeType;
    }

    public void setReasonCodeType(PayPalReasonCodeType reasonCodeType) {
        this.reasonCodeType = reasonCodeType;
    }

    public Money getSettleAmount() {
        return settleAmount;
    }

    public void setSettleAmount(Money settleAmount) {
        this.settleAmount = settleAmount;
    }

    public Money getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(Money taxAmount) {
        this.taxAmount = taxAmount;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Money totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getParentTransactionId() {
        return parentTransactionId;
    }

    public void setParentTransactionId(String parentTransactionId) {
        this.parentTransactionId = parentTransactionId;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PayPalPaymentInfo)) return false;

        PayPalPaymentInfo that = (PayPalPaymentInfo) o;

        if (currencyCode != null ? !currencyCode.equals(that.currencyCode) : that.currencyCode != null) return false;
        if (exchangeRate != null ? !exchangeRate.equals(that.exchangeRate) : that.exchangeRate != null) return false;
        if (feeAmount != null ? !feeAmount.equals(that.feeAmount) : that.feeAmount != null) return false;
        if (holdDecisionType != null ? !holdDecisionType.equals(that.holdDecisionType) : that.holdDecisionType != null)
            return false;
        if (orderTime != null ? !orderTime.equals(that.orderTime) : that.orderTime != null) return false;
        if (parentTransactionId != null ? !parentTransactionId.equals(that.parentTransactionId) : that.parentTransactionId != null)
            return false;
        if (paymentRequestId != null ? !paymentRequestId.equals(that.paymentRequestId) : that.paymentRequestId != null)
            return false;
        if (paymentStatusType != null ? !paymentStatusType.equals(that.paymentStatusType) : that.paymentStatusType != null)
            return false;
        if (paymentType != null ? !paymentType.equals(that.paymentType) : that.paymentType != null) return false;
        if (pendingReasonType != null ? !pendingReasonType.equals(that.pendingReasonType) : that.pendingReasonType != null)
            return false;
        if (reasonCodeType != null ? !reasonCodeType.equals(that.reasonCodeType) : that.reasonCodeType != null)
            return false;
        if (receiptId != null ? !receiptId.equals(that.receiptId) : that.receiptId != null) return false;
        if (settleAmount != null ? !settleAmount.equals(that.settleAmount) : that.settleAmount != null) return false;
        if (taxAmount != null ? !taxAmount.equals(that.taxAmount) : that.taxAmount != null) return false;
        if (totalAmount != null ? !totalAmount.equals(that.totalAmount) : that.totalAmount != null) return false;
        if (transactionId != null ? !transactionId.equals(that.transactionId) : that.transactionId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = transactionId != null ? transactionId.hashCode() : 0;
        result = 31 * result + (paymentType != null ? paymentType.hashCode() : 0);
        result = 31 * result + (orderTime != null ? orderTime.hashCode() : 0);
        result = 31 * result + (totalAmount != null ? totalAmount.hashCode() : 0);
        result = 31 * result + (currencyCode != null ? currencyCode.hashCode() : 0);
        result = 31 * result + (feeAmount != null ? feeAmount.hashCode() : 0);
        result = 31 * result + (settleAmount != null ? settleAmount.hashCode() : 0);
        result = 31 * result + (taxAmount != null ? taxAmount.hashCode() : 0);
        result = 31 * result + (exchangeRate != null ? exchangeRate.hashCode() : 0);
        result = 31 * result + (paymentStatusType != null ? paymentStatusType.hashCode() : 0);
        result = 31 * result + (pendingReasonType != null ? pendingReasonType.hashCode() : 0);
        result = 31 * result + (reasonCodeType != null ? reasonCodeType.hashCode() : 0);
        result = 31 * result + (holdDecisionType != null ? holdDecisionType.hashCode() : 0);
        result = 31 * result + (paymentRequestId != null ? paymentRequestId.hashCode() : 0);
        result = 31 * result + (parentTransactionId != null ? parentTransactionId.hashCode() : 0);
        result = 31 * result + (receiptId != null ? receiptId.hashCode() : 0);
        return result;
    }
}
