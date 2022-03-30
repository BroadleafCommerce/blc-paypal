/*
 * Copyright (C) 2009 - 2020 Broadleaf Commerce
 *
 * Licensed under the Broadleaf End User License Agreement (EULA), Version 1.1 (the
 * "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt).
 *
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the
 * "Custom License") between you and Broadleaf Commerce. You may not use this file except in
 * compliance with the applicable license.
 *
 * NOTICE: All information contained herein is, and remains the property of Broadleaf Commerce, LLC
 * The intellectual and technical concepts contained herein are proprietary to Broadleaf Commerce,
 * LLC and may be covered by U.S. and Foreign Patents, patents in process, and are protected by
 * trade secret or copyright law. Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained from Broadleaf Commerce, LLC.
 */
package org.broadleaf.payment.service.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutExternalCallService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutPaymentGatewayType;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutRestConfigurationProperties;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutTransactionService;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalAuthorizationRetrievalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalAuthorizationRetrievalResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalAuthorizeRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalAuthorizeResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCaptureRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCaptureResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCaptureRetrievalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCaptureRetrievalResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalRefundRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalRefundResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalSaleRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalSaleResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalSaleRetrievalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalSaleRetrievalResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalVoidRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalVoidResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.broadleafcommerce.money.util.MonetaryUtils;
import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.domain.PaymentResponse;
import com.broadleafcommerce.paymentgateway.domain.enums.DefaultTransactionFailureTypes;
import com.broadleafcommerce.paymentgateway.domain.enums.DefaultTransactionTypes;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.paypal.api.payments.Authorization;
import com.paypal.api.payments.Capture;
import com.paypal.api.payments.DetailedRefund;
import com.paypal.api.payments.Error;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.RelatedResources;
import com.paypal.api.payments.Sale;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.PayPalRESTException;

import java.util.Collections;

/**
 * @author Dima Myroniuk (dmyroniuk)
 */
@SpringBootTest
public class PayPalCheckoutTransactionServiceIT {

    private static final String CUSTOM_ID = "customId";
    private static final String RAW_RESPONSE = "rawResponse";
    private static final String AUTH_ID = "authId";
    private static final String REASON_CODE = "reasonCode";
    private static final String PAYMENT_ID = "paymentId";
    private static final String PAYER_ID = "payerId";
    private static final String SALE_ID = "saleId";
    private static final String BILLING_AGREEMENT_ID = "billingAgreementId";
    private static final String CAPTURE_ID = "captureId";
    private static final String VOIDED_AUTHORIZATION_ID = "voidedAuthorizationId";
    private static final String DETAIL_REFUND_ID = "detailRefundId";

    @MockBean
    PayPalCheckoutExternalCallService paypalCheckoutService;

    @Autowired
    PayPalCheckoutTransactionService payPalCheckoutTransactionService;

    @Autowired
    PayPalCheckoutRestConfigurationProperties configProperties;

    @Test
    void testAuthorize() {
        Transaction transaction = new Transaction();
        transaction.setCustom(CUSTOM_ID);

        RelatedResources relatedResources = new RelatedResources();
        Authorization authorization = Mockito.mock(Authorization.class);
        relatedResources.setAuthorization(authorization);

        transaction.setRelatedResources(Collections.singletonList(relatedResources));

        Payment payment = Mockito.mock(Payment.class);
        when(payment.getTransactions()).thenReturn(Collections.singletonList(transaction));
        when(payment.toJSON()).thenReturn(RAW_RESPONSE);
        PayPalAuthorizeResponse payPalAuthorizeResponse = new PayPalAuthorizeResponse(payment);

        when(paypalCheckoutService.call(any(PayPalAuthorizeRequest.class)))
                .thenReturn(payPalAuthorizeResponse);
        when(authorization.getId()).thenReturn(AUTH_ID);
        when(authorization.getReasonCode()).thenReturn(REASON_CODE);

        PaymentRequest paymentRequest = createBasePaymentRequest();

        paymentRequest.additionalField(MessageConstants.PAYMENTID, PAYMENT_ID);
        paymentRequest.additionalField(MessageConstants.PAYERID, PAYER_ID);

        PaymentResponse paymentResponse =
                payPalCheckoutTransactionService.authorize(paymentRequest);

        assertThat(paymentResponse.getPaymentGatewayType())
                .isEqualTo(PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);
        assertThat(paymentResponse.getTransactionType())
                .isEqualTo(DefaultTransactionTypes.AUTHORIZE);
        assertThat(paymentResponse.isSuccessful()).isTrue();
        assertThat(paymentResponse.getDateRecorded()).isNotNull();
        assertThat(paymentResponse.getTransactionReferenceId()).isEqualTo(transaction.getCustom());
        assertThat(paymentResponse.getGatewayTransactionId()).isEqualTo(AUTH_ID);
        assertThat(paymentResponse.getGatewayResponseCode()).isEqualTo(REASON_CODE);
        assertThat(paymentResponse.getResponseMap()).containsEntry(MessageConstants.AUTHORIZATONID,
                AUTH_ID);
        assertThat(paymentResponse.getRawResponse()).isEqualTo(RAW_RESPONSE);
    }

