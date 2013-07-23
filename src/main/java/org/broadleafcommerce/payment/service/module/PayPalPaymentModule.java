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

package org.broadleafcommerce.payment.service.module;

import org.broadleafcommerce.common.currency.util.BroadleafCurrencyUtils;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.time.SystemTime;
import org.broadleafcommerce.core.order.domain.FulfillmentGroup;
import org.broadleafcommerce.core.payment.domain.AmountItem;
import org.broadleafcommerce.core.payment.domain.PaymentInfo;
import org.broadleafcommerce.core.payment.domain.PaymentResponseItem;
import org.broadleafcommerce.core.payment.service.PaymentContext;
import org.broadleafcommerce.core.payment.service.exception.PaymentException;
import org.broadleafcommerce.core.payment.service.module.AbstractModule;
import org.broadleafcommerce.core.payment.service.type.PaymentInfoType;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalPaymentService;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalErrorResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.message.details.PayPalDetailsRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.details.PayPalDetailsResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalItemRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalPaymentRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalPaymentResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalShippingRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalSummaryRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalMethodType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalRefundType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalTransactionType;
import org.springframework.util.Assert;

/**
 * 
 * @author jfischer
 *
 */
public class PayPalPaymentModule extends AbstractModule {

    protected PayPalPaymentService payPalPaymentService;

    public PayPalDetailsResponse getExpressCheckoutDetails(PayPalDetailsRequest request) throws PaymentException {
        PayPalDetailsResponse response;
        try {
            response = (PayPalDetailsResponse) payPalPaymentService.process(request);
        } catch (org.broadleafcommerce.common.vendor.service.exception.PaymentException e) {
            throw new PaymentException(e);
        }

        return response;
    }

    @Override
    public Boolean isValidCandidate(PaymentInfoType paymentType) {
        return PaymentInfoType.PAYPAL.equals(paymentType);
    }

    @Override
    public PaymentResponseItem processReverseAuthorize(PaymentContext paymentContext, Money amountToReverseAuthorize, PaymentResponseItem responseItem) throws PaymentException {
        //void authorization from DoVoid
        PayPalPaymentRequest request = buildBasicRequest(paymentContext, PayPalTransactionType.REVERSEAUTHORIZE);
        request.setMethodType(PayPalMethodType.VOID);
        request.setCurrency(paymentContext.getPaymentInfo().getAmount().getCurrency().getCurrencyCode());

        Assert.isTrue(paymentContext.getPaymentInfo().getAdditionalFields().get(MessageConstants.TRANSACTIONID) != null, "The TRANSACTIONID value must be defined as an additional field in the PaymentInfo instance passed in.");
        request.setTransactionID(paymentContext.getPaymentInfo().getAdditionalFields().get(MessageConstants.TRANSACTIONID));

        PayPalPaymentResponse response;
        try {
            response = (PayPalPaymentResponse) payPalPaymentService.process(request);
        } catch (org.broadleafcommerce.common.vendor.service.exception.PaymentException e) {
            throw new PaymentException(e);
        }

        buildBasicResponse(response, responseItem);
        setDecisionInformation(response, responseItem);
        responseItem.setTransactionAmount(paymentContext.getPaymentInfo().getAmount());
        responseItem.setCurrency(paymentContext.getPaymentInfo().getOrder().getCurrency());
        return responseItem;
    }

    @Override
    public PaymentResponseItem processAuthorize(PaymentContext paymentContext, Money amountToAuthorize, PaymentResponseItem responseItem) throws PaymentException {
        //authorize from SetExpressCheckout "Authorization"
        return commonAuthorizeOrSale(paymentContext, PayPalTransactionType.AUTHORIZE, responseItem);
    }

