/*
 * Copyright 2008-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.payment.service.gateway;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.payment.dto.LineItemDTO;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.AbstractExternalPaymentGatewayCall;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalRequestGenerator;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalResponseGenerator;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalItemRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalPaymentRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalPaymentResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalShippingRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalSummaryRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalMethodType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalTransactionType;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

/**
 * @author Elbert Bautista (elbertbautista)
 */
public abstract class AbstractPayPalExpressService extends AbstractExternalPaymentGatewayCall<PayPalRequest, PayPalResponse> {

    private static final Log LOG = LogFactory.getLog(AbstractPayPalExpressService.class);

    @Resource(name = "blPayPalExpressConfigurationService")
    protected PayPalExpressConfigurationService configurationService;

    @Resource(name = "blPayPalExpressRequestGenerator")
    protected PayPalRequestGenerator requestGenerator;

    @Resource(name = "blPayPalExpressResponseGenerator")
    protected PayPalResponseGenerator responseGenerator;

    @Override
    public Integer getFailureReportingThreshold() {
        return configurationService.getFailureReportingThreshold();
    }

    @Override
    public PayPalResponse communicateWithVendor(PayPalRequest paymentRequest) throws Exception {
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(getServerUrl());
        List<NameValuePair> nvps = requestGenerator.buildRequest(paymentRequest);
        postMethod.setRequestBody(nvps.toArray(new NameValuePair[nvps.size()]));
        httpClient.executeMethod(postMethod);
        String responseString = postMethod.getResponseBodyAsString();

        return responseGenerator.buildResponse(responseString, paymentRequest);
    }

    public String getServerUrl() {
        return configurationService.getServerUrl();
    }

    protected PayPalPaymentRequest buildBasicRequest(PaymentRequestDTO requestDTO, PayPalTransactionType transactionType) {
        Assert.isTrue(requestDTO.getOrderId() != null, "The Order ID for the paypal request cannot be null");
        if (requestDTO.getOrderId() != null) {
            Assert.isTrue(requestDTO.getOrderId().length() <= 127, "The reference number for the paypal request cannot be greater than 127 characters");
        }

        PayPalPaymentRequest request = new PayPalPaymentRequest();
        request.setTransactionType(transactionType);
        request.setReferenceNumber(requestDTO.getOrderId());
        request.setCurrency(requestDTO.getOrderCurrencyCode());

        PayPalSummaryRequest summaryRequest = new PayPalSummaryRequest();
        summaryRequest.setGrandTotal(new Money(requestDTO.getOrderTotal(), requestDTO.getOrderCurrencyCode()));
        request.setSummaryRequest(summaryRequest);

        return request;
    }

    protected PaymentResponseDTO commonAuthorizeOrSale(PaymentRequestDTO requestDTO, PayPalTransactionType transactionType,
                                                       String token, String payerId) throws PaymentException {

        PayPalPaymentRequest request = buildBasicRequest(requestDTO, transactionType);

        Assert.isTrue(requestDTO.getOrderSubtotal() != null, "Must specify an Order Subtotal value on the PaymentRequestDTO");
        Assert.isTrue(requestDTO.getShippingTotal() != null, "Must specify a Shipping Total value on the PaymentRequestDTO");
        Assert.isTrue(requestDTO.getTaxTotal() != null, "Must specify a Tax Total value on the PaymentRequestDTO");

        request.getSummaryRequest().setSubTotal(new Money(requestDTO.getOrderSubtotal(), requestDTO.getOrderCurrencyCode()));
        request.getSummaryRequest().setTotalShipping(new Money(requestDTO.getShippingTotal(), requestDTO.getOrderCurrencyCode()));
        request.getSummaryRequest().setTotalTax(new Money(requestDTO.getTaxTotal(), requestDTO.getOrderCurrencyCode()));

        if (token == null && payerId == null) {
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

            request.setPayerID(payerId);
            request.setToken(token);
        }

        for(LineItemDTO lineItem : requestDTO.getLineItems()) {
            PayPalItemRequest itemRequest = new PayPalItemRequest();
            itemRequest.setDescription(lineItem.getDescription());
            itemRequest.setShortDescription(lineItem.getShortDescription());
            itemRequest.setQuantity(Long.parseLong(lineItem.getQuantity()));
            itemRequest.setUnitPrice(new Money(lineItem.getAmount(), requestDTO.getOrderCurrencyCode()));
            itemRequest.setSystemId(lineItem.getSystemId());
            request.getItemRequests().add(itemRequest);
        }

        PayPalShippingRequest shippingRequest = new PayPalShippingRequest();
        shippingRequest.setShipToName(requestDTO.getShipToFirstName() + " " + requestDTO.getShipToLastName());
        shippingRequest.setShipToStreet(requestDTO.getShipToAddressLine1());
        shippingRequest.setShipToStreet2(requestDTO.getShipToAddressLine2());
        shippingRequest.setShipToCity(requestDTO.getShipToCityLocality());
        shippingRequest.setShipToState(requestDTO.getShipToStateRegion());
        shippingRequest.setShipToZip(requestDTO.getShipToPostalCode());
        shippingRequest.setShipToCountryCode(requestDTO.getShipToCountryCode());
        shippingRequest.setShipToPhoneNum(requestDTO.getShipToPhone());
        request.getShippingRequests().add(shippingRequest);

        PaymentResponseDTO responseDTO = new PaymentResponseDTO();
        PayPalPaymentResponse response;

        response = (PayPalPaymentResponse) process(request);
        setRawResponse(response, responseDTO);
        responseDTO.setSuccessful(response.isSuccessful());
        if(PayPalMethodType.PROCESS.equals(request.getMethodType())){
            setDecisionInformation(response, responseDTO);
        } else if (PayPalMethodType.CHECKOUT.equals(request.getMethodType()) || PayPalMethodType.AUTHORIZATION.equals(request.getMethodType())) {
            responseDTO.getResponseMap().put(MessageConstants.REDIRECTURL, response.getUserRedirectUrl());
        }

        return responseDTO;
    }

