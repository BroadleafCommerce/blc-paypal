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

package org.broadleafcommerce.payment.service.module;

import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.time.SystemTime;
import org.broadleafcommerce.core.payment.domain.AmountItem;
import org.broadleafcommerce.core.payment.domain.PaymentResponseItem;
import org.broadleafcommerce.core.payment.domain.PaymentResponseItemImpl;
import org.broadleafcommerce.core.payment.domain.TotalledPaymentInfo;
import org.broadleafcommerce.core.payment.service.PaymentContext;
import org.broadleafcommerce.core.payment.service.exception.PaymentException;
import org.broadleafcommerce.core.payment.service.module.PaymentModule;
import org.broadleafcommerce.core.payment.service.type.PaymentInfoType;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalPaymentService;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalErrorResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalItemRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalPaymentRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalPaymentResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalSummaryRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalMethodType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalTransactionType;
import org.springframework.util.Assert;

/**
 * 
 * @author jfischer
 *
 */
public class PayPalPaymentModule implements PaymentModule {

    protected PayPalPaymentService payPalPaymentService;

    @Override
    public PaymentResponseItem authorize(PaymentContext paymentContext) throws PaymentException {
        //authorize from SetExpressCheckout "Authorization"
        PayPalPaymentRequest request = new PayPalPaymentRequest();
        for(AmountItem amountItem : paymentContext.getPaymentInfo().getAmountItems()) {
            PayPalItemRequest itemRequest = new PayPalItemRequest();
            itemRequest.setDescription(amountItem.getDescription());
            itemRequest.setShortDescription(amountItem.getShortDescription());
            itemRequest.setQuantity(amountItem.getQuantity());
            itemRequest.setUnitPrice(new Money(amountItem.getUnitPrice()));
            itemRequest.setSystemId(amountItem.getSystemId());
            request.getItemRequests().add(itemRequest);
        }
        request.setTransactionType(PayPalTransactionType.AUTHORIZE);

        Assert.isTrue(paymentContext.getPaymentInfo().getAdditionalFields().containsKey(MessageConstants.PAYPALMETHODTYPE), "When using Broadleaf Commerce PayPal support, the additionalFields in the PaymentInfo instance must specify a key (PAYPALMETHODTYPE) and the appropriate value");
        request.setMethodType(PayPalMethodType.getInstance(paymentContext.getPaymentInfo().getAdditionalFields().get(MessageConstants.PAYPALMETHODTYPE)));

        Assert.isTrue(TotalledPaymentInfo.class.isAssignableFrom(paymentContext.getPaymentInfo().getClass()), "When using Broadleaf Commerce PayPal support, all PaymentInfo instances must be instances of TotalledPaymentInfo");
        TotalledPaymentInfo totalledPaymentInfo = (TotalledPaymentInfo) paymentContext.getPaymentInfo();
        PayPalSummaryRequest summaryRequest = new PayPalSummaryRequest();
        summaryRequest.setGrandTotal(totalledPaymentInfo.getAmount());
        summaryRequest.setShippingDiscount(totalledPaymentInfo.getShippingDiscount());
        summaryRequest.setSubTotal(totalledPaymentInfo.getSubTotal());
        summaryRequest.setTotalShipping(totalledPaymentInfo.getTotalShipping());
        summaryRequest.setTotalTax(totalledPaymentInfo.getTotalTax());
        request.setSummaryRequest(summaryRequest);

        PayPalPaymentResponse response;
        try {
            response = payPalPaymentService.process(request);
        } catch (org.broadleafcommerce.common.vendor.service.exception.PaymentException e) {
            throw new PaymentException(e);
        }

        PaymentResponseItem responseItem = buildBasicResponse(response);
        responseItem.setAmountPaid(paymentContext.getPaymentInfo().getAmount());

        return responseItem;
    }