    @Override
    public PaymentResponseItem processDebit(PaymentContext paymentContext, Money amountToDebit, PaymentResponseItem responseItem) throws PaymentException {
        //PayPal Capture
        PayPalPaymentRequest request = buildBasicRequest(paymentContext, PayPalTransactionType.CAPTURE);
        request.setMethodType(PayPalMethodType.CAPTURE);
        request.setCurrency(paymentContext.getPaymentInfo().getAmount().getCurrency().getCurrencyCode());

        Assert.isTrue(paymentContext.getPaymentInfo().getAdditionalFields().get(MessageConstants.TRANSACTIONID) != null, "The TRANSACTIONID value must be defined as an additional field in the PaymentInfo instance passed in.");
        request.setTransactionID(paymentContext.getPaymentInfo().getAdditionalFields().get(MessageConstants.TRANSACTIONID));

        PayPalPaymentResponse response;
        try {
            response = (PayPalPaymentResponse) payPalPaymentService.process(request);
        } catch (org.broadleafcommerce.common.vendor.service.exception.PaymentException e) {
            throw new PaymentException(e);
        }

        buildBasicResponse(response, responseItem);
        setDecisionInformation(response, responseItem);
        responseItem.setTransactionAmount(paymentContext.getPaymentInfo().getAmount());
        responseItem.setCurrency(paymentContext.getPaymentInfo().getOrder().getCurrency());
        return responseItem;
    }

    @Override
    public PaymentResponseItem processAuthorizeAndDebit(PaymentContext paymentContext, Money amountToDebit, PaymentResponseItem responseItem) throws PaymentException {
        return commonAuthorizeOrSale(paymentContext, PayPalTransactionType.AUTHORIZEANDCAPTURE, responseItem);
    }

    @Override
    public PaymentResponseItem processCredit(PaymentContext paymentContext, Money amountToCredit, PaymentResponseItem responseItem) throws PaymentException {
        //PayPal Refund
        PayPalPaymentRequest request = buildBasicRequest(paymentContext, PayPalTransactionType.CREDIT);
        request.setMethodType(PayPalMethodType.REFUND);
        request.setCurrency(paymentContext.getPaymentInfo().getAmount().getCurrency().getCurrencyCode());

        Assert.isTrue(paymentContext.getPaymentInfo().getAdditionalFields().get(MessageConstants.REFUNDTYPE) != null, "The REFUNDTYPE value must be defined as an additional field in the PaymentInfo instance passed in.");
        request.setRefundType(PayPalRefundType.getInstance(paymentContext.getPaymentInfo().getAdditionalFields().get(MessageConstants.REFUNDTYPE)));
        Assert.isTrue(paymentContext.getPaymentInfo().getAdditionalFields().get(MessageConstants.TRANSACTIONID) != null, "The TRANSACTIONID value must be defined as an additional field in the PaymentInfo instance passed in.");
        request.setTransactionID(paymentContext.getPaymentInfo().getAdditionalFields().get(MessageConstants.TRANSACTIONID));

        PayPalPaymentResponse response;
        try {
            response = (PayPalPaymentResponse) payPalPaymentService.process(request);
        } catch (org.broadleafcommerce.common.vendor.service.exception.PaymentException e) {
            throw new PaymentException(e);
        }

        buildBasicResponse(response, responseItem);
        setDecisionInformation(response, responseItem);
        setRefundInformation(response, responseItem);
        responseItem.setTransactionAmount(paymentContext.getPaymentInfo().getAmount());
        responseItem.setCurrency(paymentContext.getPaymentInfo().getOrder().getCurrency());
        return responseItem;
    }

    @Override
    public PaymentResponseItem processVoidPayment(PaymentContext paymentContext, Money amountToVoid, PaymentResponseItem responseItem) throws PaymentException {
        throw new PaymentException("The void method is not supported by this module");
    }

    @Override
    public PaymentResponseItem processBalance(PaymentContext paymentContext, PaymentResponseItem responseItem) throws PaymentException {
        throw new PaymentException("The balance method is not supported by this module");
    }

    @Override
    public PaymentResponseItem processPartialPayment(PaymentContext paymentContext, Money amountToDebit, PaymentResponseItem responseItem) throws PaymentException {
        throw new PaymentException("The partial payment method is not supported by this module"); 
    }

