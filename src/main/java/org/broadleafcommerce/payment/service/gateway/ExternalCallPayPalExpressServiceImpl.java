/*
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2014 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.payment.service.gateway;

import org.apache.commons.collections4.CollectionUtils;
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
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalExpressPaymentGatewayType;
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
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.paypal.api.payments.Details;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.ShippingAddress;
import com.paypal.api.payments.Transaction;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.annotation.Resource;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blExternalCallPayPalExpressService")
public class ExternalCallPayPalExpressServiceImpl extends AbstractExternalPaymentGatewayCall<PayPalRequest, PayPalResponse> implements ExternalCallPayPalExpressService {

    private static final Log LOG = LogFactory.getLog(ExternalCallPayPalExpressServiceImpl.class);

    @Resource(name = "blPayPalExpressConfiguration")
    protected PayPalExpressConfiguration configuration;

    @Resource(name = "blPayPalExpressRequestGenerator")
    protected PayPalRequestGenerator requestGenerator;

    @Resource(name = "blPayPalExpressResponseGenerator")
    protected PayPalResponseGenerator responseGenerator;

    @Override
    public PayPalExpressConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public Integer getFailureReportingThreshold() {
        return configuration.getFailureReportingThreshold();
    }

    @Override
    public PayPalResponse call(PayPalRequest paymentRequest) throws PaymentException {
        throw new UnsupportedOperationException("Shouldn't use the call method because all calls should be done through the PayPal SDK using methods on the Payment object");
    }

    @Override
    public PayPalResponse communicateWithVendor(PayPalRequest paymentRequest) throws Exception {
        throw new UnsupportedOperationException("Shouldn't use the communicateWithVendor method because all calls should be done through the PayPal SDK using methods on the Payment object");

    }

    public String getServerUrl() {
        return configuration.getServerUrl();
    }

    @Override
    public PayPalPaymentRequest buildBasicRequest(PaymentRequestDTO requestDTO, PayPalTransactionType transactionType) {
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

    @Override
    public PaymentResponseDTO commonAuthorizeOrSale(PaymentRequestDTO requestDTO, PayPalTransactionType transactionType,
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
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT,
                PayPalExpressPaymentGatewayType.PAYPAL_EXPRESS);

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

    @Override
    public void setCommonPaymentResponse(PayPalPaymentResponse response, PaymentResponseDTO responseDTO) {

        try {
            responseDTO.rawResponse(URLDecoder.decode(response.getRawResponse(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        responseDTO.responseMap(MessageConstants.TOKEN, response.getResponseToken());
        responseDTO.responseMap(MessageConstants.CORRELATIONID, response.getCorrelationId());
    }

    @Override
    public void setCommonDetailsResponse(Payment response, PaymentResponseDTO responseDTO) {
        responseDTO.rawResponse(response.toJSON());
        
        // TODO Do we just use the first transaction?
        if (CollectionUtils.isNotEmpty(response.getTransactions()) && 
            response.getTransactions().get(0) != null &&
            response.getTransactions().get(0).getItemList() != null) {
            ShippingAddress shippingAddress = response.getTransactions().get(0).getItemList().getShippingAddress();
            
            responseDTO.shipTo()
                .addressFirstName(shippingAddress.getRecipientName())
                .addressLine1(shippingAddress.getLine1())
                .addressLine2(shippingAddress.getLine2())
                .addressCityLocality(shippingAddress.getCity())
                .addressStateRegion(shippingAddress.getState())
                .addressPostalCode(shippingAddress.getPostalCode())
                .addressCountryCode(shippingAddress.getCountryCode())
                .addressPhone(shippingAddress.getPhone())
                .done();
            
            if (shippingAddress.getStatus() != null) {
                responseDTO.getShipTo().additionalFields(MessageConstants.ADDRESSSTATUS, shippingAddress.getStatus());
            }

            // TODO Do we just use the first transaction?
            Transaction transaction = response.getTransactions().get(0);
            
            String itemTotal = "";
            String shippingDiscount = "";
            String shippingTotal = "";
            String totalTax = "";
            String total = "0.00";
            String currency = "USD";
            if (transaction.getAmount() != null && transaction.getAmount().getDetails() != null) {
                Details details = transaction.getAmount().getDetails();
                if (details.getSubtotal() != null) {
                    itemTotal = details.getSubtotal();
                }
                if (details.getShippingDiscount() != null) {
                    shippingDiscount = details.getShippingDiscount();
                }
                if (details.getShipping() != null) {
                    shippingTotal = details.getShipping();
                }
                if (details.getTax() != null) {
                    totalTax = details.getTax();
                }
            }
            if (transaction.getAmount() != null) {
                total = transaction.getAmount().getTotal();
                if (transaction.getAmount().getCurrency() != null) {
                    currency = transaction.getAmount().getCurrency();
                }
            }
            responseDTO.amount(new Money(total, currency))
                    .orderId(transaction.getCustom())
                    .successful(true)
                    .valid(true)
                    .completeCheckoutOnCallback(true) // TODO need to figure this out dynamically
                    .responseMap(MessageConstants.DETAILSPAYMENTALLOWEDMETHOD, response.getPayer().getPaymentMethod())
                    .responseMap(MessageConstants.DETAILSPAYMENTTRANSACTIONID, response.getId()) // TODO not sure if this is the correct id or not
                    .responseMap(MessageConstants.DETAILSPAYMENTITEMTOTAL, itemTotal)
                    .responseMap(MessageConstants.DETAILSPAYMENTSHIPPINGDISCOUNT, shippingDiscount)
                    .responseMap(MessageConstants.DETAILSPAYMENTSHIPPINGTOTAL,shippingTotal)
                    .responseMap(MessageConstants.DETAILSPAYMENTTOTALTAX, totalTax);
            
//            TODO don't have the slightest as to what this is now
//            if (response.getCheckoutStatusType()!=null) {
//                responseDTO.responseMap(MessageConstants.CHECKOUTSTATUS, response.getCheckoutStatusType().getType());
//            }
    
//            TODO No idea what a paypal adjustment is
//            String paypalAdjustment = (response.getPayPalAdjustment() != null)? response.getPayPalAdjustment().toString() : "";
            String payerStatus = response.getPayer().getStatus();
    
            responseDTO.customer()
                .firstName(response.getPayer().getPayerInfo().getFirstName())
                .lastName(response.getPayer().getPayerInfo().getLastName())
//                .companyName(response.getBusiness()) TODO No idea if this is even sent anymore
                .phone(response.getPayer().getPayerInfo().getPhone())
                .email(response.getPayer().getPayerInfo().getEmail())
                .done()
//             TODO I don't think I care about this anymore
//            .responseMap(MessageConstants.TOKEN, response.getResponseToken())
            .responseMap(MessageConstants.NOTE, response.getNoteToPayer())
//            TODO don't know what this is
//            .responseMap(MessageConstants.PAYPALADJUSTMENT, paypalAdjustment)
            .responseMap(MessageConstants.PAYERSTATUS, payerStatus);
        }

    }

    @Override
    public void setDecisionInformation(PayPalPaymentResponse response, PaymentResponseDTO responseDTO) {
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

    @Override
    public void setRefundInformation(PayPalPaymentResponse response, PaymentResponseDTO responseDTO) {
        if (response.getRefundInfo().getRefundTransactionId() != null) {
            responseDTO.responseMap(MessageConstants.REFUNDTRANSACTIONID, response.getRefundInfo().getRefundTransactionId());
        }

        if (response.getRefundInfo().getGrossRefundAmount() != null) {
            responseDTO.amount(new Money(response.getRefundInfo().getGrossRefundAmount().toString()));
            responseDTO.responseMap(MessageConstants.GROSSREFUNDAMT, response.getRefundInfo().getGrossRefundAmount().toString());
        }

        if (response.getRefundInfo().getFeeRefundAmount() != null) {
            responseDTO.responseMap(MessageConstants.FEEREFUNDAMT, response.getRefundInfo().getFeeRefundAmount().toString());
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

    @Override
    public String getServiceName() {
        return getClass().getName();
    }
}