    @Override
    public PaymentResponseItem reverseAuthorize(PaymentContext paymentContext) throws PaymentException {
        //void authorization from DoVoid
        PayPalPaymentRequest request = new PayPalPaymentRequest();

        request.setTransactionType(PayPalTransactionType.REVERSEAUTHORIZE);
        Assert.isTrue(paymentContext.getPaymentInfo().getAdditionalFields().containsKey(MessageConstants.PAYPALMETHODTYPE), "When using Broadleaf Commerce PayPal support, the additionalFields in the PaymentInfo instance must specify a key (PAYPALMETHODTYPE) and the appropriate value");
        request.setMethodType(PayPalMethodType.getInstance(paymentContext.getPaymentInfo().getAdditionalFields().get(MessageConstants.PAYPALMETHODTYPE)));

        Assert.isTrue(TotalledPaymentInfo.class.isAssignableFrom(paymentContext.getPaymentInfo().getClass()), "When using Broadleaf Commerce PayPal support, all PaymentInfo instances must be instances of TotalledPaymentInfo");
        TotalledPaymentInfo totalledPaymentInfo = (TotalledPaymentInfo) paymentContext.getPaymentInfo();
        PayPalSummaryRequest summaryRequest = new PayPalSummaryRequest();
        summaryRequest.setGrandTotal(totalledPaymentInfo.getAmount());
        request.setSummaryRequest(summaryRequest);

        PayPalPaymentResponse response;
        try {
            response = payPalPaymentService.process(request);
        } catch (org.broadleafcommerce.common.vendor.service.exception.PaymentException e) {
            throw new PaymentException(e);
        }

        PaymentResponseItem responseItem = buildBasicResponse(response);
        responseItem.setAmountPaid(paymentContext.getPaymentInfo().getAmount());

        return responseItem;
    }

    @Override
    public PaymentResponseItem debit(PaymentContext paymentContext) throws PaymentException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PaymentResponseItem authorizeAndDebit(PaymentContext paymentContext) throws PaymentException {
        //TODO I'm sure as the other methods are flushed out, a lot of this code will be re-usable. Some refactoring of common pieces is likely called for.
        PayPalPaymentRequest request = new PayPalPaymentRequest();
        for(AmountItem amountItem : paymentContext.getPaymentInfo().getAmountItems()) {
            PayPalItemRequest itemRequest = new PayPalItemRequest();
            itemRequest.setDescription(amountItem.getDescription());
            itemRequest.setShortDescription(amountItem.getShortDescription());
            itemRequest.setQuantity(amountItem.getQuantity());
            itemRequest.setUnitPrice(new Money(amountItem.getUnitPrice()));
            itemRequest.setSystemId(amountItem.getSystemId());
            request.getItemRequests().add(itemRequest);
        }
        request.setTransactionType(PayPalTransactionType.AUTHORIZEANDCAPTURE);

        Assert.isTrue(paymentContext.getPaymentInfo().getAdditionalFields().containsKey(MessageConstants.PAYPALMETHODTYPE), "When using Broadleaf Commerce PayPal support, the additionalFields in the PaymentInfo instance must specify a key (PAYPALMETHODTYPE) and the appropriate value");
        request.setMethodType(PayPalMethodType.getInstance(paymentContext.getPaymentInfo().getAdditionalFields().get(MessageConstants.PAYPALMETHODTYPE)));

        Assert.isTrue(TotalledPaymentInfo.class.isAssignableFrom(paymentContext.getPaymentInfo().getClass()), "When using Broadleaf Commerce PayPal support, all PaymentInfo instances must be instances of TotalledPaymentInfo");
        TotalledPaymentInfo totalledPaymentInfo = (TotalledPaymentInfo) paymentContext.getPaymentInfo();
        PayPalSummaryRequest summaryRequest = new PayPalSummaryRequest();
        summaryRequest.setGrandTotal(totalledPaymentInfo.getAmount());
        summaryRequest.setShippingDiscount(totalledPaymentInfo.getShippingDiscount());
        summaryRequest.setSubTotal(totalledPaymentInfo.getSubTotal());
        summaryRequest.setTotalShipping(totalledPaymentInfo.getTotalShipping());
        summaryRequest.setTotalTax(totalledPaymentInfo.getTotalTax());
        request.setSummaryRequest(summaryRequest);
        if(request.getMethodType().equals(PayPalMethodType.PROCESS)){
            request.setPayerID(paymentContext.getPaymentInfo().getAdditionalFields().get("payerID"));
            request.setToken(paymentContext.getPaymentInfo().getAdditionalFields().get("token"));
            request.setOrderNumber(paymentContext.getPaymentInfo().getAdditionalFields().get("orderNumber"));
        }
        PayPalPaymentResponse response;
        try {
            response = payPalPaymentService.process(request);
        } catch (org.broadleafcommerce.common.vendor.service.exception.PaymentException e) {
            throw new PaymentException(e);
        }
        
        PaymentResponseItem responseItem = buildBasicResponse(response);
        responseItem.setAmountPaid(paymentContext.getPaymentInfo().getAmount());

        return responseItem;
    }
    