    @Test
    void testAuthorizeAndCapture() {
        Transaction transaction = new Transaction();
        transaction.setCustom(CUSTOM_ID);

        RelatedResources relatedResources = new RelatedResources();
        Sale sale = Mockito.mock(Sale.class);

        when(sale.getId()).thenReturn(SALE_ID);
        when(sale.getReasonCode()).thenReturn(REASON_CODE);
        when(sale.getBillingAgreementId()).thenReturn(BILLING_AGREEMENT_ID);
        when(sale.toJSON()).thenReturn(RAW_RESPONSE);

        relatedResources.setSale(sale);

        transaction.setRelatedResources(Collections.singletonList(relatedResources));

        Payment payment = Mockito.mock(Payment.class);

        when(payment.getTransactions()).thenReturn(Collections.singletonList(transaction));

        PayPalSaleResponse payPalSaleResponse = new PayPalSaleResponse(payment);

        when(paypalCheckoutService.call(any(PayPalSaleRequest.class)))
                .thenReturn(payPalSaleResponse);

        PaymentRequest paymentRequest = createBasePaymentRequest();

        paymentRequest.additionalField(MessageConstants.PAYMENTID, PAYMENT_ID);
        paymentRequest.additionalField(MessageConstants.PAYERID, PAYER_ID);

        PaymentResponse paymentResponse =
                payPalCheckoutTransactionService.authorizeAndCapture(paymentRequest);

        assertThat(paymentResponse.getPaymentGatewayType())
                .isEqualTo(PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);
        assertThat(paymentResponse.getTransactionType())
                .isEqualTo(DefaultTransactionTypes.AUTHORIZE_AND_CAPTURE);
        assertThat(paymentResponse.isSuccessful()).isTrue();
        assertThat(paymentResponse.getDateRecorded()).isNotNull();
        assertThat(paymentResponse.getGatewayTransactionId()).isEqualTo(SALE_ID);
        assertThat(paymentResponse.getGatewayResponseCode()).isEqualTo(REASON_CODE);
        assertThat(paymentResponse.getResponseMap()).containsEntry(MessageConstants.SALEID,
                SALE_ID);
        assertThat(paymentResponse.getResponseMap())
                .containsEntry(MessageConstants.BILLINGAGREEMENTID, BILLING_AGREEMENT_ID);
        assertThat(paymentResponse.getRawResponse()).isEqualTo(RAW_RESPONSE);
    }

