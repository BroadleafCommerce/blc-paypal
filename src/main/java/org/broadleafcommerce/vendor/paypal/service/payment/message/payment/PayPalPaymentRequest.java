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

import java.util.ArrayList;
import java.util.List;

import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalRefundType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalTransactionType;

/**
 * 
 * @author jfischer
 *
 */
public class PayPalPaymentRequest extends PayPalRequest {

    private static final long serialVersionUID = 1L;

    protected String orderId;
    protected String currency;
    protected String payerID;
    protected String token;
    protected List<PayPalItemRequest> itemRequests = new AutoNumberMemberIdList();
    protected List<PayPalShippingRequest> shippingRequests = new ArrayList<PayPalShippingRequest>();
    protected PayPalTransactionType transactionType;
    protected PayPalSummaryRequest summaryRequest;
    protected String transactionID;
    protected PayPalRefundType refundType;
    protected boolean completeCheckoutOnCallback = true;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public PayPalTransactionType getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(PayPalTransactionType transactionType) {
        this.transactionType = transactionType;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public List<PayPalItemRequest> getItemRequests() {
        return itemRequests;
    }
    
    public void setItemRequests(List<PayPalItemRequest> itemRequests) {
        this.itemRequests = itemRequests;
    }

    public PayPalSummaryRequest getSummaryRequest() {
        return summaryRequest;
    }

    public void setSummaryRequest(PayPalSummaryRequest summaryRequest) {
        this.summaryRequest = summaryRequest;
    }

    public String getPayerID() {
        return payerID;
    }

    public void setPayerID(String payerID) {
        this.payerID = payerID;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public PayPalRefundType getRefundType() {
        return refundType;
    }

    public void setRefundType(PayPalRefundType refundType) {
        this.refundType = refundType;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public List<PayPalShippingRequest> getShippingRequests() {
        return shippingRequests;
    }

    public void setShippingRequests(List<PayPalShippingRequest> shippingRequests) {
        this.shippingRequests = shippingRequests;
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
        if (!(o instanceof PayPalPaymentRequest)) return false;
        if (!super.equals(o)) return false;

        PayPalPaymentRequest request = (PayPalPaymentRequest) o;

        if (currency != null ? !currency.equals(request.currency) : request.currency != null) return false;
        if (itemRequests != null ? !itemRequests.equals(request.itemRequests) : request.itemRequests != null)
            return false;
        if (shippingRequests != null ? !shippingRequests.equals(request.shippingRequests) : request.shippingRequests != null)
            return false;
        if (payerID != null ? !payerID.equals(request.payerID) : request.payerID != null) return false;
        if (orderId != null ? !orderId.equals(request.orderId) : request.orderId != null)
            return false;
        if (refundType != null ? !refundType.equals(request.refundType) : request.refundType != null) return false;
        if (summaryRequest != null ? !summaryRequest.equals(request.summaryRequest) : request.summaryRequest != null)
            return false;
        if (token != null ? !token.equals(request.token) : request.token != null) return false;
        if (transactionID != null ? !transactionID.equals(request.transactionID) : request.transactionID != null)
            return false;
        if (transactionType != null ? !transactionType.equals(request.transactionType) : request.transactionType != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (payerID != null ? payerID.hashCode() : 0);
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (itemRequests != null ? itemRequests.hashCode() : 0);
        result = 31 * result + (shippingRequests != null ? shippingRequests.hashCode() : 0);
        result = 31 * result + (transactionType != null ? transactionType.hashCode() : 0);
        result = 31 * result + (summaryRequest != null ? summaryRequest.hashCode() : 0);
        result = 31 * result + (orderId != null ? orderId.hashCode() : 0);
        result = 31 * result + (transactionID != null ? transactionID.hashCode() : 0);
        result = 31 * result + (refundType != null ? refundType.hashCode() : 0);
        return result;
    }
}
