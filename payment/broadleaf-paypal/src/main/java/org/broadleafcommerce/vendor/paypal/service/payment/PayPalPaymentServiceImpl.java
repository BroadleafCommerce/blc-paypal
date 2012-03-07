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

package org.broadleafcommerce.vendor.paypal.service.payment;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.common.vendor.service.type.ServiceStatusType;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalErrorResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalItemRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalPaymentRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalPaymentResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalMethodType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalTransactionType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author jfischer
 *
 */
public class PayPalPaymentServiceImpl implements PayPalPaymentService {
	
	private static final Log LOG = LogFactory.getLog(PayPalPaymentServiceImpl.class);

    protected String user;
    protected String password;
    protected String signature;
    protected String serverUrl;
    protected String libVersion;
    protected Integer failureReportingThreshold;
    protected Integer failureCount = 0;
    protected Boolean isUp = true;
    protected String userRedirectUrl;
    protected String returnUrl;
    protected String cancelUrl;
    protected Map<String, String> additionalConfig;
    protected String token;
    protected String transactionID;

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
    public PayPalPaymentResponse process(PayPalPaymentRequest paymentRequest) throws PaymentException {
        String response;
        try {
            response = communicateWithVendor(paymentRequest);
        } catch (Exception e) {
            incrementFailure();
            throw new PaymentException(e);
        }
        clearStatus();
        
        PayPalPaymentResponse payPalPaymentResponse = new PayPalPaymentResponse();
        payPalPaymentResponse.setTransactionType(paymentRequest.getTransactionType());
        payPalPaymentResponse.setMethodType(paymentRequest.getMethodType());
        buildResponse(response, payPalPaymentResponse);
        token = payPalPaymentResponse.getResponseToken();
        if (paymentRequest.getMethodType() == PayPalMethodType.CHECKOUT) {
            payPalPaymentResponse.setUserRedirectUrl(getUserRedirectUrl() + "?cmd=_express-checkout&token=" + token);
        } else if(paymentRequest.getMethodType() == PayPalMethodType.PROCESS) {
            payPalPaymentResponse.setUserRedirectUrl("/orders/viewOrderConfirmation.htm?orderNumber=" + paymentRequest.getOrderNumber());
        } else if(paymentRequest.getMethodType() == PayPalMethodType.DETAILS) {
            payPalPaymentResponse.setUserRedirectUrl("checkout/checkoutReview");
        } else  {
            payPalPaymentResponse.setUserRedirectUrl("");
        }
        //TODO handle the redirect urls for the other method types
        
        return payPalPaymentResponse;
    }
    
    protected void buildResponse(String rawResponse, PayPalPaymentResponse response) {
        response.setCorrelationId(getResponseValue(rawResponse, MessageConstants.CORRELATIONID));
        String ack = getResponseValue(rawResponse, MessageConstants.ACK);
        response.setAck(ack);
        if (ack.toLowerCase().equals(MessageConstants.SUCCESS)) {
            response.setErrorDetected(false);
            response.setSuccessful(true);
            response.setResponseToken(getResponseValue(rawResponse, MessageConstants.TOKEN));
        } else if (ack.toLowerCase().equals(MessageConstants.SUCCESSWITHWARNINGS)) {
            response.setSuccessful(true);
            response.setErrorDetected(true);
            response.setResponseToken(getResponseValue(rawResponse, MessageConstants.TOKEN));
        } else {
            response.setSuccessful(false);
            response.setErrorDetected(true);
        }
        if (response.isErrorDetected()) {
            boolean eof = false;
            int errorNumber = 0;
            while (!eof) {
                String errorCode = getResponseValue(rawResponse, MessageConstants.ERRORCODE + errorNumber);
                if (errorCode != null) {
                    PayPalErrorResponse errorResponse = new PayPalErrorResponse();
                    errorResponse.setErrorCode(errorCode);
                    errorResponse.setShortMessage(getResponseValue(rawResponse, MessageConstants.ERRORSHORTMESSAGE + errorNumber));
                    errorResponse.setLongMessage(getResponseValue(rawResponse, MessageConstants.ERRORLONGMESSAGE + errorNumber));
                    errorResponse.setSeverityCode(getResponseValue(rawResponse, MessageConstants.ERRORSEVERITYCODE + errorNumber));
                    response.getErrorResponses().add(errorResponse);
                } else {
                    eof = true;
                }
                errorNumber++;
            }

            errorNumber = 0;
            eof = false;
            while (!eof) {
                String passThroughErrorName = getResponseValue(rawResponse, MessageConstants.ERRORPASSTHROUGHNAME + errorNumber);
                if (passThroughErrorName != null) {
                    response.getPassThroughErrors().put(passThroughErrorName, getResponseValue(rawResponse, MessageConstants.ERRORPASSTHROUGHVALUE + errorNumber));
                } else {
                    eof = true;
                }
                errorNumber++;
            }
        }
    }