    @Test
    void testCapture() {
        Authorization authorization = Mockito.mock(Authorization.class);
        PayPalAuthorizationRetrievalResponse payPalAuthorizationRetrievalResponse =
                new PayPalAuthorizationRetrievalResponse(authorization);
        Capture capture = Mockito.mock(Capture.class);
        PayPalCaptureResponse payPalCaptureResponse = new PayPalCaptureResponse(capture);

        when(paypalCheckoutService.call(any(PayPalAuthorizationRetrievalRequest.class)))
                .thenReturn(payPalAuthorizationRetrievalResponse);
        when(paypalCheckoutService.call(any(PayPalCaptureRequest.class)))
                .thenReturn(payPalCaptureResponse);

        when(capture.getId()).thenReturn(CAPTURE_ID);
        when(capture.getReasonCode()).thenReturn(REASON_CODE);
        when(capture.toJSON()).thenReturn(RAW_RESPONSE);

        PaymentRequest paymentRequest = createBasePaymentRequest();

        PaymentResponse paymentResponse = payPalCheckoutTransactionService.capture(paymentRequest);

        assertThat(paymentResponse.getPaymentGatewayType())
                .isEqualTo(PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);
        assertThat(paymentResponse.getTransactionType()).isEqualTo(DefaultTransactionTypes.CAPTURE);
        assertThat(paymentResponse.isSuccessful()).isTrue();

        assertThat(paymentResponse.getDateRecorded()).isNotNull();
        assertThat(paymentResponse.getGatewayTransactionId()).isEqualTo(CAPTURE_ID);
        assertThat(paymentResponse.getGatewayResponseCode()).isEqualTo(REASON_CODE);
        assertThat(paymentResponse.getResponseMap()).containsEntry(MessageConstants.CAPTUREID,
                CAPTURE_ID);
        assertThat(paymentResponse.getRawResponse()).isEqualTo(RAW_RESPONSE);
    }

    @Test
    void testReverseAuthorize() {
        Authorization authorization = Mockito.mock(Authorization.class);
        PayPalAuthorizationRetrievalResponse payPalAuthorizationRetrievalResponse =
                new PayPalAuthorizationRetrievalResponse(authorization);
        Capture capture = Mockito.mock(Capture.class);
        PayPalCaptureResponse payPalCaptureResponse = new PayPalCaptureResponse(capture);

        Authorization voidedAuthorization = Mockito.mock(Authorization.class);
        PayPalVoidResponse payPalVoidResponse = new PayPalVoidResponse(voidedAuthorization);

        when(paypalCheckoutService.call(any(PayPalAuthorizationRetrievalRequest.class)))
                .thenReturn(payPalAuthorizationRetrievalResponse);
        when(paypalCheckoutService.call(any(PayPalVoidRequest.class)))
                .thenReturn(payPalVoidResponse);
        when(paypalCheckoutService.call(any(PayPalCaptureRequest.class)))
                .thenReturn(payPalCaptureResponse);

        when(voidedAuthorization.getId()).thenReturn(VOIDED_AUTHORIZATION_ID);
        when(voidedAuthorization.getReasonCode()).thenReturn(REASON_CODE);
        when(voidedAuthorization.toJSON()).thenReturn(RAW_RESPONSE);

        PaymentRequest paymentRequest = createBasePaymentRequest();

        PaymentResponse paymentResponse =
                payPalCheckoutTransactionService.reverseAuthorize(paymentRequest);

        assertThat(paymentResponse.getPaymentGatewayType())
                .isEqualTo(PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);
        assertThat(paymentResponse.getTransactionType())
                .isEqualTo(DefaultTransactionTypes.REVERSE_AUTH);
        assertThat(paymentResponse.isSuccessful()).isTrue();

        assertThat(paymentResponse.getDateRecorded()).isNotNull();
        assertThat(paymentResponse.getGatewayTransactionId()).isEqualTo(VOIDED_AUTHORIZATION_ID);
        assertThat(paymentResponse.getGatewayResponseCode()).isEqualTo(REASON_CODE);
        assertThat(paymentResponse.getRawResponse()).isEqualTo(RAW_RESPONSE);
    }

