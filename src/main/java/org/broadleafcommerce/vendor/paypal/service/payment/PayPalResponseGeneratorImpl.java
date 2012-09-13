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

import org.apache.commons.lang.StringUtils;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.vendor.paypal.service.payment.message.ErrorCheckable;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalErrorResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.message.details.PayPalDetailsResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.message.details.PayPalPayerAddress;
import org.broadleafcommerce.vendor.paypal.service.payment.message.details.PayPalPaymentDetails;
import org.broadleafcommerce.vendor.paypal.service.payment.message.details.PayPalPaymentItemDetails;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalPaymentInfo;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalPaymentRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalPaymentResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalRefundInfo;
import org.broadleafcommerce.vendor.paypal.service.payment.type.*;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Currency;

/**
 * @author Jeff Fischer
 */
public class PayPalResponseGeneratorImpl implements PayPalResponseGenerator {

    protected String userRedirectUrl;
    protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Override
    public PayPalResponse buildResponse(String response, PayPalRequest paymentRequest) {
        PayPalResponse payPalResponse;
        if (PayPalMethodType.CHECKOUT.equals(paymentRequest.getMethodType()) || PayPalMethodType.AUTHORIZATION.equals(paymentRequest.getMethodType())) {
            payPalResponse = buildCheckoutResponse(response, (PayPalPaymentRequest) paymentRequest);
        } else if (PayPalMethodType.DETAILS.equals(paymentRequest.getMethodType())) {
            payPalResponse = buildDetailsResponse(response);
        } else if (PayPalMethodType.PROCESS.equals(paymentRequest.getMethodType())) {
            payPalResponse = buildCheckoutResponse(response, (PayPalPaymentRequest) paymentRequest);
            addPaymentInfoData(response, (PayPalPaymentResponse) payPalResponse);
        } else {
            payPalResponse = buildCheckoutResponse(response, (PayPalPaymentRequest) paymentRequest);
            addRefundData(response, (PayPalPaymentResponse) payPalResponse);
            addCapturePaymentInfoData(response, (PayPalPaymentResponse) payPalResponse);
        }

        return payPalResponse;
    }

