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

import java.io.Serializable;
import java.util.List;

import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalTransactionType;

/**
 * 
 * @author jfischer
 *
 */
public abstract class PayPalPaymentRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	protected PayPalBillingRequest billingRequest;
	protected String currency;
	protected List<PayPalItemRequest> itemRequests = new AutoNumberMemberIdList();
	protected PayPalTransactionType transactionType;
	protected Money grandTotal;
	protected Boolean useGrandTotal;

	public PayPalTransactionType getTransactionType() {
		return transactionType;
	}
	
	public void setTransactionType(PayPalTransactionType transactionType) {
		this.transactionType = transactionType;
	}
	
	public void setBillingRequest(PayPalBillingRequest billingRequest) {
		this.billingRequest = billingRequest;
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

	public Money getGrandTotal() {
		return grandTotal;
	}

	public void setGrandTotal(Money grandTotal) {
		this.grandTotal = grandTotal;
	}

	public Boolean getUseGrandTotal() {
		return useGrandTotal;
	}

	public void setUseGrandTotal(Boolean useGrandTotal) {
		this.useGrandTotal = useGrandTotal;
	}
	
}
