/*
 * Copyright 2008-2012 the original author or authors.
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

package org.broadleafcommerce.vendor.paypal.service.payment;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.common.vendor.service.monitor.ServiceStatusDetectable;
import org.broadleafcommerce.common.vendor.service.type.ServiceStatusType;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalResponse;

import java.io.IOException;
import java.util.List;

/**
 * 
 * @author jfischer
 *
 */
public class PayPalPaymentServiceImpl implements PayPalPaymentService, ServiceStatusDetectable<PayPalRequest> {
    
    private static final Log LOG = LogFactory.getLog(PayPalPaymentServiceImpl.class);

    protected String serverUrl;
    protected Integer failureReportingThreshold;
    protected Integer failureCount = 0;
    protected Boolean isUp = true;
    protected PayPalRequestGenerator requestGenerator;
    protected PayPalResponseGenerator responseGenerator;

    protected synchronized void clearStatus() {
        isUp = true;
        failureCount = 0;
    }

    protected synchronized void incrementFailure() {
        if (failureCount >= failureReportingThreshold) {
            isUp = false;
        } else {
            failureCount++;
        }
    }

    @Override
    public PayPalResponse process(PayPalRequest paymentRequest) throws PaymentException {
        String response;
        try {
            response = communicateWithVendor(paymentRequest);
        } catch (Exception e) {
            incrementFailure();
            throw new PaymentException(e);
        }
        clearStatus();
        
        return responseGenerator.buildResponse(response, paymentRequest);
    }

    protected String communicateWithVendor(PayPalRequest paymentRequest) throws IOException {
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(getServerUrl());
        List<NameValuePair> nvps = requestGenerator.buildRequest(paymentRequest);
        postMethod.setRequestBody(nvps.toArray(new NameValuePair[nvps.size()]));
        httpClient.executeMethod(postMethod);
        return postMethod.getResponseBodyAsString();
    }

    public Integer getFailureReportingThreshold() {
        return failureReportingThreshold;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public synchronized ServiceStatusType getServiceStatus() {
        if (isUp) {
            return ServiceStatusType.UP;
        } else {
            return ServiceStatusType.DOWN;
        }
    }

    public void setFailureReportingThreshold(Integer failureReportingThreshold) {
        this.failureReportingThreshold = failureReportingThreshold;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
    
    public String getServiceName() {
        return getClass().getName();
    }

    public PayPalRequestGenerator getRequestGenerator() {
        return requestGenerator;
    }

    public void setRequestGenerator(PayPalRequestGenerator requestGenerator) {
        this.requestGenerator = requestGenerator;
    }

    public PayPalResponseGenerator getResponseGenerator() {
        return responseGenerator;
    }

    public void setResponseGenerator(PayPalResponseGenerator responseGenerator) {
        this.responseGenerator = responseGenerator;
    }
}