    @Test
    void testRefundCapture() {
        Capture capture = Mockito.mock(Capture.class);
        PayPalCaptureRetrievalResponse payPalCaptureRetrievalResponse =
                new PayPalCaptureRetrievalResponse(capture);

        DetailedRefund detailRefund = Mockito.mock(DetailedRefund.class);

        PayPalRefundResponse payPalRefundResponse = new PayPalRefundResponse(detailRefund);

        when(paypalCheckoutService.call(any(PayPalCaptureRetrievalRequest.class)))
                .thenReturn(payPalCaptureRetrievalResponse);
        when(paypalCheckoutService.call(any(PayPalRefundRequest.class)))
                .thenReturn(payPalRefundResponse);

        when(detailRefund.getId()).thenReturn(DETAIL_REFUND_ID);
        when(detailRefund.getCaptureId()).thenReturn(CAPTURE_ID);
        when(detailRefund.getReasonCode()).thenReturn(REASON_CODE);
        when(detailRefund.toJSON()).thenReturn(RAW_RESPONSE);

        PaymentRequest paymentRequest = createBasePaymentRequest();

        paymentRequest.additionalField(MessageConstants.CAPTUREID, CAPTURE_ID);

        PaymentResponse paymentResponse = payPalCheckoutTransactionService.refund(paymentRequest);

        assertThat(paymentResponse.getPaymentGatewayType())
                .isEqualTo(PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);
        assertThat(paymentResponse.getTransactionType()).isEqualTo(DefaultTransactionTypes.REFUND);
        assertThat(paymentResponse.isSuccessful()).isTrue();

        assertThat(paymentResponse.getDateRecorded()).isNotNull();
        assertThat(paymentResponse.getGatewayTransactionId()).isEqualTo(DETAIL_REFUND_ID);
        assertThat(paymentResponse.getGatewayResponseCode()).isEqualTo(REASON_CODE);
        assertThat(paymentResponse.getResponseMap()).containsEntry(MessageConstants.REFUNDID,
                DETAIL_REFUND_ID);
        assertThat(paymentResponse.getResponseMap()).containsEntry(MessageConstants.CAPTUREID,
                CAPTURE_ID);
        assertThat(paymentResponse.getGatewayResponseCode()).isEqualTo(REASON_CODE);
        assertThat(paymentResponse.getRawResponse()).isEqualTo(RAW_RESPONSE);
    }

    @Test
    void testRefundSale() {
        Sale sale = Mockito.mock(Sale.class);
        PayPalSaleRetrievalResponse payPalSaleRetrievalResponse =
                new PayPalSaleRetrievalResponse(sale);

        DetailedRefund detailRefund = Mockito.mock(DetailedRefund.class);

        PayPalRefundResponse payPalRefundResponse = new PayPalRefundResponse(detailRefund);

        when(paypalCheckoutService.call(any(PayPalSaleRetrievalRequest.class)))
                .thenReturn(payPalSaleRetrievalResponse);
        when(paypalCheckoutService.call(any(PayPalRefundRequest.class)))
                .thenReturn(payPalRefundResponse);

        when(detailRefund.getId()).thenReturn(DETAIL_REFUND_ID);
        when(detailRefund.getSaleId()).thenReturn(SALE_ID);
        when(detailRefund.getReasonCode()).thenReturn(REASON_CODE);
        when(detailRefund.toJSON()).thenReturn(RAW_RESPONSE);

        PaymentRequest paymentRequest = createBasePaymentRequest();

        paymentRequest.additionalField(MessageConstants.SALEID, SALE_ID);

        PaymentResponse paymentResponse = payPalCheckoutTransactionService.refund(paymentRequest);

        assertThat(paymentResponse.getPaymentGatewayType())
                .isEqualTo(PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);
        assertThat(paymentResponse.getTransactionType()).isEqualTo(DefaultTransactionTypes.REFUND);
        assertThat(paymentResponse.isSuccessful()).isTrue();

        assertThat(paymentResponse.getDateRecorded()).isNotNull();
        assertThat(paymentResponse.getGatewayTransactionId()).isEqualTo(DETAIL_REFUND_ID);
        assertThat(paymentResponse.getGatewayResponseCode()).isEqualTo(REASON_CODE);
        assertThat(paymentResponse.getResponseMap()).containsEntry(MessageConstants.REFUNDID,
                DETAIL_REFUND_ID);
        assertThat(paymentResponse.getResponseMap()).containsEntry(MessageConstants.SALEID,
                SALE_ID);
        assertThat(paymentResponse.getGatewayResponseCode()).isEqualTo(REASON_CODE);
        assertThat(paymentResponse.getRawResponse()).isEqualTo(RAW_RESPONSE);
    }

