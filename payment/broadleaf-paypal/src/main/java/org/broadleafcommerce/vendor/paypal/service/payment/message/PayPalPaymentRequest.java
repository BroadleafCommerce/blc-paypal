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

package org.broadleafcommerce.vendor.paypal.service.payment.message;

import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalMethodType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalTransactionType;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @author jfischer
 *
 */
public class PayPalPaymentRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	protected String currency;
    private String payerID;
    private String token;
	protected List<PayPalItemRequest> itemRequests = new AutoNumberMemberIdList();
	protected PayPalTransactionType transactionType;
    protected PayPalMethodType methodType;
    protected PayPalSummaryRequest summaryRequest;
    private String orderNumber;

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

    public PayPalMethodType getMethodType() {
        return methodType;
    }

    public void setMethodType(PayPalMethodType methodType) {
        this.methodType = methodType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PayPalPaymentRequest that = (PayPalPaymentRequest) o;

        if (currency != null ? !currency.equals(that.currency) : that.currency != null) return false;
        if (itemRequests != null ? !itemRequests.equals(that.itemRequests) : that.itemRequests != null) return false;
        if (methodType != null ? !methodType.equals(that.methodType) : that.methodType != null) return false;
        if (summaryRequest != null ? !summaryRequest.equals(that.summaryRequest) : that.summaryRequest != null)
            return false;
        if (transactionType != null ? !transactionType.equals(that.transactionType) : that.transactionType != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = currency != null ? currency.hashCode() : 0;
        result = 31 * result + (itemRequests != null ? itemRequests.hashCode() : 0);
        result = 31 * result + (transactionType != null ? transactionType.hashCode() : 0);
        result = 31 * result + (methodType != null ? methodType.hashCode() : 0);
        result = 31 * result + (summaryRequest != null ? summaryRequest.hashCode() : 0);
        return result;
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

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
}