    protected String communicateWithVendor(PayPalPaymentRequest paymentRequest) throws IOException {
        //TODO incorporate different currency type
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(getServerUrl());
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        if (paymentRequest.getMethodType() == PayPalMethodType.CHECKOUT) {
            setNvpsForCheckout(nvps, paymentRequest);
        } else if (paymentRequest.getMethodType() == PayPalMethodType.PROCESS) {
            setNvpsForProcess(nvps, paymentRequest);
        } else if (paymentRequest.getMethodType() == PayPalMethodType.DETAILS) {
            setNvpsForDetails(nvps);
        } else if (paymentRequest.getMethodType() == PayPalMethodType.REFUND) {
            setNvpsForRefund(nvps);
        } else if (paymentRequest.getMethodType() == PayPalMethodType.CAPTURE) {
            setNvpsForCapture(nvps, paymentRequest);
        } else if (paymentRequest.getMethodType() == PayPalMethodType.VOID) {
            setNvpsForVoid(nvps);
        } else if (paymentRequest.getMethodType() == PayPalMethodType.REAUTHORIZATION) {
            setNvpsForReauthorization(nvps, paymentRequest);
        }
        //TODO implement the other methods (process and details)

        postMethod.setRequestBody(nvps.toArray(new NameValuePair[nvps.size()]));
        httpClient.executeMethod(postMethod);
        return postMethod.getResponseBodyAsString();
    }

    protected void setNvpsForCheckout(List<NameValuePair> nvps, PayPalPaymentRequest paymentRequest) {
        nvps.add(new NameValuePair(MessageConstants.USER, user));
        nvps.add(new NameValuePair(MessageConstants.PASSWORD, password));
        nvps.add(new NameValuePair(MessageConstants.SIGNATURE, signature));
        nvps.add(new NameValuePair(MessageConstants.VERSION, libVersion));
        if (paymentRequest.getTransactionType() == PayPalTransactionType.AUTHORIZEANDCAPTURE) {
            nvps.add(new NameValuePair(MessageConstants.PAYMENTACTION, MessageConstants.SALEACTION));
        } else if (paymentRequest.getTransactionType() == PayPalTransactionType.AUTHORIZE) {
            nvps.add(new NameValuePair(MessageConstants.PAYMENTACTION, MessageConstants.AUTHORIZATIONACTION));
        }
        setCostNvps(nvps, paymentRequest);

        nvps.add(new NameValuePair(MessageConstants.RETURNURL, getReturnUrl()));
        nvps.add(new NameValuePair(MessageConstants.CANCELURL, getCancelUrl()));
        for (Map.Entry<String, String> entry : getAdditionalConfig().entrySet()) {
            nvps.add(new NameValuePair(entry.getKey(), entry.getValue()));
        }
        nvps.add(new NameValuePair(MessageConstants.METHOD, MessageConstants.EXPRESSCHECKOUTACTION));
    }
    
    protected void setCostNvps(List<NameValuePair> nvps, PayPalPaymentRequest paymentRequest) {
        int counter = 0;
        for (PayPalItemRequest itemRequest : paymentRequest.getItemRequests()) {
            nvps.add(new NameValuePair(MessageConstants.NAMEREQUEST + counter, itemRequest.getShortDescription()));
            nvps.add(new NameValuePair(MessageConstants.NUMBERREQUEST + counter, itemRequest.getSystemId()));
            //nvps.add(new NameValuePair(MessageConstants.DESCRIPTIONREQUEST + counter, itemRequest.getDescription()));
            nvps.add(new NameValuePair(MessageConstants.AMOUNTREQUEST + counter, itemRequest.getUnitPrice().toString()));
            nvps.add(new NameValuePair(MessageConstants.QUANTITYREQUEST + counter, String.valueOf(itemRequest.getQuantity())));
            counter++;
        }
        nvps.add(new NameValuePair(MessageConstants.SUBTOTALREQUEST, paymentRequest.getSummaryRequest().getSubTotal().toString()));
        nvps.add(new NameValuePair(MessageConstants.TAXREQUEST, paymentRequest.getSummaryRequest().getTotalTax().toString()));
        nvps.add(new NameValuePair(MessageConstants.SHIPPINGREQUEST, paymentRequest.getSummaryRequest().getTotalShipping().toString()));
        nvps.add(new NameValuePair(MessageConstants.SHIPPINGDISCOUNTREQUEST, "-" + paymentRequest.getSummaryRequest().getShippingDiscount().toString()));
        nvps.add(new NameValuePair(MessageConstants.GRANDTOTALREQUEST, paymentRequest.getSummaryRequest().getGrandTotal().toString()));
    }

    private void setNvpsForProcess(List<NameValuePair> nvps, PayPalPaymentRequest paymentRequest) {

        nvps.add(new NameValuePair(MessageConstants.USER, user));
        nvps.add(new NameValuePair(MessageConstants.PASSWORD, password));
        nvps.add(new NameValuePair(MessageConstants.SIGNATURE, signature));
        nvps.add(new NameValuePair(MessageConstants.VERSION, libVersion));
        nvps.add(new NameValuePair(MessageConstants.PAYMENTACTION, MessageConstants.SALEACTION));
        nvps.add(new NameValuePair(MessageConstants.TOKEN, paymentRequest.getToken()));
        nvps.add(new NameValuePair(MessageConstants.PAYERID, paymentRequest.getPayerID()));

        for (Map.Entry<String, String> entry : getAdditionalConfig().entrySet()) {
            nvps.add(new NameValuePair(entry.getKey(), entry.getValue()));
        }
        nvps.add(new NameValuePair(MessageConstants.GRANDTOTALREQUEST, paymentRequest.getSummaryRequest().getGrandTotal().toString()));
        nvps.add(new NameValuePair(MessageConstants.METHOD, MessageConstants.PROCESSPAYMENTACTION));
    }