    protected void setRawResponse(PayPalResponse response, PaymentResponseDTO responseDTO) {
        try {
            responseDTO.setRawResponse(URLDecoder.decode(response.getRawResponse(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setDecisionInformation(PayPalPaymentResponse response, PaymentResponseDTO responseDTO) {
        responseDTO.getResponseMap().put(MessageConstants.TRANSACTIONID, response.getPaymentInfo().getTransactionId());

        if (response.getPaymentInfo().getTotalAmount() != null) {
            responseDTO.setAmount(response.getPaymentInfo().getTotalAmount());
        }

        if (response.getPaymentInfo().getParentTransactionId() != null) {
            responseDTO.getResponseMap().put(MessageConstants.PARENTTRANSACTIONID, response.getPaymentInfo().getParentTransactionId());
        }
        if (response.getPaymentInfo().getReceiptId() != null) {
            responseDTO.getResponseMap().put(MessageConstants.RECEIPTID, response.getPaymentInfo().getReceiptId());
        }
        if (response.getPaymentInfo().getExchangeRate() != null) {
            responseDTO.getResponseMap().put(MessageConstants.EXCHANGERATE, response.getPaymentInfo().getExchangeRate().toString());
        }
        if (response.getPaymentInfo().getPaymentStatusType() != null) {
            responseDTO.getResponseMap().put(MessageConstants.PAYMENTSTATUS, response.getPaymentInfo().getPaymentStatusType().getType());
        }
        if (response.getPaymentInfo().getPendingReasonType() != null) {
            responseDTO.getResponseMap().put(MessageConstants.PENDINGREASON, response.getPaymentInfo().getPendingReasonType().getType());
        }
        if (response.getPaymentInfo().getReasonCodeType() != null) {
            responseDTO.getResponseMap().put(MessageConstants.REASONCODE, response.getPaymentInfo().getReasonCodeType().getType());
        }
        if (response.getPaymentInfo().getHoldDecisionType() != null) {
            responseDTO.getResponseMap().put(MessageConstants.HOLDDECISION, response.getPaymentInfo().getHoldDecisionType().getType());
        }
        if (response.getPaymentInfo().getFeeAmount() != null) {
            responseDTO.getResponseMap().put(MessageConstants.FEEAMOUNT, response.getPaymentInfo().getFeeAmount().toString());
        }
        if (response.getPaymentInfo().getSettleAmount() != null) {
            responseDTO.getResponseMap().put(MessageConstants.SETTLEAMOUNT, response.getPaymentInfo().getSettleAmount().toString());
        }
        if (response.getPaymentInfo().getTaxAmount() != null) {
            responseDTO.getResponseMap().put(MessageConstants.TAXAMOUNT, response.getPaymentInfo().getTaxAmount().toString());
        }
    }

    protected void setRefundInformation(PayPalPaymentResponse response, PaymentResponseDTO responseDTO) {
        if (response.getRefundInfo().getRefundTransactionId() != null) {
            responseDTO.getResponseMap().put(MessageConstants.REFUNDTRANSACTIONID, response.getRefundInfo().getRefundTransactionId());
        }
        if (response.getRefundInfo().getFeeRefundAmount() != null) {
            responseDTO.getResponseMap().put(MessageConstants.FEEREFUNDAMT, response.getRefundInfo().getFeeRefundAmount().toString());
        }
        if (response.getRefundInfo().getGrossRefundAmount() != null) {
            responseDTO.getResponseMap().put(MessageConstants.GROSSREFUNDAMT, response.getRefundInfo().getGrossRefundAmount().toString());
        }
        if (response.getRefundInfo().getNetRefundAmount() != null) {
            responseDTO.getResponseMap().put(MessageConstants.NETREFUNDAMT, response.getRefundInfo().getNetRefundAmount().toString());
        }
        if (response.getRefundInfo().getTotalRefundAmount() != null) {
            responseDTO.getResponseMap().put(MessageConstants.TOTALREFUNDEDAMT, response.getRefundInfo().getTotalRefundAmount().toString());
        }
        if (response.getRefundInfo().getRefundInfo() != null) {
            responseDTO.getResponseMap().put(MessageConstants.REFUNDINFO, response.getRefundInfo().getRefundInfo());
        }
        if (response.getRefundInfo().getRefundStatusType() != null) {
            responseDTO.getResponseMap().put(MessageConstants.REFUNDSTATUS, response.getRefundInfo().getRefundStatusType().getType());
        }
        if (response.getRefundInfo().getPendingReasonType() != null) {
            responseDTO.getResponseMap().put(MessageConstants.PENDINGREASON, response.getRefundInfo().getPendingReasonType().getType());
        }
    }

}