    protected PaymentResponseItem commonAuthorizeOrSale(PaymentContext paymentContext, PayPalTransactionType transactionType, PaymentResponseItem responseItem) throws PaymentException {
        PayPalPaymentRequest request = buildBasicRequest(paymentContext, transactionType);

        Assert.isTrue(paymentContext.getPaymentInfo().getAdditionalFields().containsKey(MessageConstants.SUBTOTAL), "Must specify a SUBTOTAL value on the additionalFields of the PaymentInfo instance.");
        Assert.isTrue(paymentContext.getPaymentInfo().getAdditionalFields().containsKey(MessageConstants.TOTALSHIPPING), "Must specify a TOTALSHIPPING value on the additionalFields of the PaymentInfo instance.");
        Assert.isTrue(paymentContext.getPaymentInfo().getAdditionalFields().containsKey(MessageConstants.TOTALTAX), "Must specify a TOTALTAX value on the additionalFields of the PaymentInfo instance.");
        PaymentInfo paymentInfo = paymentContext.getPaymentInfo();
        PayPalSummaryRequest summaryRequest = request.getSummaryRequest();
        summaryRequest.setSubTotal(new Money(paymentInfo.getAdditionalFields().get(MessageConstants.SUBTOTAL), paymentInfo.getAmount().getCurrency().getCurrencyCode()));
        summaryRequest.setTotalShipping(new Money(paymentInfo.getAdditionalFields().get(MessageConstants.TOTALSHIPPING), paymentInfo.getAmount().getCurrency().getCurrencyCode()));
        summaryRequest.setTotalTax(new Money(paymentInfo.getAdditionalFields().get(MessageConstants.TOTALTAX), paymentInfo.getAmount().getCurrency().getCurrencyCode()));

        String token = paymentContext.getPaymentInfo().getAdditionalFields().get(MessageConstants.TOKEN);
        if (token == null) {
            if (PayPalTransactionType.AUTHORIZE.equals(transactionType)) {
                request.setMethodType(PayPalMethodType.AUTHORIZATION);
            } else {
                request.setMethodType(PayPalMethodType.CHECKOUT);
            }
        } else {
            request.setMethodType(PayPalMethodType.PROCESS);
            if (PayPalTransactionType.AUTHORIZE.equals(transactionType)) {
                request.setSecondaryMethodType(PayPalMethodType.AUTHORIZATION);
            } else {
                request.setSecondaryMethodType(PayPalMethodType.CHECKOUT);
            }
            request.setPayerID(paymentContext.getPaymentInfo().getAdditionalFields().get(MessageConstants.PAYERID));
            request.setToken(token);
        }
        request.setCurrency(paymentContext.getPaymentInfo().getAmount().getCurrency().getCurrencyCode());
        for(AmountItem amountItem : paymentContext.getPaymentInfo().getAmountItems()) {
            PayPalItemRequest itemRequest = new PayPalItemRequest();
            itemRequest.setDescription(amountItem.getDescription());
            itemRequest.setShortDescription(amountItem.getShortDescription());
            itemRequest.setQuantity(amountItem.getQuantity());
            itemRequest.setUnitPrice(BroadleafCurrencyUtils.getMoney(amountItem.getUnitPrice(),paymentContext.getPaymentInfo().getOrder().getCurrency()));
            itemRequest.setSystemId(amountItem.getSystemId());
            request.getItemRequests().add(itemRequest);
        }

        for (FulfillmentGroup fulfillmentGroup : paymentContext.getPaymentInfo().getOrder().getFulfillmentGroups()) {
            if (fulfillmentGroup.getAddress() != null) {
                PayPalShippingRequest shippingRequest = new PayPalShippingRequest();
                shippingRequest.setShipToName(fulfillmentGroup.getAddress().getFirstName() + " " + fulfillmentGroup.getAddress().getLastName());
                shippingRequest.setShipToStreet(fulfillmentGroup.getAddress().getAddressLine1());
                shippingRequest.setShipToStreet2(fulfillmentGroup.getAddress().getAddressLine2());
                shippingRequest.setShipToCity(fulfillmentGroup.getAddress().getCity());
                if (fulfillmentGroup.getAddress().getState() != null) {
                    shippingRequest.setShipToState(fulfillmentGroup.getAddress().getState().getAbbreviation());
                }
                shippingRequest.setShipToZip(fulfillmentGroup.getAddress().getPostalCode());
                if (fulfillmentGroup.getAddress().getCountry() != null) {
                    shippingRequest.setShipToCountryCode(fulfillmentGroup.getAddress().getCountry().getAbbreviation());
                }
                shippingRequest.setShipToPhoneNum(fulfillmentGroup.getAddress().getPrimaryPhone());
                request.getShippingRequests().add(shippingRequest);
            }
        }

        PayPalPaymentResponse response;
        try {
            response = (PayPalPaymentResponse) payPalPaymentService.process(request);
        } catch (org.broadleafcommerce.common.vendor.service.exception.PaymentException e) {
            throw new PaymentException(e);
        }

        buildBasicResponse(response, responseItem);
        if(PayPalMethodType.PROCESS.equals(request.getMethodType())){
            setDecisionInformation(response, responseItem);
        } else if (PayPalMethodType.CHECKOUT.equals(request.getMethodType()) || PayPalMethodType.AUTHORIZATION.equals(request.getMethodType())) {
            responseItem.getAdditionalFields().put(MessageConstants.REDIRECTURL, response.getUserRedirectUrl());
        }
        responseItem.setTransactionAmount(paymentContext.getPaymentInfo().getAmount());
        responseItem.setCurrency(paymentContext.getPaymentInfo().getOrder().getCurrency());
        return responseItem;
    }

