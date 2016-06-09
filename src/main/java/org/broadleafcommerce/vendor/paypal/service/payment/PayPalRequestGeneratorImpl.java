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
package org.broadleafcommerce.vendor.paypal.service.payment;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.util.BLCRequestUtils;
import org.broadleafcommerce.payment.service.gateway.PayPalExpressConfiguration;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.details.PayPalDetailsRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalItemRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalPaymentRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalShippingRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalMethodType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalRefundType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalShippingDisplayType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

/**
 * @author Jeff Fischer
 */
@Service("blPayPalExpressRequestGenerator")
public class PayPalRequestGeneratorImpl implements PayPalRequestGenerator {

    @Resource(name = "blPayPalExpressConfiguration")
    protected PayPalExpressConfiguration configuration;

    protected Logger logger = Logger.getLogger(PayPalRequestGeneratorImpl.class);
    
    @Override
    public List<NameValuePair> buildRequest(PayPalRequest request) {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        setBaseNvps(nvps);

        if (PayPalMethodType.CHECKOUT.equals(request.getMethodType())) {
            setNvpsForCheckoutOrAuth(nvps, (PayPalPaymentRequest) request, MessageConstants.SALEACTION);
            nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTCURRENCYCODE, new Integer[]{0}, new String[]{"n"}), ((PayPalPaymentRequest) request).getCurrency()));
        } else if (PayPalMethodType.AUTHORIZATION.equals(request.getMethodType())) {
            setNvpsForCheckoutOrAuth(nvps, (PayPalPaymentRequest) request, MessageConstants.AUTHORIZATIONACTION);
            nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTCURRENCYCODE, new Integer[]{0}, new String[]{"n"}), ((PayPalPaymentRequest) request).getCurrency()));
        } else if (PayPalMethodType.PROCESS.equals(request.getMethodType())) {
            setCostNvps(nvps, (PayPalPaymentRequest) request);
            setNvpsForProcess(nvps, (PayPalPaymentRequest) request);
            nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTCURRENCYCODE, new Integer[]{0}, new String[]{"n"}), ((PayPalPaymentRequest) request).getCurrency()));
        } else if (PayPalMethodType.REFUND.equals(request.getMethodType())) {
            setNvpsForRefund(nvps, (PayPalPaymentRequest) request);
            nvps.add(new BasicNameValuePair(MessageConstants.CURRENCYCODE, ((PayPalPaymentRequest) request).getCurrency()));
        } else if (PayPalMethodType.CAPTURE.equals(request.getMethodType())) {
            setNvpsForCapture(nvps, (PayPalPaymentRequest) request);
            nvps.add(new BasicNameValuePair(MessageConstants.CURRENCYCODE, ((PayPalPaymentRequest) request).getCurrency()));
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
        nvps.add(new BasicNameValuePair(MessageConstants.TOKEN, paymentRequest.getToken()));
        nvps.add(new BasicNameValuePair(MessageConstants.METHOD, MessageConstants.PAYMENTDETAILSACTION));
    }

    protected void setNvpsForVoid(List<NameValuePair> nvps, PayPalPaymentRequest paymentRequest) {
        nvps.add(new BasicNameValuePair(MessageConstants.AUTHORIZATONID, paymentRequest.getTransactionID()));

        for (Map.Entry<String, String> entry : getAdditionalConfig().entrySet()) {
            nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        nvps.add(new BasicNameValuePair(MessageConstants.METHOD, MessageConstants.VOIDACTION));
    }

    protected void setNvpsForCapture(List<NameValuePair> nvps, PayPalPaymentRequest paymentRequest) {
        nvps.add(new BasicNameValuePair(MessageConstants.AUTHORIZATONID, paymentRequest.getTransactionID()));
        nvps.add(new BasicNameValuePair(MessageConstants.AMOUNT, paymentRequest.getSummaryRequest().getGrandTotal().toString()));
        nvps.add(new BasicNameValuePair(MessageConstants.COMPLETETYPE, MessageConstants.CAPTURECOMPLETE));

        if (getAdditionalConfig() != null) {
            for (Map.Entry<String, String> entry : getAdditionalConfig().entrySet()) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }

        nvps.add(new BasicNameValuePair(MessageConstants.METHOD, MessageConstants.CAPTUREACTION));
    }

    protected void setNvpsForRefund(List<NameValuePair> nvps, PayPalPaymentRequest paymentRequest) {
        nvps.add(new BasicNameValuePair(MessageConstants.TRANSACTIONID, paymentRequest.getTransactionID()));
        nvps.add(new BasicNameValuePair(MessageConstants.REFUNDTYPE, paymentRequest.getRefundType().getType()));
        if (paymentRequest.getRefundType() != PayPalRefundType.FULL) {
            nvps.add(new BasicNameValuePair(MessageConstants.AMOUNT, paymentRequest.getSummaryRequest().getGrandTotal().toString()));
        }
        nvps.add(new BasicNameValuePair(MessageConstants.METHOD, MessageConstants.REFUNDACTION));
    }

    protected void setNvpsForProcess(List<NameValuePair> nvps, PayPalPaymentRequest paymentRequest) {
        if (paymentRequest.getSecondaryMethodType() != null && PayPalMethodType.AUTHORIZATION.equals(paymentRequest.getSecondaryMethodType())) {
            nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.PAYMENTACTION, new Integer[]{0}, new String[]{"n"}), MessageConstants.AUTHORIZATIONACTION));
        } else {
            nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.PAYMENTACTION, new Integer[]{0}, new String[]{"n"}), MessageConstants.SALEACTION));
        }
        nvps.add(new BasicNameValuePair(MessageConstants.TOKEN, paymentRequest.getToken()));
        nvps.add(new BasicNameValuePair(MessageConstants.PAYERID, paymentRequest.getPayerID()));

        for (Map.Entry<String, String> entry : getAdditionalConfig().entrySet()) {
            nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.GRANDTOTALREQUEST, new Integer[]{0}, new String[]{"n"}), paymentRequest.getSummaryRequest().getGrandTotal().toString()));
        nvps.add(new BasicNameValuePair(MessageConstants.METHOD, MessageConstants.PROCESSPAYMENTACTION));
        nvps.add(new BasicNameValuePair(MessageConstants.BN, MessageConstants.BNCODE));
    }
    
    protected void setBaseNvps(List<NameValuePair> nvps) {
        nvps.add(new BasicNameValuePair(MessageConstants.USER, getUser()));
        nvps.add(new BasicNameValuePair(MessageConstants.PASSWORD, getPassword()));
        nvps.add(new BasicNameValuePair(MessageConstants.SIGNATURE, getSignature()));
        nvps.add(new BasicNameValuePair(MessageConstants.VERSION, getLibVersion()));
    }
    
    protected void setNvpsForCheckoutOrAuth(List<NameValuePair> nvps, PayPalPaymentRequest paymentRequest, String payPalAction) {
        nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.PAYMENTACTION, new Integer[]{0}, new String[]{"n"}), payPalAction));

        //PAYMENTREQUEST_0_CUSTOM is the boolean of whether or not to Complete Checkout on Callback and the Broadleaf Order ID
        // concatenated with an underscore "_"
        // for example if the complete checkout on callback = true and the order id = 12345
        // the custom fields would be true_12345
        String customField = Boolean.toString(paymentRequest.isCompleteCheckoutOnCallback()) + "_" + paymentRequest.getOrderId();
        nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTCUSTOM, new Integer[]{0}, new String[]{"n"}), customField));

        //Determine if PayPal displays the shipping address fields on the PayPal pages.
        //For digital goods, this field is required and must be set to 1.
        // 0 - PayPal displays the shipping address passed in.
        // 1 - PayPal does not display the shipping fields at all. (Default)
        // 2 - PayPal will obtain the shipping address from the buyer's profile.
        nvps.add(new BasicNameValuePair(MessageConstants.NOSHIPPING, getShippingDisplayType().getType()));
        if (PayPalShippingDisplayType.PROVIDE_SHIPPING.equals(getShippingDisplayType())){
            // This must be set to 1 in order for PayPal to display the passed in address
            nvps.add(new BasicNameValuePair(MessageConstants.ADDROVERRIDE, "1"));
            setShippingNvps(nvps, paymentRequest);
        }

        setCostNvps(nvps, paymentRequest);

        nvps.add(new BasicNameValuePair(MessageConstants.RETURNURL, getReturnUrl()));
        nvps.add(new BasicNameValuePair(MessageConstants.CANCELURL, getCancelUrl()));
        nvps.add(new BasicNameValuePair(MessageConstants.TOTALTYPE, getTotalType()));
        for (Map.Entry<String, String> entry : getAdditionalConfig().entrySet()) {
            nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        nvps.add(new BasicNameValuePair(MessageConstants.METHOD, MessageConstants.EXPRESSCHECKOUTACTION));
    }
    
    protected void setCostNvps(List<NameValuePair> nvps, PayPalPaymentRequest paymentRequest) {
        int counter = 0;
        for (PayPalItemRequest itemRequest : paymentRequest.getItemRequests()) {
            nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.NAMEREQUEST, new Integer[] {0, counter}, new String[] {"n", "m"}), StringUtils.abbreviate(itemRequest.getShortDescription(), 120)));
            nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.NUMBERREQUEST, new Integer[] {0, counter}, new String[] {"n", "m"}), itemRequest.getSystemId()));
            nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.DESCRIPTIONREQUEST, new Integer[] {0, counter}, new String[] {"n", "m"}), StringUtils.abbreviate(itemRequest.getDescription(), 120)));
            nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.AMOUNTREQUEST, new Integer[] {0, counter}, new String[] {"n", "m"}), handleZeroConversionForMoney(itemRequest.getUnitPrice())));
            nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.QUANTITYREQUEST, new Integer[] {0, counter}, new String[] {"n", "m"}), String.valueOf(itemRequest.getQuantity())));
            counter++;
        }
        nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.SUBTOTALREQUEST, new Integer[] {0}, new String[] {"n"}), handleZeroConversionForMoney(paymentRequest.getSummaryRequest().getSubTotal())));
        nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.TAXREQUEST, new Integer[] {0}, new String[] {"n"}), handleZeroConversionForMoney(paymentRequest.getSummaryRequest().getTotalTax())));
        nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.SHIPPINGREQUEST, new Integer[] {0}, new String[] {"n"}), handleZeroConversionForMoney(paymentRequest.getSummaryRequest().getTotalShipping())));
        nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.GRANDTOTALREQUEST, new Integer[] {0}, new String[] {"n"}), handleZeroConversionForMoney(paymentRequest.getSummaryRequest().getGrandTotal())));
    }

    protected void setShippingNvps(List<NameValuePair> nvps, PayPalPaymentRequest paymentRequest) {
        int counter = 0;
        for (PayPalShippingRequest shippingRequest : paymentRequest.getShippingRequests()) {
            if (StringUtils.isNotBlank(shippingRequest.getShipToName())) {
                nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.SHIPTONAME, new Integer[] {counter}, new String[] {"n"}), shippingRequest.getShipToName()));
            }
            if (StringUtils.isNotBlank(shippingRequest.getShipToStreet())) {
                nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.SHIPTOSTREET, new Integer[] {counter}, new String[] {"n"}), shippingRequest.getShipToStreet()));
            }
            if (StringUtils.isNotBlank(shippingRequest.getShipToStreet2())) {
                nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.SHIPTOSTREET2, new Integer[] {counter}, new String[] {"n"}), shippingRequest.getShipToStreet2()));
            }
            if (StringUtils.isNotBlank(shippingRequest.getShipToCity())) {
                nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.SHIPTOCITY, new Integer[] {counter}, new String[] {"n"}), shippingRequest.getShipToCity()));
            }
            if (StringUtils.isNotBlank(shippingRequest.getShipToState())) {
                nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.SHIPTOSTATE, new Integer[] {counter}, new String[] {"n"}), shippingRequest.getShipToState()));
            }
            if (StringUtils.isNotBlank(shippingRequest.getShipToZip())) {
                nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.SHIPTOZIP, new Integer[] {counter}, new String[] {"n"}), shippingRequest.getShipToZip()));
            }
            if (StringUtils.isNotBlank(shippingRequest.getShipToCountryCode())) {
                nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.SHIPTOCOUNTRYCODE, new Integer[] {counter}, new String[] {"n"}), shippingRequest.getShipToCountryCode()));
            }
            if (StringUtils.isNotBlank(shippingRequest.getShipToPhoneNum())) {
                nvps.add(new BasicNameValuePair(replaceNumericBoundProperty(MessageConstants.SHIPTOPHONENUMBER, new Integer[] {counter}, new String[] {"n"}), shippingRequest.getShipToPhoneNum()));
            }
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

    @Override
    public Map<String, String> getAdditionalConfig() {
        return configuration.getAdditionalConfig();
    }

    @Override
    public String getCancelUrl() {
        return configuration.getCancelUrl();
    }

    @Override
    public String getLibVersion() {
        return configuration.getLibVersion();
    }

    @Override
    public String getPassword() {
        return configuration.getPassword();
    }

    @Override
    public String getReturnUrl() {
        return configuration.getReturnUrl();
    }

    @Override
    public String getSignature() {
        return configuration.getSignature();
    }

    @Override
    public String getUser() {
        return configuration.getUser();
    }

    @Override
    public String getTotalType() {
        return configuration.getTotalType();
    }

    @Override
    public PayPalShippingDisplayType getShippingDisplayType() {
        return configuration.getShippingDisplayType();
    }

}