    protected PayPalDetailsResponse buildDetailsResponse(String rawResponse) {
        PayPalDetailsResponse response = new PayPalDetailsResponse();
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
        String currencyCode = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTCURRENCYCODE, new Integer[]{0}, new String[]{"n"}));
        paymentDetails.setCurrencyCode(currencyCode);
        String paymentRequestAmount = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTAMOUNT, new Integer[]{0}, new String[]{"n"}));
        if (!StringUtils.isEmpty(paymentRequestAmount)) {
            paymentDetails.setAmount(new Money(paymentRequestAmount, Currency.getInstance(currencyCode)));
        }
        String paymentRequestItemTotal = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTITEMTOTAL, new Integer[]{0}, new String[]{"n"}));
        if (!StringUtils.isEmpty(paymentRequestItemTotal)) {
            paymentDetails.setItemTotal(new Money(paymentRequestItemTotal, Currency.getInstance(currencyCode)));
        }
        String paymentRequestShippingTotal = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTSHIPPINGTOTAL, new Integer[]{0}, new String[]{"n"}));
        if (!StringUtils.isEmpty(paymentRequestShippingTotal)) {
            paymentDetails.setShippingTotal(new Money(paymentRequestShippingTotal, Currency.getInstance(currencyCode)));
        }
        String paymentRequestShippingDiscount = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTSHIPPINGDISCOUNT, new Integer[]{0}, new String[]{"n"}));
        if (!StringUtils.isEmpty(paymentRequestShippingDiscount)) {
            paymentDetails.setShippingDiscount(new Money(paymentRequestShippingDiscount, Currency.getInstance(currencyCode)));
        }
        String paymentRequestTotalTax = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTTOTALTAX, new Integer[]{0}, new String[]{"n"}));
        if (!StringUtils.isEmpty(paymentRequestTotalTax)) {
            paymentDetails.setTotalTax(new Money(paymentRequestTotalTax, Currency.getInstance(currencyCode)));
        }
        paymentDetails.setReferenceNumber(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTREFERENCENUMBER, new Integer[]{0}, new String[]{"n"})));
        paymentDetails.setTransactionId(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTTRANSACTIONID, new Integer[]{0}, new String[]{"n"})));
        paymentDetails.setPaymentMethod(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTALLOWEDMETHOD, new Integer[]{0}, new String[]{"n"})));
        paymentDetails.setPaymentRequestId(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTREQUESTID, new Integer[]{0}, new String[]{"n"})));
        response.setPaymentDetails(paymentDetails);
        
        eof = false;
        number = 0;
        while (!eof) {
            String paymentRequestItemName = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTITEMNAME, new Integer[]{0, number}, new String[]{"n", "m"}));
            if (!StringUtils.isEmpty(paymentRequestItemName)) {
                PayPalPaymentItemDetails itemDetails = new PayPalPaymentItemDetails();
                itemDetails.setName(paymentRequestItemName);
                itemDetails.setDescription(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTITEMDESCRIPTION, new Integer[]{0, number}, new String[]{"n", "m"})));
                String paymentRequestItemAmount = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTITEMAMOUNT, new Integer[]{0, number}, new String[]{"n", "m"}));
                if (!StringUtils.isEmpty(paymentRequestItemAmount)) {
                    itemDetails.setAmount(new Money(paymentRequestItemAmount, Currency.getInstance(currencyCode)));
                }
                itemDetails.setItemNumber(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTITEMNUMBER, new Integer[]{0, number}, new String[]{"n", "m"})));
                String paymentRequestItemQuantity = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTITEMQUANTITY, new Integer[]{0, number}, new String[]{"n", "m"}));
                if (!StringUtils.isEmpty(paymentRequestItemQuantity)) {
                    itemDetails.setQuantity(Integer.valueOf(paymentRequestItemQuantity));
                }
                String paymentRequestItemTax = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTITEMTAX, new Integer[]{0, number}, new String[]{"n", "m"}));
                if (!StringUtils.isEmpty(paymentRequestItemTax)) {
                    itemDetails.setTax(new Money(paymentRequestItemTax, Currency.getInstance(currencyCode)));
                }
                paymentDetails.getItemDetails().add(itemDetails);
            } else {
                eof = true;
            }
            number++;
        }

        populateDetailErrors(rawResponse, response);

        return response;
    }

    protected void populateDetailErrors(String rawResponse, ErrorCheckable response) {
        boolean eof;
        eof = false;
        int errorNumber = 0;
        while (!eof) {
            String errorCode = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTERRORCODE, new Integer[]{errorNumber}, new String[]{"n"}));
            if (errorCode != null) {
                PayPalErrorResponse errorResponse = new PayPalErrorResponse();
                errorResponse.setErrorCode(errorCode);
                errorResponse.setShortMessage(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTERRORSHORTMESSAGE, new Integer[]{errorNumber}, new String[]{"n"})));
                errorResponse.setLongMessage(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTERRORLONGMESSAGE, new Integer[]{errorNumber}, new String[]{"n"})));
                errorResponse.setSeverityCode(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTERRORSEVERITYCODE, new Integer[]{errorNumber}, new String[]{"n"})));
                errorResponse.setAck(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.DETAILSPAYMENTERRORACK, new Integer[]{errorNumber}, new String[]{"n"})));
                response.getErrorResponses().add(errorResponse);
            } else {
                eof = true;
            }
            errorNumber++;
        }
    }
    
    protected void addRefundData(String rawResponse, PayPalPaymentResponse response) {
        PayPalRefundInfo refundInfo = new PayPalRefundInfo();
        response.setRefundInfo(refundInfo);
        String currencyCode = getResponseValue(rawResponse, MessageConstants.CURRENCYCODE);
        refundInfo.setCurrencyCode(currencyCode);
        refundInfo.setRefundTransactionId(getResponseValue(rawResponse, MessageConstants.REFUNDTRANSACTIONID));
        String feeRefundAmount = getResponseValue(rawResponse, MessageConstants.FEEREFUNDAMT);
        if (!StringUtils.isEmpty(feeRefundAmount)) {
            refundInfo.setFeeRefundAmount(new Money(feeRefundAmount, Currency.getInstance(currencyCode)));
        }
        String grossRefundAmount = getResponseValue(rawResponse, MessageConstants.GROSSREFUNDAMT);
        if (!StringUtils.isEmpty(grossRefundAmount)) {
            refundInfo.setGrossRefundAmount(new Money(grossRefundAmount, Currency.getInstance(currencyCode)));
        }
        String netRefundAmount = getResponseValue(rawResponse, MessageConstants.NETREFUNDAMT);
        if (!StringUtils.isEmpty(netRefundAmount)) {
            refundInfo.setNetRefundAmount(new Money(netRefundAmount, Currency.getInstance(currencyCode)));
        }
        String totalRefundAmount = getResponseValue(rawResponse, MessageConstants.TOTALREFUNDEDAMT);
        if (!StringUtils.isEmpty(totalRefundAmount)) {
            refundInfo.setTotalRefundAmount(new Money(totalRefundAmount, Currency.getInstance(currencyCode)));
        }
        refundInfo.setRefundInfo(getResponseValue(rawResponse, MessageConstants.REFUNDINFO));
        String refundStatus = getResponseValue(rawResponse, MessageConstants.REFUNDSTATUS);
        if (!StringUtils.isEmpty(refundStatus)) {
            refundInfo.setRefundStatusType(PayPalRefundStatusType.getInstance(refundStatus));
        }
        String pendingReason = getResponseValue(rawResponse, MessageConstants.PENDINGREASON);
        if (!StringUtils.isEmpty(pendingReason)) {
            refundInfo.setPendingReasonType(PayPalRefundPendingReasonType.getInstance(pendingReason));
        }
    }
    
    protected void addCapturePaymentInfoData(String rawResponse, PayPalPaymentResponse response) {
        PayPalPaymentInfo paymentInfo = new PayPalPaymentInfo();
        response.setPaymentInfo(paymentInfo);
        String currencyCode = getResponseValue(rawResponse, MessageConstants.CURRENCYCODE);
        paymentInfo.setCurrencyCode(currencyCode);
        paymentInfo.setTransactionId(getResponseValue(rawResponse, MessageConstants.TRANSACTIONID));
        paymentInfo.setParentTransactionId(getResponseValue(rawResponse, MessageConstants.PARENTTRANSACTIONID));
        paymentInfo.setReceiptId(getResponseValue(rawResponse, MessageConstants.RECEIPTID));

        String paymentType = getResponseValue(rawResponse, MessageConstants.PAYMENTTYPE);
        if (!StringUtils.isEmpty(paymentType)) {
            paymentInfo.setPaymentType(PayPalPaymentType.getInstance(paymentType));
        }
        String orderTime = getResponseValue(rawResponse, MessageConstants.ORDERITEM);
        if (!StringUtils.isEmpty(orderTime)) {
            try {
                paymentInfo.setOrderTime(dateFormat.parse(orderTime));
            } catch (ParseException e) {
                throw new RuntimeException("Unable to parse the date string (" + orderTime + ")");
            }
        }
        String amount = getResponseValue(rawResponse, MessageConstants.AMOUNT);
        if (!StringUtils.isEmpty(amount)) {
            paymentInfo.setTotalAmount(new Money(amount, Currency.getInstance(currencyCode)));
        }
        String feeAmount = getResponseValue(rawResponse, MessageConstants.FEEAMOUNT);
        if (!StringUtils.isEmpty(feeAmount)) {
            paymentInfo.setFeeAmount(new Money(feeAmount, Currency.getInstance(currencyCode)));
        }
        String settleAmount = getResponseValue(rawResponse, MessageConstants.SETTLEAMOUNT);
        if (!StringUtils.isEmpty(settleAmount)) {
            paymentInfo.setSettleAmount(new Money(settleAmount, Currency.getInstance(currencyCode)));
        }
        String taxAmount = getResponseValue(rawResponse, MessageConstants.TAXAMOUNT);
        if (!StringUtils.isEmpty(taxAmount)) {
            paymentInfo.setTaxAmount(new Money(taxAmount, Currency.getInstance(currencyCode)));
        }
        String exchangeRate = getResponseValue(rawResponse, MessageConstants.EXCHANGERATE);
        if (!StringUtils.isEmpty(exchangeRate)) {
            paymentInfo.setExchangeRate(new BigDecimal(exchangeRate));
        }
        String paymentStatusType = getResponseValue(rawResponse, MessageConstants.PAYMENTSTATUSTYPE);
        if (!StringUtils.isEmpty(paymentStatusType)) {
            paymentInfo.setPaymentStatusType(PayPalPaymentStatusType.getInstance(paymentStatusType));
        }
        String pendingReasonType = getResponseValue(rawResponse, MessageConstants.PENDINGREASONTYPE);
        if (!StringUtils.isEmpty(pendingReasonType)) {
            paymentInfo.setPendingReasonType(PayPalPendingReasonType.getInstance(pendingReasonType));
        }
    }

    protected void addPaymentInfoData(String rawResponse, PayPalPaymentResponse response) {
        PayPalPaymentInfo paymentInfo = new PayPalPaymentInfo();
        response.setPaymentInfo(paymentInfo);
        String currencyCode = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.PROCESSPAYMENTCURRENCYCODE, new Integer[]{0}, new String[]{"n"}));
        paymentInfo.setCurrencyCode(currencyCode);
        paymentInfo.setTransactionId(getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.PROCESSPAYMENTTRANSACTIONID, new Integer[]{0}, new String[]{"n"})));
        String paymentType = getResponseValue(rawResponse, MessageConstants.PROCESSPAYMENTPAYMENTTYPE);
        if (!StringUtils.isEmpty(paymentType)) {
            paymentInfo.setPaymentType(PayPalPaymentType.getInstance(paymentType));
        }
        String orderTime = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.PROCESSPAYMENTORDERITEM, new Integer[]{0}, new String[]{"n"}));
        if (!StringUtils.isEmpty(orderTime)) {
            try {
                paymentInfo.setOrderTime(dateFormat.parse(orderTime));
            } catch (ParseException e) {
                throw new RuntimeException("Unable to parse the date string (" + orderTime + ")");
            }
        }
        String amount = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.PROCESSPAYMENTAMOUNT, new Integer[]{0}, new String[]{"n"}));
        if (!StringUtils.isEmpty(amount)) {
            paymentInfo.setTotalAmount(new Money(amount, Currency.getInstance(currencyCode)));
        }
        String feeAmount = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.PROCESSPAYMENTFEEAMOUNT, new Integer[]{0}, new String[]{"n"}));
        if (!StringUtils.isEmpty(feeAmount)) {
            paymentInfo.setFeeAmount(new Money(feeAmount, Currency.getInstance(currencyCode)));
        }
        String settleAmount = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.PROCESSPAYMENTSETTLEAMOUNT, new Integer[]{0}, new String[]{"n"}));
        if (!StringUtils.isEmpty(settleAmount)) {
            paymentInfo.setSettleAmount(new Money(settleAmount, Currency.getInstance(currencyCode)));
        }
        String taxAmount = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.PROCESSPAYMENTTAXAMOUNT, new Integer[]{0}, new String[]{"n"}));
        if (!StringUtils.isEmpty(taxAmount)) {
            paymentInfo.setTaxAmount(new Money(taxAmount, Currency.getInstance(currencyCode)));
        }
        String exchangeRate = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.PROCESSPAYMENTEXCHANGERATE, new Integer[]{0}, new String[]{"n"}));
        if (!StringUtils.isEmpty(exchangeRate)) {
            paymentInfo.setExchangeRate(new BigDecimal(exchangeRate));
        }
        String paymentStatusType = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.PROCESSPAYMENTPAYMENTSTATUSTYPE, new Integer[]{0}, new String[]{"n"}));
        if (!StringUtils.isEmpty(paymentStatusType)) {
            paymentInfo.setPaymentStatusType(PayPalPaymentStatusType.getInstance(paymentStatusType));
        }
        String pendingReasonType = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.PROCESSPAYMENTPENDINGREASONTYPE, new Integer[]{0}, new String[]{"n"}));
        if (!StringUtils.isEmpty(pendingReasonType)) {
            paymentInfo.setPendingReasonType(PayPalPendingReasonType.getInstance(pendingReasonType));
        }
        String reasonCodeType = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.PROCESSPAYMENTREASONCODETYPE, new Integer[]{0}, new String[]{"n"}));
        if (!StringUtils.isEmpty(reasonCodeType)) {
            paymentInfo.setReasonCodeType(PayPalReasonCodeType.getInstance(reasonCodeType));
        }
        String holdDecisionType = getResponseValue(rawResponse, replaceNumericBoundProperty(MessageConstants.PROCESSPAYMENTHOLDDECISIONTYPE, new Integer[]{0}, new String[]{"n"}));
        if (!StringUtils.isEmpty(holdDecisionType)) {
            paymentInfo.setHoldDecisionType(PayPalHoldDecisionType.getInstance(holdDecisionType));
        }

        populateDetailErrors(rawResponse, response);
    }

    protected PayPalPaymentResponse buildCheckoutResponse(String rawResponse, PayPalPaymentRequest paymentRequest) {
        PayPalPaymentResponse response = new PayPalPaymentResponse();
        response.setTransactionType(paymentRequest.getTransactionType());
        response.setMethodType(paymentRequest.getMethodType());
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
        try {
            int keyBegin = resp.indexOf(valueName);
            if (keyBegin >= 0) {
                int tokenBegin = keyBegin + valueName.length() + 1;
                int tokenEnd = resp.indexOf('&', tokenBegin);
                if (tokenEnd < 0) {
                    tokenEnd = resp.length();
                }
                return URLDecoder.decode(resp.substring(tokenBegin, tokenEnd), "UTF-8");
            }
            return null;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
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
