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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.NameValuePair;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalItemRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalPaymentRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.details.PayPalDetailsRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalMethodType;

/**
 * @author Jeff Fischer
 */
public class PayPalRequestGeneratorImpl implements PayPalRequestGenerator {
    
    protected String user;
    protected String password;
    protected String signature;
    protected String libVersion;
    protected String returnUrl;
    protected String cancelUrl;
    protected Map<String, String> additionalConfig;
    
    @Override
    public List<NameValuePair> buildRequest(PayPalRequest request) {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        setBaseNvps(nvps);

        if (request.getMethodType() == PayPalMethodType.CHECKOUT) {
            setNvpsForCheckout(nvps, (PayPalPaymentRequest) request);
        } else if (request.getMethodType() == PayPalMethodType.PROCESS) {
            setNvpsForProcess(nvps, (PayPalPaymentRequest) request);
        } else if (request.getMethodType() == PayPalMethodType.REFUND) {
            setNvpsForRefund(nvps, (PayPalPaymentRequest) request);
        } else if (request.getMethodType() == PayPalMethodType.CAPTURE) {
            setNvpsForCapture(nvps, (PayPalPaymentRequest) request);
        } else if (request.getMethodType() == PayPalMethodType.VOID) {
            setNvpsForVoid(nvps, (PayPalPaymentRequest) request);
        } else if (request.getMethodType() == PayPalMethodType.DETAILS) {
            setNvpsForDetails(nvps, (PayPalDetailsRequest) request);
        } else {
            throw new IllegalArgumentException("Method type not supported: " + request.getMethodType().getFriendlyType());
        }
        
        return nvps;
    }

    protected void setNvpsForDetails(List<NameValuePair> nvps, PayPalDetailsRequest paymentRequest) {
        nvps.add(new NameValuePair(MessageConstants.TOKEN, paymentRequest.getToken()));
        nvps.add(new NameValuePair(MessageConstants.METHOD, MessageConstants.PAYMENTDETAILSACTION));
    }

    protected void setNvpsForVoid(List<NameValuePair> nvps, PayPalPaymentRequest paymentRequest) {
        nvps.add(new NameValuePair(MessageConstants.TRANSACTIONID, paymentRequest.getTransactionID()));

        for (Map.Entry<String, String> entry : getAdditionalConfig().entrySet()) {
            nvps.add(new NameValuePair(entry.getKey(), entry.getValue()));
        }
        nvps.add(new NameValuePair(MessageConstants.METHOD, MessageConstants.VOIDACTION));
    }

    protected void setNvpsForCapture(List<NameValuePair> nvps, PayPalPaymentRequest paymentRequest) {
        nvps.add(new NameValuePair(MessageConstants.TRANSACTIONID, paymentRequest.getTransactionID()));
        nvps.add(new NameValuePair(MessageConstants.GRANDTOTALREQUEST, paymentRequest.getSummaryRequest().getGrandTotal().toString()));
        nvps.add(new NameValuePair(MessageConstants.COMPLETETYPE, MessageConstants.CAPTURECOMPLETE));
        for (Map.Entry<String, String> entry : getAdditionalConfig().entrySet()) {
            nvps.add(new NameValuePair(entry.getKey(), entry.getValue()));
        }
        nvps.add(new NameValuePair(MessageConstants.METHOD, MessageConstants.CAPTUREACTION));
    }

    protected void setNvpsForRefund(List<NameValuePair> nvps, PayPalPaymentRequest paymentRequest) {
        nvps.add(new NameValuePair(MessageConstants.TRANSACTIONID, paymentRequest.getTransactionID()));

        for (Map.Entry<String, String> entry : getAdditionalConfig().entrySet()) {
            nvps.add(new NameValuePair(entry.getKey(), entry.getValue()));
        }
        nvps.add(new NameValuePair(MessageConstants.METHOD, MessageConstants.REFUNDACTION));
    }