    protected PaymentResponseItem buildBasicResponse(PayPalPaymentResponse response) {
        PaymentResponseItem responseItem = new PaymentResponseItemImpl();
        responseItem.setTransactionTimestamp(SystemTime.asDate());
        responseItem.setMiddlewareResponseCode(response.getAck());
        responseItem.setMiddlewareResponseText(response.getAck());
        responseItem.setReferenceNumber(response.getResponseToken());
        responseItem.setTransactionId(response.getCorrelationId());
        responseItem.setTransactionSuccess(response.isSuccessful());
        responseItem.setAuthorizationCode(response.getAck());
        for (PayPalErrorResponse errorResponse : response.getErrorResponses()) {
            String errorCode = errorResponse.getErrorCode();
            responseItem.getAdditionalFields().put(MessageConstants.ERRORCODE, errorCode);
            responseItem.getAdditionalFields().put(MessageConstants.SEVERITYCODE + "_" + errorCode, errorResponse.getSeverityCode());
            responseItem.getAdditionalFields().put(MessageConstants.LONGMESSAGE + "_" + errorCode, errorResponse.getLongMessage());
            responseItem.getAdditionalFields().put(MessageConstants.SHORTMESSAGE + "_" + errorCode, errorResponse.getShortMessage());
        }
        responseItem.getAdditionalFields().putAll(response.getPassThroughErrors());
        responseItem.getAdditionalFields().put(MessageConstants.REDIRECTURL, response.getUserRedirectUrl());
        
        return responseItem;
    }

    @Override
    public PaymentResponseItem credit(PaymentContext paymentContext) throws PaymentException {
        //PayPal Refund
        PayPalPaymentRequest request = new PayPalPaymentRequest();
        request.setTransactionType(PayPalTransactionType.CREDIT);

        Assert.isTrue(paymentContext.getPaymentInfo().getAdditionalFields().containsKey(MessageConstants.PAYPALMETHODTYPE), "When using Broadleaf Commerce PayPal support, the additionalFields in the PaymentInfo instance must specify a key (PAYPALMETHODTYPE) and the appropriate value");
        request.setMethodType(PayPalMethodType.getInstance(paymentContext.getPaymentInfo().getAdditionalFields().get(MessageConstants.PAYPALMETHODTYPE)));

        Assert.isTrue(TotalledPaymentInfo.class.isAssignableFrom(paymentContext.getPaymentInfo().getClass()), "When using Broadleaf Commerce PayPal support, all PaymentInfo instances must be instances of TotalledPaymentInfo");
        TotalledPaymentInfo totalledPaymentInfo = (TotalledPaymentInfo) paymentContext.getPaymentInfo();
        PayPalSummaryRequest summaryRequest = new PayPalSummaryRequest();
        summaryRequest.setGrandTotal(totalledPaymentInfo.getAmount());
        request.setSummaryRequest(summaryRequest);

        PayPalPaymentResponse response;
        try {
            response = payPalPaymentService.process(request);
        } catch (org.broadleafcommerce.common.vendor.service.exception.PaymentException e) {
            throw new PaymentException(e);
        }

        PaymentResponseItem responseItem = buildBasicResponse(response);
        responseItem.setAmountPaid(paymentContext.getPaymentInfo().getAmount());

        return responseItem;
    }

    @Override
    public PaymentResponseItem voidPayment(PaymentContext paymentContext) throws PaymentException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PaymentResponseItem balance(PaymentContext paymentContext) throws PaymentException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Boolean isValidCandidate(PaymentInfoType paymentType) {
        return paymentType == PaymentInfoType.PAYPAL;
    }

    public PayPalPaymentService getPayPalPaymentService() {
        return payPalPaymentService;
    }

    public void setPayPalPaymentService(PayPalPaymentService payPalPaymentService) {
        this.payPalPaymentService = payPalPaymentService;
    }
}
