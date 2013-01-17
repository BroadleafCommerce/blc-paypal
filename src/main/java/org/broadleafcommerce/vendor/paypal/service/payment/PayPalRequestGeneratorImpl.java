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

import org.apache.commons.httpclient.NameValuePair;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.web.BroadleafRequestContext;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.details.PayPalDetailsRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalItemRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalPaymentRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalShippingRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalMethodType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalRefundType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalShippingDisplayType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Jeff Fischer
 */
public class PayPalRequestGeneratorImpl implements PayPalRequestGenerator {
    
    protected String user;
    protected String password;
    protected String signature;
    protected String libVersion;
    protected Boolean useRelativeUrls;
    protected String returnUrl;
    protected String cancelUrl;
    protected PayPalShippingDisplayType shippingDisplayType;
    protected Map<String, String> additionalConfig;
    
    @Override
    public List<NameValuePair> buildRequest(PayPalRequest request) {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        setBaseNvps(nvps);

        if (PayPalMethodType.CHECKOUT.equals(request.getMethodType())) {
            setNvpsForCheckoutOrAuth(nvps, (PayPalPaymentRequest) request, MessageConstants.SALEACTION);
            nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTCURRENCYCODE, new Integer[]{0}, new String[]{"n"}), ((PayPalPaymentRequest) request).getCurrency()));
        } else if (PayPalMethodType.AUTHORIZATION.equals(request.getMethodType())) {
            setNvpsForCheckoutOrAuth(nvps, (PayPalPaymentRequest) request, MessageConstants.AUTHORIZATIONACTION);
            nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTCURRENCYCODE, new Integer[]{0}, new String[]{"n"}), ((PayPalPaymentRequest) request).getCurrency()));
        } else if (PayPalMethodType.PROCESS.equals(request.getMethodType())) {
            setNvpsForProcess(nvps, (PayPalPaymentRequest) request);
            nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTCURRENCYCODE, new Integer[]{0}, new String[]{"n"}), ((PayPalPaymentRequest) request).getCurrency()));
        } else if (PayPalMethodType.REFUND.equals(request.getMethodType())) {
            setNvpsForRefund(nvps, (PayPalPaymentRequest) request);
            nvps.add(new NameValuePair(MessageConstants.CURRENCYCODE, ((PayPalPaymentRequest) request).getCurrency()));
        } else if (PayPalMethodType.CAPTURE.equals(request.getMethodType())) {
            setNvpsForCapture(nvps, (PayPalPaymentRequest) request);
            nvps.add(new NameValuePair(MessageConstants.CURRENCYCODE, ((PayPalPaymentRequest) request).getCurrency()));
        } else if (PayPalMethodType.VOID.equals(request.getMethodType())) {
            setNvpsForVoid(nvps, (PayPalPaymentRequest) request);
        } else if (PayPalMethodType.DETAILS.equals(request.getMethodType())) {
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
        nvps.add(new NameValuePair(MessageConstants.AUTHORIZATONID, paymentRequest.getTransactionID()));

        for (Map.Entry<String, String> entry : getAdditionalConfig().entrySet()) {
            nvps.add(new NameValuePair(entry.getKey(), entry.getValue()));
        }
        nvps.add(new NameValuePair(MessageConstants.METHOD, MessageConstants.VOIDACTION));
    }

    protected void setNvpsForCapture(List<NameValuePair> nvps, PayPalPaymentRequest paymentRequest) {
        nvps.add(new NameValuePair(MessageConstants.AUTHORIZATONID, paymentRequest.getTransactionID()));
        nvps.add(new NameValuePair(MessageConstants.AMOUNT, paymentRequest.getSummaryRequest().getGrandTotal().toString()));
        nvps.add(new NameValuePair(MessageConstants.COMPLETETYPE, MessageConstants.CAPTURECOMPLETE));
        for (Map.Entry<String, String> entry : getAdditionalConfig().entrySet()) {
            nvps.add(new NameValuePair(entry.getKey(), entry.getValue()));
        }
        nvps.add(new NameValuePair(MessageConstants.METHOD, MessageConstants.CAPTUREACTION));
    }

    protected void setNvpsForRefund(List<NameValuePair> nvps, PayPalPaymentRequest paymentRequest) {
        nvps.add(new NameValuePair(MessageConstants.TRANSACTIONID, paymentRequest.getTransactionID()));
        nvps.add(new NameValuePair(MessageConstants.REFUNDTYPE, paymentRequest.getRefundType().getType()));
        if (paymentRequest.getRefundType() != PayPalRefundType.FULL) {
            nvps.add(new NameValuePair(MessageConstants.AMOUNT, paymentRequest.getSummaryRequest().getGrandTotal().toString()));
        }
        nvps.add(new NameValuePair(MessageConstants.METHOD, MessageConstants.REFUNDACTION));
    }

    protected void setNvpsForProcess(List<NameValuePair> nvps, PayPalPaymentRequest paymentRequest) {
        if (paymentRequest.getSecondaryMethodType() != null && PayPalMethodType.AUTHORIZATION.equals(paymentRequest.getSecondaryMethodType())) {
            nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.PAYMENTACTION, new Integer[]{0}, new String[]{"n"}), MessageConstants.AUTHORIZATIONACTION));
        } else {
            nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.PAYMENTACTION, new Integer[]{0}, new String[]{"n"}), MessageConstants.SALEACTION));
        }
        nvps.add(new NameValuePair(MessageConstants.TOKEN, paymentRequest.getToken()));
        nvps.add(new NameValuePair(MessageConstants.PAYERID, paymentRequest.getPayerID()));

        for (Map.Entry<String, String> entry : getAdditionalConfig().entrySet()) {
            nvps.add(new NameValuePair(entry.getKey(), entry.getValue()));
        }
        nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.GRANDTOTALREQUEST, new Integer[]{0}, new String[]{"n"}), paymentRequest.getSummaryRequest().getGrandTotal().toString()));
        nvps.add(new NameValuePair(MessageConstants.METHOD, MessageConstants.PROCESSPAYMENTACTION));
        nvps.add(new NameValuePair(MessageConstants.BN, MessageConstants.BNCODE));
    }
    
    protected void setBaseNvps(List<NameValuePair> nvps) {
        nvps.add(new NameValuePair(MessageConstants.USER, user));
        nvps.add(new NameValuePair(MessageConstants.PASSWORD, password));
        nvps.add(new NameValuePair(MessageConstants.SIGNATURE, signature));
        nvps.add(new NameValuePair(MessageConstants.VERSION, libVersion));
    }
    
    protected void setNvpsForCheckoutOrAuth(List<NameValuePair> nvps, PayPalPaymentRequest paymentRequest, String payPalAction) {
        nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.PAYMENTACTION, new Integer[]{0}, new String[]{"n"}), payPalAction));
        nvps.add(new NameValuePair(MessageConstants.INVNUM, paymentRequest.getReferenceNumber()));

        //Determine if PayPal displays the shipping address fields on the PayPal pages.
        //For digital goods, this field is required and must be set to 1.
        // 0 - PayPal displays the shipping address passed in.
        // 1 - PayPal does not display the shipping fields at all. (Default)
        // 2 - PayPal will obtain the shipping address from the buyer's profile.
        nvps.add(new NameValuePair(MessageConstants.NOSHIPPING, shippingDisplayType.getType()));
        if (PayPalShippingDisplayType.PROVIDE_SHIPPING.equals(shippingDisplayType)){
            // This must be set to 1 in order for PayPal to display the passed in address
            nvps.add(new NameValuePair(MessageConstants.ADDROVERRIDE, "1"));
            setShippingNvps(nvps, paymentRequest);
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
            nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.NAMEREQUEST, new Integer[] {0, counter}, new String[] {"n", "m"}), itemRequest.getShortDescription()));
            nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.NUMBERREQUEST, new Integer[] {0, counter}, new String[] {"n", "m"}), itemRequest.getSystemId()));
            nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.DESCRIPTIONREQUEST, new Integer[] {0, counter}, new String[] {"n", "m"}), itemRequest.getDescription()));
            nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.AMOUNTREQUEST, new Integer[] {0, counter}, new String[] {"n", "m"}), handleZeroConversionForMoney(itemRequest.getUnitPrice())));
            nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.QUANTITYREQUEST, new Integer[] {0, counter}, new String[] {"n", "m"}), String.valueOf(itemRequest.getQuantity())));
            counter++;
        }
        nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.SUBTOTALREQUEST, new Integer[] {0}, new String[] {"n"}), handleZeroConversionForMoney(paymentRequest.getSummaryRequest().getSubTotal())));
        nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.TAXREQUEST, new Integer[] {0}, new String[] {"n"}), handleZeroConversionForMoney(paymentRequest.getSummaryRequest().getTotalTax())));
        nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.SHIPPINGREQUEST, new Integer[] {0}, new String[] {"n"}), handleZeroConversionForMoney(paymentRequest.getSummaryRequest().getTotalShipping())));
        nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.GRANDTOTALREQUEST, new Integer[] {0}, new String[] {"n"}), handleZeroConversionForMoney(paymentRequest.getSummaryRequest().getGrandTotal())));
    }

    protected void setShippingNvps(List<NameValuePair> nvps, PayPalPaymentRequest paymentRequest) {
        int counter = 0;
        for (PayPalShippingRequest shippingRequest : paymentRequest.getShippingRequests()) {
            nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.SHIPTONAME, new Integer[] {counter}, new String[] {"n"}), shippingRequest.getShipToName()));
            nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.SHIPTOSTREET, new Integer[] {counter}, new String[] {"n"}), shippingRequest.getShipToStreet()));
            nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.SHIPTOSTREET2, new Integer[] {counter}, new String[] {"n"}), shippingRequest.getShipToStreet2()));
            nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.SHIPTOCITY, new Integer[] {counter}, new String[] {"n"}), shippingRequest.getShipToCity()));
            nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.SHIPTOSTATE, new Integer[] {counter}, new String[] {"n"}), shippingRequest.getShipToState()));
            nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.SHIPTOZIP, new Integer[] {counter}, new String[] {"n"}), shippingRequest.getShipToZip()));
            nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.SHIPTOCOUNTRYCODE, new Integer[] {counter}, new String[] {"n"}), shippingRequest.getShipToCountryCode()));
            nvps.add(new NameValuePair(replaceNumericBoundProperty(MessageConstants.SHIPTOPHONENUMBER, new Integer[] {counter}, new String[] {"n"}), shippingRequest.getShipToPhoneNum()));
            counter++;
        }
    }

    //If the value of Money is Zero, we want to send "0" and not "0.000000".
    //This causes PayPal to throw invalid argument exceptions.
    protected String handleZeroConversionForMoney(Money money) {
        if (money.isZero()) {
            return "0";
        }
        return money.toString();
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
    
    protected String getRequestedServerPrefix() {
        HttpServletRequest request = BroadleafRequestContext.getBroadleafRequestContext().getRequest();
        String scheme = request.getScheme();
        StringBuilder serverPrefix = new StringBuilder(scheme);
        serverPrefix.append("://");
        serverPrefix.append(request.getServerName());
        if ((scheme.equalsIgnoreCase("http") && request.getServerPort() != 80) || (scheme.equalsIgnoreCase("https") && request.getServerPort() != 443)) {
        	serverPrefix.append(":");
        	serverPrefix.append(request.getServerPort());
        }
        return serverPrefix.toString();
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
        return Boolean.TRUE.equals(useRelativeUrls) ? getRequestedServerPrefix() + cancelUrl : cancelUrl;
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
        return Boolean.TRUE.equals(useRelativeUrls) ? getRequestedServerPrefix() + returnUrl : returnUrl;
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

    public String getShippingDisplayType() {
        if (shippingDisplayType != null) {
            return shippingDisplayType.getType();
        }
        return null;
    }

    public void setShippingDisplayType(String shippingDisplayType) {
        PayPalShippingDisplayType displayType = PayPalShippingDisplayType.getInstance(shippingDisplayType);
        if (displayType == null) {
            displayType = PayPalShippingDisplayType.NO_DISPLAY;
        }
        this.shippingDisplayType = displayType;
    }

	@Override
	public Boolean getUseRelativeUrls() {
		return useRelativeUrls;
	}

	@Override
	public void setUseRelativeUrls(Boolean useRelativeUrls) {
		this.useRelativeUrls = useRelativeUrls;
	}
}