    protected void setNvpsForProcess(List<NameValuePair> nvps, PayPalPaymentRequest paymentRequest) {
        nvps.add(new NameValuePair(MessageConstants.PAYMENTACTION, MessageConstants.SALEACTION));
        nvps.add(new NameValuePair(MessageConstants.TOKEN, paymentRequest.getToken()));
        nvps.add(new NameValuePair(MessageConstants.PAYERID, paymentRequest.getPayerID()));

        for (Map.Entry<String, String> entry : getAdditionalConfig().entrySet()) {
            nvps.add(new NameValuePair(entry.getKey(), entry.getValue()));
        }
        nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.GRANDTOTALREQUEST, new Integer[] {0}, new String[] {"n"}), paymentRequest.getSummaryRequest().getGrandTotal().toString()));
        nvps.add(new NameValuePair(MessageConstants.METHOD, MessageConstants.PROCESSPAYMENTACTION));
    }
    
    protected void setBaseNvps(List<NameValuePair> nvps) {
        nvps.add(new NameValuePair(MessageConstants.USER, user));
        nvps.add(new NameValuePair(MessageConstants.PASSWORD, password));
        nvps.add(new NameValuePair(MessageConstants.SIGNATURE, signature));
        nvps.add(new NameValuePair(MessageConstants.VERSION, libVersion));
    }
    
    protected void setNvpsForCheckout(List<NameValuePair> nvps, PayPalPaymentRequest paymentRequest) {
        nvps.add(new NameValuePair(MessageConstants.PAYMENTACTION, MessageConstants.SALEACTION));
        nvps.add(new NameValuePair(MessageConstants.INVNUM, paymentRequest.getReferenceNumber()));
        //do not display shipping address fields on paypal pages
        nvps.add(new NameValuePair(MessageConstants.NOSHIPPING, "1"));

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
            nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.NAMEREQUEST, new Integer[] {0, counter}, new String[] {"n", "m"}), itemRequest.getShortDescription()));
            nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.NUMBERREQUEST, new Integer[] {0, counter}, new String[] {"n", "m"}), itemRequest.getSystemId()));
            nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.DESCRIPTIONREQUEST, new Integer[] {0, counter}, new String[] {"n", "m"}), itemRequest.getDescription()));
            nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.AMOUNTREQUEST, new Integer[] {0, counter}, new String[] {"n", "m"}), itemRequest.getUnitPrice().toString()));
            nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.QUANTITYREQUEST, new Integer[] {0, counter}, new String[] {"n", "m"}), String.valueOf(itemRequest.getQuantity())));
            counter++;
        }
        nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.SUBTOTALREQUEST, new Integer[] {0}, new String[] {"n"}), paymentRequest.getSummaryRequest().getSubTotal().toString()));
        nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.TAXREQUEST, new Integer[] {0}, new String[] {"n"}), paymentRequest.getSummaryRequest().getTotalTax().toString()));
        nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.SHIPPINGREQUEST, new Integer[] {0}, new String[] {"n"}), paymentRequest.getSummaryRequest().getTotalShipping().toString()));
        nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.SHIPPINGDISCOUNTREQUEST, new Integer[] {0}, new String[] {"n"}), "-" + paymentRequest.getSummaryRequest().getShippingDiscount().toString()));
        nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.GRANDTOTALREQUEST, new Integer[] {0}, new String[] {"n"}), paymentRequest.getSummaryRequest().getGrandTotal().toString()));
    }
    
    protected String replaceNumericBoundProperty(String property, Integer[] number, String[] positions) {
        int counter = 0;
        for (String position : positions) {
            int pos = property.indexOf(position);
            if (pos < 0) {
                throw new IllegalArgumentException("Property does not contain the specified position value (" + position +")");
            }
            String newValue = String.valueOf(number[counter]);
            property = property.substring(0 , pos) + newValue + property.substring(pos + position.length(), property.length());
            counter++;
        }
        return property;
    }

    @Override
    public Map<String, String> getAdditionalConfig() {
        return additionalConfig;
    }

    @Override
    public void setAdditionalConfig(Map<String, String> additionalConfig) {
        this.additionalConfig = additionalConfig;
    }

    @Override
    public String getCancelUrl() {
        return cancelUrl;
    }

    @Override
    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }

    @Override
    public String getLibVersion() {
        return libVersion;
    }

    @Override
    public void setLibVersion(String libVersion) {
        this.libVersion = libVersion;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getReturnUrl() {
        return returnUrl;
    }

    @Override
    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    @Override
    public String getSignature() {
        return signature;
    }

    @Override
    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public void setUser(String user) {
        this.user = user;
    }
}