    protected void setDecisionInformation(PayPalPaymentResponse response, PaymentResponseItem responseItem) {
        responseItem.setTransactionId(response.getPaymentInfo().getTransactionId());
        if (response.getPaymentInfo().getParentTransactionId() != null) {
            responseItem.getAdditionalFields().put(MessageConstants.PARENTTRANSACTIONID, response.getPaymentInfo().getParentTransactionId());
        }
        if (response.getPaymentInfo().getReceiptId() != null) {
            responseItem.getAdditionalFields().put(MessageConstants.RECEIPTID, response.getPaymentInfo().getReceiptId());
        }
        if (response.getPaymentInfo().getExchangeRate() != null) {
            responseItem.getAdditionalFields().put(MessageConstants.EXCHANGERATE, response.getPaymentInfo().getExchangeRate().toString());
        }
        if (response.getPaymentInfo().getPaymentStatusType() != null) {
            responseItem.getAdditionalFields().put(MessageConstants.PAYMENTSTATUS, response.getPaymentInfo().getPaymentStatusType().getType());
        }
        if (response.getPaymentInfo().getPendingReasonType() != null) {
            responseItem.getAdditionalFields().put(MessageConstants.PENDINGREASON, response.getPaymentInfo().getPendingReasonType().getType());
        }
        if (response.getPaymentInfo().getReasonCodeType() != null) {
            responseItem.getAdditionalFields().put(MessageConstants.REASONCODE, response.getPaymentInfo().getReasonCodeType().getType());
        }
        if (response.getPaymentInfo().getHoldDecisionType() != null) {
            responseItem.getAdditionalFields().put(MessageConstants.HOLDDECISION, response.getPaymentInfo().getHoldDecisionType().getType());
        }
        if (response.getPaymentInfo().getFeeAmount() != null) {
            responseItem.getAdditionalFields().put(MessageConstants.FEEAMOUNT, response.getPaymentInfo().getFeeAmount().toString());
        }
        if (response.getPaymentInfo().getSettleAmount() != null) {
            responseItem.getAdditionalFields().put(MessageConstants.SETTLEAMOUNT, response.getPaymentInfo().getSettleAmount().toString());
        }
        if (response.getPaymentInfo().getTaxAmount() != null) {
            responseItem.getAdditionalFields().put(MessageConstants.TAXAMOUNT, response.getPaymentInfo().getTaxAmount().toString());
        }
    }
    
