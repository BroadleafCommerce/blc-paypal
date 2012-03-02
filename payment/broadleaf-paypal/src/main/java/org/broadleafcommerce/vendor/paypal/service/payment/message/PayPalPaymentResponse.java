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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadleafcommerce.common.vendor.service.message.PaymentResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalTransactionType;

/**
 * 
 * @author jfischer
 *
 */
public class PayPalPaymentResponse implements PaymentResponse {
		
	private static final long serialVersionUID = 1L;
	
    private boolean isErrorDetected = false;
    private String errorText;
    private PayPalTransactionType transactionType;
    private List<PayPalErrorResponse> errorResponses = new ArrayList<PayPalErrorResponse>();
    private Map<String, String> passThroughErrors = new HashMap<String, String>();

    public PayPalTransactionType getTransactionType() {
		return transactionType;
	}
	
	public void setTransactionType(PayPalTransactionType transactionType) {
		this.transactionType = transactionType;
	}
	
	public String getErrorCode() {
		throw new RuntimeException("ErrorCode not supported");
	}

	public String getErrorText() {
		return errorText;
	}

	public boolean isErrorDetected() {
		return isErrorDetected;
	}

	public void setErrorCode(String errorCode) {
		throw new RuntimeException("ErrorCode not supported");
	}

	public void setErrorDetected(boolean isErrorDetected) {
		this.isErrorDetected = isErrorDetected;
	}

	public void setErrorText(String errorText) {
		this.errorText = errorText;
	}

    public List<PayPalErrorResponse> getErrorResponses() {
        return errorResponses;
    }

    public void setErrorResponses(List<PayPalErrorResponse> errorResponses) {
        this.errorResponses = errorResponses;
    }

    public Map<String, String> getPassThroughErrors() {
        return passThroughErrors;
    }

    public void setPassThroughErrors(Map<String, String> passThroughErrors) {
        this.passThroughErrors = passThroughErrors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PayPalPaymentResponse that = (PayPalPaymentResponse) o;

        if (isErrorDetected != that.isErrorDetected) return false;
        if (errorResponses != null ? !errorResponses.equals(that.errorResponses) : that.errorResponses != null)
            return false;
        if (errorText != null ? !errorText.equals(that.errorText) : that.errorText != null) return false;
        if (passThroughErrors != null ? !passThroughErrors.equals(that.passThroughErrors) : that.passThroughErrors != null)
            return false;
        if (transactionType != null ? !transactionType.equals(that.transactionType) : that.transactionType != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (isErrorDetected ? 1 : 0);
        result = 31 * result + (errorText != null ? errorText.hashCode() : 0);
        result = 31 * result + (transactionType != null ? transactionType.hashCode() : 0);
        result = 31 * result + (errorResponses != null ? errorResponses.hashCode() : 0);
        result = 31 * result + (passThroughErrors != null ? passThroughErrors.hashCode() : 0);
        return result;
    }
}
