/*-
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2023 Broadleaf Commerce
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

import com.paypal.http.HttpResponse;
import com.paypal.http.exceptions.HttpException;
import com.paypal.orders.AmountWithBreakdown;
import com.paypal.orders.Authorization;
import com.paypal.orders.Order;
import com.paypal.orders.PaymentCollection;
import com.paypal.orders.PurchaseUnit;
import com.paypal.payments.Capture;
import com.paypal.payments.CaptureRequest;
import com.paypal.payments.Money;
import com.paypal.payments.Refund;
import com.paypal.payments.RefundRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.payment.PaymentTransactionType;
import org.broadleafcommerce.common.payment.PaymentType;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.AbstractPaymentGatewayTransactionService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayTransactionService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.vendor.paypal.service.PayPalClientProvider;
import org.broadleafcommerce.vendor.paypal.service.exception.PayPalRESTException;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalAuthorizationRetrievalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalAuthorizationRetrievalResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalAuthorizeRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalAuthorizeResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCaptureAuthRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCaptureAuthResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCaptureOrderRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCaptureOrderResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCheckoutPaymentGatewayType;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalErrorResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalRefundRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalRefundResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalVoidAuthRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalVoidAuthResponse;
import org.broadleafcommerce.vendor.paypal.service.util.PayPalUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Optional;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalCheckoutTransactionService")
public class PayPalCheckoutTransactionServiceImpl extends AbstractPaymentGatewayTransactionService implements PaymentGatewayTransactionService {

    protected static final Log LOG = LogFactory.getLog(PayPalCheckoutTransactionServiceImpl.class);

    @Resource(name = "blExternalCallPayPalCheckoutService")
    protected ExternalCallPayPalCheckoutService payPalCheckoutService;

    @Resource(name = "blPayPalClientProvider")
    protected PayPalClientProvider clientProvider;

    @Resource(name = "blPayPalUtils")
    protected PayPalUtils utils;

    @Override
    public PaymentResponseDTO authorize(PaymentRequestDTO paymentRequest) {
        PaymentResponseDTO paymentResponse = new PaymentResponseDTO(
                PaymentType.THIRD_PARTY_ACCOUNT,
                PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT_V2)
                .paymentTransactionType(PaymentTransactionType.AUTHORIZE);

        try {
            PayPalAuthorizeResponse response = authorizeOrder(paymentRequest);
            Authorization authorization = response.getAuthorization();
            org.broadleafcommerce.common.money.Money amount =
                    utils.convertOrdersMoneyToMoney(authorization.amount());

            HttpResponse<Order> result = response.getResponse();
            paymentResponse
                    .successful(true)
                    .amount(amount)
                    .responseMap(MessageConstants.AUTHORIZATONID, authorization.id())
                    .rawResponse(utils.convertResponseToJson(result));
        } catch (Exception e) {
            processException(e, paymentResponse, paymentRequest);
        }

        return paymentResponse;
    }

    @Override
    public PaymentResponseDTO capture(PaymentRequestDTO paymentRequest) {
        PaymentResponseDTO paymentResponse =
                new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT,
                        PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT_V2)
                        .paymentTransactionType(PaymentTransactionType.CAPTURE);

        try {
            PayPalCaptureAuthResponse response = captureAuthorization(paymentRequest);
            Capture capture = response.getContent();
            Money amount = capture.amount();
            org.broadleafcommerce.common.money.Money finalAmount = amount == null ?
                    new org.broadleafcommerce.common.money.Money(paymentRequest.getTransactionTotal(), paymentRequest.getOrderCurrencyCode()) :
                    utils.convertPaymentsMoneyToMoney(amount);

            paymentResponse
                    .successful(true)
                    .rawResponse(utils.convertResponseToJson(response.getResponse()))
                    .responseMap(MessageConstants.CAPTUREID, capture.id())
                    .amount(finalAmount);
        } catch (PaymentException e) {
            processException(e, paymentResponse, paymentRequest);
        }

        return paymentResponse;
    }

    @Override
    public PaymentResponseDTO authorizeAndCapture(PaymentRequestDTO paymentRequest) {
        PaymentResponseDTO paymentResponse =
                new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT,
                        PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT_V2)
                        .paymentTransactionType(PaymentTransactionType.AUTHORIZE_AND_CAPTURE);

        try {
            PayPalCaptureOrderResponse response = captureOrder(paymentRequest);
            Order order = response.getContent();
            PurchaseUnit purchaseUnit = order.purchaseUnits().get(0);
            AmountWithBreakdown amountWithBreakdown = purchaseUnit.amountWithBreakdown();
            org.broadleafcommerce.common.money.Money finalAmount = amountWithBreakdown == null ?
                    new org.broadleafcommerce.common.money.Money(paymentRequest.getTransactionTotal(), paymentRequest.getOrderCurrencyCode()) :
                    utils.getMoneyForAmountWithBreakdown(amountWithBreakdown);

            String captureId = Optional.ofNullable(purchaseUnit.payments())
                                .map(PaymentCollection::captures)
                                .map(captures -> captures.get(0))
                                .map(com.paypal.orders.Capture::id)
                                .orElse("");

            paymentResponse
                    .successful(true)
                    .rawResponse(utils.convertResponseToJson(response.getResponse()))
                    .responseMap(MessageConstants.CAPTUREID, captureId)
                    .amount(finalAmount);
        } catch (PaymentException e) {
            processException(e, paymentResponse, paymentRequest);
        }

        return paymentResponse;
    }

    @Override
    public PaymentResponseDTO reverseAuthorize(PaymentRequestDTO paymentRequest) {
        return voidAuthorization(paymentRequest, PaymentTransactionType.REVERSE_AUTH);
    }

    @Override
    public PaymentResponseDTO voidPayment(PaymentRequestDTO paymentRequest) {
        return voidAuthorization(paymentRequest, PaymentTransactionType.VOID);
    }

    @Override
    public PaymentResponseDTO refund(PaymentRequestDTO paymentRequest) {
        PaymentResponseDTO paymentResponse =
                new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT,
                        PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT_V2)
                        .paymentTransactionType(PaymentTransactionType.REFUND);
        String captureId = getCaptureId(paymentRequest);

        try {
            if (StringUtils.isBlank(captureId)) {
                throw new PaymentException(
                        "Unable to perform refund. Unable to find corresponding capture transaction.");
            }
            PayPalRefundResponse response = refundPayment(paymentRequest);
            Refund refund = response.getContent();
            Money amount = refund.amount();
            org.broadleafcommerce.common.money.Money finalAmount = amount == null ?
                    new org.broadleafcommerce.common.money.Money(paymentRequest.getTransactionTotal(), paymentRequest.getOrderCurrencyCode()) :
                    utils.convertPaymentsMoneyToMoney(amount);

            paymentResponse
                    .successful(true)
                    .rawResponse(utils.convertResponseToJson(response.getResponse()))
                    .responseMap(MessageConstants.REFUNDID, refund.id())
                    .responseMap(MessageConstants.CAPTUREID, captureId)
                    .amount(finalAmount);
        } catch (PaymentException e) {
            processException(e, paymentResponse, paymentRequest);
        }

        return paymentResponse;
    }

    protected PaymentResponseDTO voidAuthorization(PaymentRequestDTO paymentRequest,
                                                   PaymentTransactionType transactionType) {
        PaymentResponseDTO paymentResponse =
                new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT,
                        PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT_V2)
                        .paymentTransactionType(transactionType);
        try {
            voidAuthorization(paymentRequest);
            PayPalAuthorizationRetrievalResponse response = retrieveAuthorization(paymentRequest);
            com.paypal.payments.Authorization auth = response.getContent();
            Money amount = auth.amount();
            org.broadleafcommerce.common.money.Money finalAmount = amount == null ?
                    new org.broadleafcommerce.common.money.Money(paymentRequest.getTransactionTotal(), paymentRequest.getOrderCurrencyCode()) :
                    utils.convertPaymentsMoneyToMoney(amount);

            paymentResponse
                    .successful(true)
                    .amount(finalAmount)
                    .rawResponse(utils.convertResponseToJson(response.getResponse()));
        } catch (PaymentException e) {
            processException(e, paymentResponse, paymentRequest);
        }

        return paymentResponse;
    }

    /**
     * Executes a {@link PaymentTransactionType#CAPTURE} for the provided {@link Authorization}
     *
     * @param paymentRequest The request payload that should be used to form the transaction
     * @return a {@link PayPalCaptureAuthResponse}
     */
    protected PayPalCaptureAuthResponse captureAuthorization(
            PaymentRequestDTO paymentRequest) throws PaymentException {
        String authId = getAuthorizationId(paymentRequest);
        CaptureRequest capture = new CaptureRequest();
        // if null, then will capture the full amount
        Money amount = utils.convertToPaymentsMoney(paymentRequest.getTransactionTotal(), paymentRequest.getOrderCurrencyCode());
        capture.amount(amount);

        PayPalCaptureAuthRequest captureRequest =
                new PayPalCaptureAuthRequest(clientProvider,
                        paymentRequest,
                        capture,
                        authId);
        return payPalCheckoutService.call(captureRequest, PayPalCaptureAuthResponse.class);
    }

    /**
     * Executes an {@link PaymentTransactionType#AUTHORIZE} transaction based on the provided
     * {@link PaymentRequestDTO}
     *
     * @param paymentRequest The request payload that should be used to form the transaction
     * @return an {@link PayPalAuthorizeResponse} representing the final state of the transaction
     */
    protected PayPalAuthorizeResponse authorizeOrder(PaymentRequestDTO paymentRequest) throws PaymentException {
        String orderId = getOrderId(paymentRequest);
        PayPalAuthorizeRequest authorizeRequest =
                new PayPalAuthorizeRequest(clientProvider, paymentRequest, orderId);
        return payPalCheckoutService.call(authorizeRequest, PayPalAuthorizeResponse.class);
    }

    protected PayPalAuthorizationRetrievalResponse retrieveAuthorization(PaymentRequestDTO paymentRequest) throws PaymentException {
        String authId = getAuthorizationId(paymentRequest);
        PayPalAuthorizationRetrievalRequest request =
                new PayPalAuthorizationRetrievalRequest(clientProvider, paymentRequest, authId);
        return payPalCheckoutService.call(request, PayPalAuthorizationRetrievalResponse.class);
    }

    /**
     * Executes an {@link PaymentTransactionType#AUTHORIZE_AND_CAPTURE} transaction based on the
     * provided {@link PaymentRequestDTO}. For PayPal, this captures the {@link Order} immediately with
     * no authorization.
     *
     * @param paymentRequest The request payload that should be used to form the transaction
     * @return a {@link PayPalCaptureOrderResponse}
     */
    protected PayPalCaptureOrderResponse captureOrder(PaymentRequestDTO paymentRequest) throws PaymentException {
        String orderId = getOrderId(paymentRequest);
        PayPalCaptureOrderRequest authorizeRequest =
                new PayPalCaptureOrderRequest(clientProvider,
                        paymentRequest,
                        orderId);
        return payPalCheckoutService.call(authorizeRequest, PayPalCaptureOrderResponse.class);
    }

    /**
     * Executes a {@link PaymentTransactionType#REVERSE_AUTH} for the provided
     * {@link Authorization} object. For PayPal, this means voiding the original authorization.
     *
     * @param paymentRequest The request payload that should be used to form the transaction
     * @return the {@link PayPalVoidAuthResponse}
     */
    protected PayPalVoidAuthResponse voidAuthorization(PaymentRequestDTO paymentRequest) throws PaymentException {
        String authId = getAuthorizationId(paymentRequest);

        PayPalVoidAuthRequest voidRequest =
                new PayPalVoidAuthRequest(clientProvider, paymentRequest, authId);
        return payPalCheckoutService.call(voidRequest, PayPalVoidAuthResponse.class);
    }

    /**
     * Executes a {@link PaymentTransactionType#REFUND} for the captured payment.
     *
     * @param paymentRequest The request payload that should be used to form the transaction
     * @return a {@link PayPalRefundResponse}
     */
    protected PayPalRefundResponse refundPayment(PaymentRequestDTO paymentRequest) throws PaymentException {
        String captureId = getCaptureId(paymentRequest);
        RefundRequest refund = new RefundRequest();
        refund.amount(utils.convertToPaymentsMoney(paymentRequest.getTransactionTotal(), paymentRequest.getOrderCurrencyCode()));

        PayPalRefundRequest refundRequest =
                new PayPalRefundRequest(clientProvider, paymentRequest, refund, captureId);
        return payPalCheckoutService.call(refundRequest, PayPalRefundResponse.class);
    }

    private String getOrderId(PaymentRequestDTO paymentRequest) {
        return (String) paymentRequest.getAdditionalFields().get(MessageConstants.ORDER_ID);
    }

    private String getAuthorizationId(PaymentRequestDTO paymentRequest) {
        return (String) paymentRequest.getAdditionalFields().get(MessageConstants.AUTHORIZATONID);
    }

    @Nullable
    private String getCaptureId(PaymentRequestDTO paymentRequest) {
        return (String) paymentRequest.getAdditionalFields().get(MessageConstants.CAPTUREID);
    }

    /**
     * This method is responsible for levering the exception, paymentRequest, & transactionType to
     * populate the paymentResponse instead of allowing the exception to be thrown.
     *
     * @param e               the exception indicating a failed payment gateway transaction
     * @param paymentResponse the object that will hold the transaction results
     * @param paymentRequest  the request that was used to execute the transaction
     */
    protected void processException(Exception e,
                                    PaymentResponseDTO paymentResponse,
                                    PaymentRequestDTO paymentRequest) {
        paymentResponse.successful(false);

        Throwable cause = e.getCause();

        if (!(cause instanceof PayPalRESTException)) {
            LOG.error(e.getMessage(), e);
            return;
        }

        PayPalRESTException restException = (PayPalRESTException) cause;
        HttpException httpException = (HttpException) restException.getCause();
        PayPalErrorResponse error = utils.convertFromErrorJson(httpException);
        String message = error.getMessage();
        String errorName = error.getName();
        int httpCode = httpException.statusCode();

        paymentResponse.rawResponse(httpException.getMessage());
        paymentResponse.responseMap(MessageConstants.EXCEPTION_NAME, errorName);
        paymentResponse.responseMap(MessageConstants.EXCEPTION_MESSAGE, error.getMessage());
        paymentResponse.responseMap(MessageConstants.EXCEPTION_DEBUG_ID, error.getDebugId());

        if (400 == httpCode) {
            String errorMessage = String.format(
                    "An invalid request was supplied to PayPal's API. Exception message: %s",
                    message);

            paymentResponse.responseMap(MessageConstants.EXCEPTION_MESSAGE, errorMessage);

            LOG.error("An invalid request was supplied to PayPal's API", e);
            return;
        }

        if (401 == httpCode) {
            String errorMessage = String.format(
                    "Authentication with PayPal's API failed. Maybe you changed client id or client secret recently. Exception message: %s",
                    message);

            paymentResponse.responseMap(MessageConstants.EXCEPTION_MESSAGE, errorMessage);
            LOG.error(
                    "Authentication with PayPal's API failed. Maybe you changed client id or client secret recently",
                    e);

            return;
        }

        if (403 == httpCode) {
            String errorMessage = String.format(
                    "PayPal authorization failed due to insufficient permissions.. Exception message: %s",
                    message);

            paymentResponse.responseMap(MessageConstants.EXCEPTION_MESSAGE, errorMessage);
            LOG.error("PayPal authorization failed due to insufficient permissions", e);

            return;
        }

        if (408 == httpCode) {
            String errorMessage = String.format(
                    "Network communication with Stripe failed. Exception message: %s", message);

            paymentResponse.responseMap(MessageConstants.EXCEPTION_MESSAGE, errorMessage);
            LOG.error("Network communication with Stripe failed", e);
            return;
        }

        if (429 == httpCode && "RATE_LIMIT_REACHED".equals(errorName)) {
            String errorMessage = String.format(
                    "Too many requests made to the PayPal API too quickly. Exception message: %s",
                    message);

            paymentResponse.responseMap(MessageConstants.EXCEPTION_MESSAGE, errorMessage);
            LOG.warn("Too many requests made to the PayPal API too quickly", e);
            return;
        }

        String errorMessage = String.format(
                "An invalid request was supplied to PayPal's API. Exception message: %s", message);

        paymentResponse.responseMap(MessageConstants.EXCEPTION_MESSAGE, errorMessage);
        LOG.error("An invalid request was supplied to PayPal's API", e);
    }
}
