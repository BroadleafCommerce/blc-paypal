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
import org.broadleafcommerce.common.payment.PaymentTransactionType;
import org.broadleafcommerce.common.payment.PaymentType;
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
import org.broadleafcommerce.vendor.paypal.service.payment.message.details.PayPalDetailsResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.message.details.PayPalPayerAddress;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalItemRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalPaymentRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalPaymentResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalShippingRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalSummaryRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalMethodType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalTransactionType;
import org.springframework.util.Assert;

import javax.annotation.Resource;
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
        Assert.isTrue(requestDTO.getTransactionTotal() != null, "The Transaction Total for the paypal request cannot be null");

        if (requestDTO.getOrderId() != null) {
            Assert.isTrue(requestDTO.getOrderId().length() <= 127, "The reference number for the paypal request cannot be greater than 127 characters");
        }

        PayPalPaymentRequest request = new PayPalPaymentRequest();
        request.setTransactionType(transactionType);
        request.setOrderId(requestDTO.getOrderId());
        request.setCurrency(requestDTO.getOrderCurrencyCode());
        request.setCompleteCheckoutOnCallback(requestDTO.isCompleteCheckoutOnCallback());

        PayPalSummaryRequest summaryRequest = new PayPalSummaryRequest();
        summaryRequest.setGrandTotal(new Money(requestDTO.getTransactionTotal(), requestDTO.getOrderCurrencyCode()));
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

        if (requestDTO.shipToPopulated()) {
            PayPalShippingRequest shippingRequest = new PayPalShippingRequest();
            shippingRequest.setShipToName(requestDTO.getShipTo().getAddressFirstName() + " " +
                    requestDTO.getShipTo().getAddressLastName());
            shippingRequest.setShipToStreet(requestDTO.getShipTo().getAddressLine1());
            shippingRequest.setShipToStreet2(requestDTO.getShipTo().getAddressLine2());
            shippingRequest.setShipToCity(requestDTO.getShipTo().getAddressCityLocality());
            shippingRequest.setShipToState(requestDTO.getShipTo().getAddressStateRegion());
            shippingRequest.setShipToZip(requestDTO.getShipTo().getAddressPostalCode());
            shippingRequest.setShipToCountryCode(requestDTO.getShipTo().getAddressCountryCode());
            shippingRequest.setShipToPhoneNum(requestDTO.getShipTo().getAddressPhone());
            request.getShippingRequests().add(shippingRequest);
        }

        PayPalPaymentResponse response = (PayPalPaymentResponse) process(request);
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT);

        setCommonPaymentResponse(response, responseDTO);
        responseDTO.successful(response.isSuccessful());

        if (PayPalTransactionType.AUTHORIZE.equals(transactionType)) {
            responseDTO.paymentTransactionType(PaymentTransactionType.AUTHORIZE);
        } else if (PayPalTransactionType.AUTHORIZEANDCAPTURE.equals(transactionType)) {
            responseDTO.paymentTransactionType(PaymentTransactionType.AUTHORIZE_AND_CAPTURE);
        }

        if(PayPalMethodType.PROCESS.equals(request.getMethodType())){
            setDecisionInformation(response, responseDTO);
        } else if (PayPalMethodType.CHECKOUT.equals(request.getMethodType()) || PayPalMethodType.AUTHORIZATION.equals(request.getMethodType())) {
            responseDTO.responseMap(MessageConstants.REDIRECTURL, response.getUserRedirectUrl());
        }

        return responseDTO;
    }

    protected void setCommonPaymentResponse(PayPalPaymentResponse response, PaymentResponseDTO responseDTO) {

        try {
            responseDTO.rawResponse(URLDecoder.decode(response.getRawResponse(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        responseDTO.responseMap(MessageConstants.TOKEN, response.getResponseToken());
        responseDTO.responseMap(MessageConstants.CORRELATIONID, response.getCorrelationId());
    }

    protected void setCommonDetailsResponse(PayPalDetailsResponse response, PaymentResponseDTO responseDTO) {

        try {
            responseDTO.rawResponse(URLDecoder.decode(response.getRawResponse(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        if (response.getAddresses() != null && !response.getAddresses().isEmpty()) {
            PayPalPayerAddress payerAddress = response.getAddresses().get(0);
            responseDTO.shipTo()
                    .addressFirstName(payerAddress.getName())
                    .addressLine1(payerAddress.getStreet())
                    .addressLine2(payerAddress.getStreet2())
                    .addressCityLocality(payerAddress.getCity())
                    .addressStateRegion(payerAddress.getState())
                    .addressPostalCode(payerAddress.getZip())
                    .addressCountryCode(payerAddress.getCountryCode())
                    .addressPhone(payerAddress.getPhoneNumber())
                    .done();

            if (payerAddress.getAddressStatus()!= null) {
                    responseDTO.getShipTo().additionalFields(MessageConstants.ADDRESSSTATUS, payerAddress.getAddressStatus().getType());
            }
        }

        if (response.getPaymentDetails()!= null) {
            responseDTO.amount(response.getPaymentDetails().getAmount())
                    .orderId(response.getPaymentDetails().getOrderId())
                    .completeCheckoutOnCallback(response.getPaymentDetails().isCompleteCheckoutOnCallback())
                    .responseMap(MessageConstants.DETAILSPAYMENTALLOWEDMETHOD, response.getPaymentDetails().getPaymentMethod())
                    .responseMap(MessageConstants.DETAILSPAYMENTREQUESTID, response.getPaymentDetails().getPaymentRequestId())
                    .responseMap(MessageConstants.DETAILSPAYMENTTRANSACTIONID, response.getPaymentDetails().getTransactionId())
                    .responseMap(MessageConstants.DETAILSPAYMENTITEMTOTAL, response.getPaymentDetails().getItemTotal())
                    .responseMap(MessageConstants.DETAILSPAYMENTSHIPPINGDISCOUNT, response.getPaymentDetails().getShippingDiscount())
                    .responseMap(MessageConstants.DETAILSPAYMENTSHIPPINGTOTAL,response.getPaymentDetails().getShippingTotal())
                    .responseMap(MessageConstants.DETAILSPAYMENTTOTALTAX, response.getPaymentDetails().getTotalTax());
        }

        if (response.getCheckoutStatusType()!=null) {
            responseDTO.responseMap(MessageConstants.CHECKOUTSTATUS, response.getCheckoutStatusType().getType());
        }

        responseDTO.customer()
            .firstName(response.getPayerFirstName())
            .lastName(response.getPayerLastName())
            .companyName(response.getBusiness())
            .phone(response.getPhoneNumber())
            .email(response.getEmailAddress())
            .done()
        .responseMap(MessageConstants.TOKEN, response.getResponseToken())
        .responseMap(MessageConstants.PAYERID, response.getPayerId())
        .responseMap(MessageConstants.NOTE, response.getNote())
        .responseMap(MessageConstants.PAYPALADJUSTMENT, response.getPayPalAdjustment())
        .responseMap(MessageConstants.PAYERSTATUS, response.getPayerStatus());

    }

    protected void setDecisionInformation(PayPalPaymentResponse response, PaymentResponseDTO responseDTO) {
        responseDTO.responseMap(MessageConstants.TRANSACTIONID, response.getPaymentInfo().getTransactionId());

        if (response.getPaymentInfo().getTotalAmount() != null) {
            responseDTO.amount(response.getPaymentInfo().getTotalAmount());
        }

        if (response.getPaymentInfo().getParentTransactionId() != null) {
            responseDTO.responseMap(MessageConstants.PARENTTRANSACTIONID, response.getPaymentInfo().getParentTransactionId());
        }
        if (response.getPaymentInfo().getReceiptId() != null) {
            responseDTO.responseMap(MessageConstants.RECEIPTID, response.getPaymentInfo().getReceiptId());
        }
        if (response.getPaymentInfo().getExchangeRate() != null) {
            responseDTO.responseMap(MessageConstants.EXCHANGERATE, response.getPaymentInfo().getExchangeRate().toString());
        }
        if (response.getPaymentInfo().getPaymentStatusType() != null) {
            responseDTO.responseMap(MessageConstants.PAYMENTSTATUS, response.getPaymentInfo().getPaymentStatusType().getType());
        }
        if (response.getPaymentInfo().getPendingReasonType() != null) {
            responseDTO.responseMap(MessageConstants.PENDINGREASON, response.getPaymentInfo().getPendingReasonType().getType());
        }
        if (response.getPaymentInfo().getReasonCodeType() != null) {
            responseDTO.responseMap(MessageConstants.REASONCODE, response.getPaymentInfo().getReasonCodeType().getType());
        }
        if (response.getPaymentInfo().getHoldDecisionType() != null) {
            responseDTO.responseMap(MessageConstants.HOLDDECISION, response.getPaymentInfo().getHoldDecisionType().getType());
        }
        if (response.getPaymentInfo().getFeeAmount() != null) {
            responseDTO.responseMap(MessageConstants.FEEAMOUNT, response.getPaymentInfo().getFeeAmount().toString());
        }
        if (response.getPaymentInfo().getSettleAmount() != null) {
            responseDTO.responseMap(MessageConstants.SETTLEAMOUNT, response.getPaymentInfo().getSettleAmount().toString());
        }
        if (response.getPaymentInfo().getTaxAmount() != null) {
            responseDTO.responseMap(MessageConstants.TAXAMOUNT, response.getPaymentInfo().getTaxAmount().toString());
        }
    }

    protected void setRefundInformation(PayPalPaymentResponse response, PaymentResponseDTO responseDTO) {
        if (response.getRefundInfo().getRefundTransactionId() != null) {
            responseDTO.responseMap(MessageConstants.REFUNDTRANSACTIONID, response.getRefundInfo().getRefundTransactionId());
        }
        if (response.getRefundInfo().getFeeRefundAmount() != null) {
            responseDTO.responseMap(MessageConstants.FEEREFUNDAMT, response.getRefundInfo().getFeeRefundAmount().toString());
        }
        if (response.getRefundInfo().getGrossRefundAmount() != null) {
            responseDTO.responseMap(MessageConstants.GROSSREFUNDAMT, response.getRefundInfo().getGrossRefundAmount().toString());
        }
        if (response.getRefundInfo().getNetRefundAmount() != null) {
            responseDTO.responseMap(MessageConstants.NETREFUNDAMT, response.getRefundInfo().getNetRefundAmount().toString());
        }
        if (response.getRefundInfo().getTotalRefundAmount() != null) {
            responseDTO.responseMap(MessageConstants.TOTALREFUNDEDAMT, response.getRefundInfo().getTotalRefundAmount().toString());
        }
        if (response.getRefundInfo().getRefundInfo() != null) {
            responseDTO.responseMap(MessageConstants.REFUNDINFO, response.getRefundInfo().getRefundInfo());
        }
        if (response.getRefundInfo().getRefundStatusType() != null) {
            responseDTO.responseMap(MessageConstants.REFUNDSTATUS, response.getRefundInfo().getRefundStatusType().getType());
        }
        if (response.getRefundInfo().getPendingReasonType() != null) {
            responseDTO.responseMap(MessageConstants.PENDINGREASON, response.getRefundInfo().getPendingReasonType().getType());
        }
    }

}