    @Test
    void testProcessExceptionProcessingFailure_400() {
        PaymentRequest paymentRequest = createBasePaymentRequest();

        paymentRequest.additionalField(MessageConstants.PAYMENTID, PAYMENT_ID);
        paymentRequest.additionalField(MessageConstants.PAYERID, PAYER_ID);

        PayPalRESTException payPalRESTException = new PayPalRESTException("PayPalRESTException");

        Error error = new Error();
        // COMPLIANCE_VIOLATION
        error.setName(configProperties.getPaymentDeclineCodes().get(0));

        payPalRESTException.setDetails(error);
        payPalRESTException.setResponsecode(400);

        PaymentException paymentException = new PaymentException("Error", payPalRESTException);

        when(paypalCheckoutService.call(any(PayPalAuthorizeRequest.class)))
                .thenThrow(paymentException);

        PaymentResponse paymentResponse =
                payPalCheckoutTransactionService.authorize(paymentRequest);

        assertThat(paymentResponse.isSuccessful()).isFalse();
        assertThat(paymentResponse.getFailureType())
                .isEqualTo(DefaultTransactionFailureTypes.PROCESSING_FAILURE.name());
    }

    @Test
    void testProcessExceptionInvalidRequest_400() {
        PaymentRequest paymentRequest = createBasePaymentRequest();

        paymentRequest.additionalField(MessageConstants.PAYMENTID, PAYMENT_ID);
        paymentRequest.additionalField(MessageConstants.PAYERID, PAYER_ID);

        PayPalRESTException payPalRESTException = new PayPalRESTException("PayPalRESTException");

        Error error = new Error();

        payPalRESTException.setDetails(error);
        payPalRESTException.setResponsecode(400);

        PaymentException paymentException = new PaymentException("Error", payPalRESTException);

        when(paypalCheckoutService.call(any(PayPalAuthorizeRequest.class)))
                .thenThrow(paymentException);

        PaymentResponse paymentResponse =
                payPalCheckoutTransactionService.authorize(paymentRequest);

        assertThat(paymentResponse.isSuccessful()).isFalse();
        assertThat(paymentResponse.getFailureType())
                .isEqualTo(DefaultTransactionFailureTypes.INVALID_REQUEST.name());
    }

    @Test
    void testProcessExceptionGatewayCredentialsError_401() {
        PaymentRequest paymentRequest = createBasePaymentRequest();

        paymentRequest.additionalField(MessageConstants.PAYMENTID, PAYMENT_ID);
        paymentRequest.additionalField(MessageConstants.PAYERID, PAYER_ID);

        PayPalRESTException payPalRESTException = new PayPalRESTException("PayPalRESTException");

        Error error = new Error();

        payPalRESTException.setDetails(error);
        payPalRESTException.setResponsecode(401);

        PaymentException paymentException = new PaymentException("Error", payPalRESTException);

        when(paypalCheckoutService.call(any(PayPalAuthorizeRequest.class)))
                .thenThrow(paymentException);

        PaymentResponse paymentResponse =
                payPalCheckoutTransactionService.authorize(paymentRequest);

        assertThat(paymentResponse.isSuccessful()).isFalse();
        assertThat(paymentResponse.getFailureType())
                .isEqualTo(DefaultTransactionFailureTypes.GATEWAY_CREDENTIALS_ERROR.name());
    }

    @Test
    void testProcessExceptionGatewayCredentialsError_403() {
        PaymentRequest paymentRequest = createBasePaymentRequest();

        paymentRequest.additionalField(MessageConstants.PAYMENTID, PAYMENT_ID);
        paymentRequest.additionalField(MessageConstants.PAYERID, PAYER_ID);

        PayPalRESTException payPalRESTException = new PayPalRESTException("PayPalRESTException");

        Error error = new Error();

        payPalRESTException.setDetails(error);
        payPalRESTException.setResponsecode(403);

        PaymentException paymentException = new PaymentException("Error", payPalRESTException);

        when(paypalCheckoutService.call(any(PayPalAuthorizeRequest.class)))
                .thenThrow(paymentException);

        PaymentResponse paymentResponse =
                payPalCheckoutTransactionService.authorize(paymentRequest);

        assertThat(paymentResponse.isSuccessful()).isFalse();
        assertThat(paymentResponse.getFailureType())
                .isEqualTo(DefaultTransactionFailureTypes.GATEWAY_CREDENTIALS_ERROR.name());
    }

