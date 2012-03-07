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

import org.apache.commons.lang.StringUtils;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalDetailsErrorResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalDetailsRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalDetailsResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalErrorResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalPayerAddress;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalPaymentDetails;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalPaymentItemDetails;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalPaymentRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalPaymentResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalAddressStatusType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalCheckoutStatusType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalMethodType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalPayerStatusType;

/**
 * @author Jeff Fischer
 */
public class PayPalResponseGeneratorImpl implements PayPalResponseGenerator {

    protected String userRedirectUrl;

    @Override
    public PayPalResponse buildResponse(String response, PayPalRequest paymentRequest) {
        PayPalResponse payPalResponse;
        if (paymentRequest.getMethodType() == PayPalMethodType.CHECKOUT) {
            payPalResponse = buildCheckoutResponse(response, (PayPalPaymentRequest) paymentRequest);
        } else if (paymentRequest.getMethodType() == PayPalMethodType.DETAILS) {
            payPalResponse = buildDetailsResponse(response, (PayPalDetailsRequest) paymentRequest);
        } else {
            throw new IllegalArgumentException("Method type not supported: " + paymentRequest.getMethodType().getFriendlyType());
        }

        return payPalResponse;
    }

    protected PayPalDetailsResponse buildDetailsResponse(String rawResponse, PayPalDetailsRequest paymentRequest) {
        PayPalDetailsResponse response = new PayPalDetailsResponse();
        response.setReferenceNumber(getResponseValue(rawResponse, MessageConstants.INVNUM));
        response.setResponseToken(getResponseValue(rawResponse, MessageConstants.TOKEN));
        response.setPhoneNumber(getResponseValue(rawResponse, MessageConstants.PHONENUM));
        String payPalAdjustment = getResponseValue(rawResponse, MessageConstants.PAYPALADJUSTMENT);
        if (!StringUtils.isEmpty(payPalAdjustment)) {
            response.setPayPalAdjustment(new Money(payPalAdjustment, Money.defaultCurrency()));
        }
        response.setNote(getResponseValue(rawResponse, MessageConstants.NOTE));
        String checkoutStatus = getResponseValue(rawResponse, MessageConstants.CHECKOUTSTATUS);
        if (!StringUtils.isEmpty(checkoutStatus)) {
            response.setCheckoutStatusType(PayPalCheckoutStatusType.getInstance(checkoutStatus));
        }
        response.setEmailAddress(getResponseValue(rawResponse, MessageConstants.EMAILADDRESS));
        response.setPayerId(getResponseValue(rawResponse, MessageConstants.PAYERID));
        String payerStatus = getResponseValue(rawResponse, MessageConstants.PAYERSTATUS);
        if (!StringUtils.isEmpty(payerStatus)) {
            response.setPayerStatus(PayPalPayerStatusType.getInstance(payerStatus));
        }
        response.setCountryCode(getResponseValue(rawResponse, MessageConstants.COUNTRYCODE));
        response.setBusiness(getResponseValue(rawResponse, MessageConstants.BUSINESS));
        response.setPayerSalutation(getResponseValue(rawResponse, MessageConstants.PAYERSALUTATION));
        response.setPayerFirstName(getResponseValue(rawResponse, MessageConstants.PAYERFIRSTNAME));
        response.setPayerLastName(getResponseValue(rawResponse, MessageConstants.PAYERLASTNAME));
        response.setPayerMiddleName(getResponseValue(rawResponse, MessageConstants.PAYERMIDDLENAME));
        response.setPayerSuffix(getResponseValue(rawResponse, MessageConstants.PAYERSUFFIX));

        boolean eof = false;
        int number = 0;
        while (!eof) {
            String street = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.SHIPTOSTREET, new Integer[]{number}, new String[]{"n"}));
            if (!StringUtils.isEmpty(street)) {
                PayPalPayerAddress address = new PayPalPayerAddress();
                address.setStreet(street);
                address.setName(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.SHIPTONAME, new Integer[]{number}, new String[]{"n"})));
                address.setStreet2(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.SHIPTOSTREET2, new Integer[]{number}, new String[]{"n"})));
                address.setCity(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.SHIPTOCITY, new Integer[]{number}, new String[]{"n"})));
                address.setState(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.SHIPTOSTATE, new Integer[]{number}, new String[]{"n"})));
                address.setZip(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.SHIPTOZIP, new Integer[]{number}, new String[]{"n"})));
                address.setCountryCode(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.SHIPTOCOUNTRYCODE, new Integer[]{number}, new String[]{"n"})));
                address.setPhoneNumber(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.SHIPTOPHONENUMBER, new Integer[]{number}, new String[]{"n"})));
                String addressStatus = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.ADDRESSSTATUS, new Integer[]{number}, new String[]{"n"}));
                if (!StringUtils.isEmpty(addressStatus)) {
                    address.setAddressStatus(PayPalAddressStatusType.getInstance(addressStatus));
                }
                response.getAddresses().add(address);
            } else {
                eof = true;
            }
            number++;
        }

        PayPalPaymentDetails paymentDetails = new PayPalPaymentDetails();
        String paymentRequestAmount = getResponseValue(rawResponse, MessageConstants.PAYMENTREQUESTAMOUNT);
        if (!StringUtils.isEmpty(paymentRequestAmount)) {
            paymentDetails.setAmount(new Money(paymentRequestAmount, Money.defaultCurrency()));
        }
        paymentDetails.setCurrencyCode(getResponseValue(rawResponse, MessageConstants.PAYMENTREQUESTCURRENCYCODE));
        String paymentRequestItemTotal = getResponseValue(rawResponse, MessageConstants.PAYMENTREQUESTITEMTOTAL);
        if (!StringUtils.isEmpty(paymentRequestItemTotal)) {
            paymentDetails.setItemTotal(new Money(paymentRequestItemTotal, Money.defaultCurrency()));
        }
        String paymentRequestShippingTotal = getResponseValue(rawResponse, MessageConstants.PAYMENTREQUESTSHIPPINGTOTAL);
        if (!StringUtils.isEmpty(paymentRequestShippingTotal)) {
            paymentDetails.setShippingTotal(new Money(paymentRequestShippingTotal, Money.defaultCurrency()));
        }
        String paymentRequestShippingDiscount = getResponseValue(rawResponse, MessageConstants.PAYMENTREQUESTSHIPPINGDICSOUNT);
        if (!StringUtils.isEmpty(paymentRequestShippingDiscount)) {
            paymentDetails.setShippingDiscount(new Money(paymentRequestShippingDiscount, Money.defaultCurrency()));
        }
        String paymentRequestTotalTax = getResponseValue(rawResponse, MessageConstants.PAYMENTREQUESTTOTALTAX);
        if (!StringUtils.isEmpty(paymentRequestTotalTax)) {
            paymentDetails.setTotalTax(new Money(paymentRequestTotalTax, Money.defaultCurrency()));
        }
        paymentDetails.setReferenceNumber(getResponseValue(rawResponse, MessageConstants.PAYMENTREQUESTREFERENCENUMBER));
        paymentDetails.setTransactionId(getResponseValue(rawResponse, MessageConstants.PAYMENTREQUESTTRANSACTIONID));
        paymentDetails.setPaymentMethod(getResponseValue(rawResponse, MessageConstants.PAYMENTREQUESTPAYMENTMETHOD));
        paymentDetails.setPaymentRequestId(getResponseValue(rawResponse, MessageConstants.PAYMENTREQUESTPAYMENTREQUESTID));
        response.setPaymentDetails(paymentDetails);
        
        eof = false;
        number = 0;
        while (!eof) {
            String paymentRequestItemName = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.PAYMENTREQUESTITEMNAME, new Integer[]{0, number}, new String[]{"n", "m"}));
            if (!StringUtils.isEmpty(paymentRequestItemName)) {
                PayPalPaymentItemDetails itemDetails = new PayPalPaymentItemDetails();
                itemDetails.setName(paymentRequestItemName);
                itemDetails.setDescription(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.PAYMENTREQUESTITEMDESCRIPTION, new Integer[]{0, number}, new String[]{"n", "m"})));
                String paymentRequestItemAmount = getResponseValue(rawResponse, MessageConstants.PAYMENTREQUESTITEMAMOUNT);
                if (!StringUtils.isEmpty(paymentRequestItemAmount)) {
                    itemDetails.setAmount(new Money(paymentRequestItemAmount, Money.defaultCurrency()));
                }
                itemDetails.setItemNumber(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.PAYMENTREQUESTITEMNUMBER, new Integer[]{0, number}, new String[]{"n", "m"})));
                String paymentRequestItemQuantity = getResponseValue(rawResponse, MessageConstants.PAYMENTREQUESTITEMQUANTITY);
                if (!StringUtils.isEmpty(paymentRequestItemQuantity)) {
                    itemDetails.setQuantity(Integer.valueOf(paymentRequestItemQuantity));
                }
                String paymentRequestItemTax = getResponseValue(rawResponse, MessageConstants.PAYMENTREQUESTITEMTAX);
                if (!StringUtils.isEmpty(paymentRequestItemTax)) {
                    itemDetails.setTax(new Money(paymentRequestItemTax, Money.defaultCurrency()));
                }
                paymentDetails.getItemDetails().add(itemDetails);
            } else {
                eof = true;
            }
            number++;
        }

        eof = false;
        int errorNumber = 0;
        while (!eof) {
            String errorCode = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.PAYMENTREQUESTERRORCODE, new Integer[]{errorNumber}, new String[]{"n"}));
            if (errorCode != null) {
                PayPalDetailsErrorResponse errorResponse = new PayPalDetailsErrorResponse();
                errorResponse.setErrorCode(errorCode);
                errorResponse.setShortMessage(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.PAYMENTREQUESTERORRSHORTMESSAGE, new Integer[]{errorNumber}, new String[]{"n"})));
                errorResponse.setLongMessage(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.PAYMENTREQUESTERRORLONGMESSAGE, new Integer[]{errorNumber}, new String[]{"n"})));
                errorResponse.setSeverityCode(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.PAYMENTREQUESTERRORSEVERITYCODE, new Integer[]{errorNumber}, new String[]{"n"})));
                errorResponse.setAck(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.PAYMENTREQUESTERRORACK, new Integer[]{errorNumber}, new String[]{"n"})));
                response.getErrorResponses().add(errorResponse);
            } else {
                eof = true;
            }
            errorNumber++;
        }

        return response;
    }

    protected PayPalPaymentResponse buildCheckoutResponse(String rawResponse, PayPalPaymentRequest paymentRequest) {
        PayPalPaymentResponse response = new PayPalPaymentResponse();
        response.setTransactionType(paymentRequest.getTransactionType());
        response.setMethodType(paymentRequest.getMethodType());
        response.setCorrelationId(getResponseValue(rawResponse, MessageConstants.CORRELATIONID));
        response.setReferenceNumber(getResponseValue(rawResponse, MessageConstants.INVNUM));
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
                String errorCode = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.ERRORCODE, new Integer[]{errorNumber}, new String[]{"n"}));
                if (errorCode != null) {
                    PayPalErrorResponse errorResponse = new PayPalErrorResponse();
                    errorResponse.setErrorCode(errorCode);
                    errorResponse.setShortMessage(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.ERRORSHORTMESSAGE, new Integer[]{errorNumber}, new String[]{"n"})));
                    errorResponse.setLongMessage(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.ERRORLONGMESSAGE, new Integer[]{errorNumber}, new String[]{"n"})));
                    errorResponse.setSeverityCode(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.ERRORSEVERITYCODE, new Integer[]{errorNumber}, new String[]{"n"})));
                    response.getErrorResponses().add(errorResponse);
                } else {
                    eof = true;
                }
                errorNumber++;
            }

            errorNumber = 0;
            eof = false;
            while (!eof) {
                String passThroughErrorName = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.ERRORPASSTHROUGHNAME, new Integer[]{errorNumber}, new String[]{"n"}));
                if (passThroughErrorName != null) {
                    response.getPassThroughErrors().put(passThroughErrorName, getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.ERRORPASSTHROUGHVALUE, new Integer[]{errorNumber}, new String[]{"n"})));
                } else {
                    eof = true;
                }
                errorNumber++;
            }
        }
        response.setUserRedirectUrl(getUserRedirectUrl() + "?cmd=_express-checkout&token=" + response.getResponseToken());
        
        return response;
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

    @Override
    public String getUserRedirectUrl() {
        return userRedirectUrl;
    }

    @Override
    public void setUserRedirectUrl(String userRedirectUrl) {
        this.userRedirectUrl = userRedirectUrl;
    }
}