    protected void setRefundInformation(PayPalPaymentResponse response, PaymentResponseItem responseItem) {
        if (response.getRefundInfo().getRefundTransactionId() != null) {
            responseItem.getAdditionalFields().put(MessageConstants.REFUNDTRANSACTIONID, response.getRefundInfo().getRefundTransactionId());
        }
        if (response.getRefundInfo().getFeeRefundAmount() != null) {
            responseItem.getAdditionalFields().put(MessageConstants.FEEREFUNDAMT, response.getRefundInfo().getFeeRefundAmount().toString());
        }
        if (response.getRefundInfo().getGrossRefundAmount() != null) {
            responseItem.getAdditionalFields().put(MessageConstants.GROSSREFUNDAMT, response.getRefundInfo().getGrossRefundAmount().toString());
        }
        if (response.getRefundInfo().getNetRefundAmount() != null) {
            responseItem.getAdditionalFields().put(MessageConstants.NETREFUNDAMT, response.getRefundInfo().getNetRefundAmount().toString());
        }
        if (response.getRefundInfo().getTotalRefundAmount() != null) {
            responseItem.getAdditionalFields().put(MessageConstants.TOTALREFUNDEDAMT, response.getRefundInfo().getTotalRefundAmount().toString());
        }
        if (response.getRefundInfo().getRefundInfo() != null) {
            responseItem.getAdditionalFields().put(MessageConstants.REFUNDINFO, response.getRefundInfo().getRefundInfo());
        }
        if (response.getRefundInfo().getRefundStatusType() != null) {
            responseItem.getAdditionalFields().put(MessageConstants.REFUNDSTATUS, response.getRefundInfo().getRefundStatusType().getType());
        }
        if (response.getRefundInfo().getPendingReasonType() != null) {
            responseItem.getAdditionalFields().put(MessageConstants.PENDINGREASON, response.getRefundInfo().getPendingReasonType().getType());
        }
    }

    protected PayPalPaymentRequest buildBasicRequest(PaymentContext paymentContext, PayPalTransactionType transactionType) {
        PayPalPaymentRequest request = new PayPalPaymentRequest();
        request.setTransactionType(transactionType);
        Assert.isTrue(paymentContext.getPaymentInfo().getReferenceNumber().length() <= 127, "The reference number for the paypal request cannot be greater than 127 characters");
        request.setReferenceNumber(paymentContext.getPaymentInfo().getReferenceNumber());

        PaymentInfo paymentInfo = paymentContext.getPaymentInfo();
        PayPalSummaryRequest summaryRequest = new PayPalSummaryRequest();
        summaryRequest.setGrandTotal(paymentInfo.getAmount());
        request.setSummaryRequest(summaryRequest);

        return request;
    }

    protected PaymentResponseItem buildBasicResponse(PayPalPaymentResponse response, PaymentResponseItem baseResponseItem) {
        baseResponseItem.setTransactionTimestamp(SystemTime.asDate());
        baseResponseItem.setReferenceNumber(response.getResponseToken());
        baseResponseItem.setTransactionSuccess(response.isSuccessful());
        baseResponseItem.setAuthorizationCode(response.getAck());
        baseResponseItem.setMiddlewareResponseCode(response.getAck());
        baseResponseItem.setMiddlewareResponseText(response.getAck());
        int counter = 0;
        for (PayPalErrorResponse errorResponse : response.getErrorResponses()) {
            String errorCode = errorResponse.getErrorCode();
            if (counter == 0) {
                baseResponseItem.setMiddlewareResponseCode(errorCode);
                baseResponseItem.setMiddlewareResponseText(errorResponse.getLongMessage());
            }
            counter++;
            baseResponseItem.getAdditionalFields().put(MessageConstants.MODULEERRORCODE, errorCode);
            baseResponseItem.getAdditionalFields().put(MessageConstants.MODULEERRORSEVERITYCODE + "_" + errorCode, errorResponse.getSeverityCode());
            baseResponseItem.getAdditionalFields().put(MessageConstants.MODULEERRORLONGMESSAGE + "_" + errorCode, errorResponse.getLongMessage());
            baseResponseItem.getAdditionalFields().put(MessageConstants.MODULEERRORSHORTMESSAGE + "_" + errorCode, errorResponse.getShortMessage());
        }
        baseResponseItem.getAdditionalFields().putAll(response.getPassThroughErrors());

        return baseResponseItem;
    }

    public PayPalPaymentService getPayPalPaymentService() {
        return payPalPaymentService;
    }

    public void setPayPalPaymentService(PayPalPaymentService payPalPaymentService) {
        this.payPalPaymentService = payPalPaymentService;
    }

}