    @Test
    void testProcessExceptionNetworkError_408() {
        PaymentRequest paymentRequest = createBasePaymentRequest();

        paymentRequest.additionalField(MessageConstants.PAYMENTID, PAYMENT_ID);
        paymentRequest.additionalField(MessageConstants.PAYERID, PAYER_ID);

        PayPalRESTException payPalRESTException = new PayPalRESTException("PayPalRESTException");

        Error error = new Error();

        payPalRESTException.setDetails(error);
        payPalRESTException.setResponsecode(408);

        PaymentException paymentException = new PaymentException("Error", payPalRESTException);

        when(paypalCheckoutService.call(any(PayPalAuthorizeRequest.class)))
                .thenThrow(paymentException);

        PaymentResponse paymentResponse =
                payPalCheckoutTransactionService.authorize(paymentRequest);

        assertThat(paymentResponse.isSuccessful()).isFalse();
        assertThat(paymentResponse.getFailureType())
                .isEqualTo(DefaultTransactionFailureTypes.NETWORK_ERROR.name());
    }

    @Test
    void testProcessExceptionRateLimitError_429() {
        PaymentRequest paymentRequest = createBasePaymentRequest();

        paymentRequest.additionalField(MessageConstants.PAYMENTID, PAYMENT_ID);
        paymentRequest.additionalField(MessageConstants.PAYERID, PAYER_ID);

        PayPalRESTException payPalRESTException = new PayPalRESTException("PayPalRESTException");

        Error error = new Error();
        error.setName("RATE_LIMIT_REACHED");
        payPalRESTException.setDetails(error);
        payPalRESTException.setResponsecode(429);

        PaymentException paymentException = new PaymentException("Error", payPalRESTException);

        when(paypalCheckoutService.call(any(PayPalAuthorizeRequest.class)))
                .thenThrow(paymentException);

        PaymentResponse paymentResponse =
                payPalCheckoutTransactionService.authorize(paymentRequest);

        assertThat(paymentResponse.isSuccessful()).isFalse();
        assertThat(paymentResponse.getFailureType())
                .isEqualTo(DefaultTransactionFailureTypes.API_RATE_LIMIT_ERROR.name());
    }

    @Test
    void testProcessExceptionGatewayError_500() {
        PaymentRequest paymentRequest = createBasePaymentRequest();

        paymentRequest.additionalField(MessageConstants.PAYMENTID, PAYMENT_ID);
        paymentRequest.additionalField(MessageConstants.PAYERID, PAYER_ID);

        PayPalRESTException payPalRESTException = new PayPalRESTException("PayPalRESTException");

        Error error = new Error();

        payPalRESTException.setDetails(error);
        payPalRESTException.setResponsecode(500);

        PaymentException paymentException = new PaymentException("Error", payPalRESTException);

        when(paypalCheckoutService.call(any(PayPalAuthorizeRequest.class)))
                .thenThrow(paymentException);

        PaymentResponse paymentResponse =
                payPalCheckoutTransactionService.authorize(paymentRequest);

        assertThat(paymentResponse.isSuccessful()).isFalse();
        assertThat(paymentResponse.getFailureType())
                .isEqualTo(DefaultTransactionFailureTypes.GATEWAY_ERROR.name());
    }

    private PaymentRequest createBasePaymentRequest() {
        return new PaymentRequest()
                .paymentId(PAYMENT_ID)
                .transactionTotal(MonetaryUtils.toAmount("10.99", "USD"))
                .orderSubtotal(MonetaryUtils.toAmount("10.99", "USD"))
                .shippingTotal(MonetaryUtils.zero("USD"))
                .taxTotal(MonetaryUtils.zero("USD"))
                .paymentOwnerType("BLC_CART")
                .paymentOwnerId("ownerId")
                .transactionReferenceId("transactionReferenceId");
    }
}