    private void setNvpsForDetails(List<NameValuePair> nvps) {

        nvps.add(new NameValuePair(MessageConstants.USER, user));
        nvps.add(new NameValuePair(MessageConstants.PASSWORD, password));
        nvps.add(new NameValuePair(MessageConstants.SIGNATURE, signature));
        nvps.add(new NameValuePair(MessageConstants.VERSION, libVersion));
        nvps.add(new NameValuePair(MessageConstants.TOKEN, token));

        for (Map.Entry<String, String> entry : getAdditionalConfig().entrySet()) {
            nvps.add(new NameValuePair(entry.getKey(), entry.getValue()));
        }
        nvps.add(new NameValuePair(MessageConstants.METHOD, MessageConstants.PAYMENTDETAILSACTION));
    }

    private void setNvpsForRefund(List<NameValuePair> nvps) {

        nvps.add(new NameValuePair(MessageConstants.TRANSACTIONID, transactionID));

        for (Map.Entry<String, String> entry : getAdditionalConfig().entrySet()) {
            nvps.add(new NameValuePair(entry.getKey(), entry.getValue()));
        }
        nvps.add(new NameValuePair(MessageConstants.METHOD, MessageConstants.REFUNDACTION));
    }

    //Only for authorization or order payment actions, not for sale payment actions
    private void setNvpsForCapture(List<NameValuePair> nvps, PayPalPaymentRequest paymentRequest) {

        nvps.add(new NameValuePair(MessageConstants.TRANSACTIONID, transactionID));
        nvps.add(new NameValuePair(MessageConstants.GRANDTOTALREQUEST, paymentRequest.getSummaryRequest().getGrandTotal().toString()));
        nvps.add(new NameValuePair(MessageConstants.COMPLETETYPE, MessageConstants.CAPTURECOMPLETE));
        for (Map.Entry<String, String> entry : getAdditionalConfig().entrySet()) {
            nvps.add(new NameValuePair(entry.getKey(), entry.getValue()));
        }
        nvps.add(new NameValuePair(MessageConstants.METHOD, MessageConstants.CAPTUREACTION));
    }

    //Only for authorization or order payment actions, not for sale payment actions
    private void setNvpsForVoid(List<NameValuePair> nvps) {

        nvps.add(new NameValuePair(MessageConstants.TRANSACTIONID, transactionID));

        for (Map.Entry<String, String> entry : getAdditionalConfig().entrySet()) {
            nvps.add(new NameValuePair(entry.getKey(), entry.getValue()));
        }
        nvps.add(new NameValuePair(MessageConstants.METHOD, MessageConstants.VOIDACTION));
    }

    //Only for authorization or order payment actions, not for sale payment actions
    private void setNvpsForReauthorization(List<NameValuePair> nvps, PayPalPaymentRequest paymentRequest) {

        nvps.add(new NameValuePair(MessageConstants.TRANSACTIONID, transactionID));
        nvps.add(new NameValuePair(MessageConstants.GRANDTOTALREQUEST, paymentRequest.getSummaryRequest().getGrandTotal().toString()));
        
        for (Map.Entry<String, String> entry : getAdditionalConfig().entrySet()) {
            nvps.add(new NameValuePair(entry.getKey(), entry.getValue()));
        }
        nvps.add(new NameValuePair(MessageConstants.METHOD, MessageConstants.REAUTHORIZATIONACTION));
    }


    protected String getResponseValue(String resp, String valueName) {
        int keyBegin = resp.indexOf(valueName);
        if (keyBegin >= 0) {
            int tokenBegin = keyBegin + valueName.length() + 1;
            int tokenEnd = resp.indexOf('&', tokenBegin);
            if (tokenEnd < 0) {
                tokenEnd = resp.length();
            }
            return resp.substring(tokenBegin, tokenEnd);
        }
        return null;
    }


    public Integer getFailureReportingThreshold() {
        return failureReportingThreshold;
    }

    public String getUser() {
        return user;
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

    public void setUser(String user) {
        this.user = user;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
    
    public String getServiceName() {
        return getClass().getName();
    }

    public String getLibVersion() {
        return libVersion;
    }

    public void setLibVersion(String libVersion) {
        this.libVersion = libVersion;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserRedirectUrl() {
        return userRedirectUrl;
    }

    public void setUserRedirectUrl(String userRedirectUrl) {
        this.userRedirectUrl = userRedirectUrl;
    }

    public Map<String, String> getAdditionalConfig() {
        return additionalConfig;
    }

    public void setAdditionalConfig(Map<String, String> additionalConfig) {
        this.additionalConfig = additionalConfig;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
}