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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.payment.PaymentTransactionType;
import org.broadleafcommerce.common.payment.PaymentType;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.AbstractPaymentGatewayTransactionService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayTransactionService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalAuthorizationRetrievalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalAuthorizationRetrievalResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalAuthorizeRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalAuthorizeResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCaptureRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCaptureResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCaptureRetrievalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCaptureRetrievalResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCheckoutPaymentGatewayType;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalRefundRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalRefundResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalVoidRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalVoidResponse;
import org.springframework.stereotype.Service;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Authorization;
import com.paypal.api.payments.Capture;
import com.paypal.api.payments.DetailedRefund;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RefundRequest;
import com.paypal.base.rest.APIContext;

import javax.annotation.Resource;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalCheckoutTransactionService")
public class PayPalCheckoutTransactionServiceImpl extends AbstractPaymentGatewayTransactionService implements PaymentGatewayTransactionService {

    protected static final Log LOG = LogFactory.getLog(PayPalCheckoutTransactionServiceImpl.class);

    @Resource(name = "blExternalCallPayPalCheckoutService")
    protected ExternalCallPayPalCheckoutService payPalCheckoutService;

    @Resource(name = "blPayPalApiContext")
    protected APIContext apiContext;

    @Override
    public PaymentResponseDTO authorize(PaymentRequestDTO paymentRequestDTO) throws PaymentException  {
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT, PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);
        Authorization auth = authorizePayment(paymentRequestDTO);
        responseDTO
            .successful(true)
            .paymentTransactionType(PaymentTransactionType.AUTHORIZE)
            .responseMap(MessageConstants.AUTHORIZATONID, auth.getId())
            .amount(new Money(auth.getAmount().getTotal(), auth.getAmount().getCurrency()));
        return responseDTO;
    }

    @Override
    public PaymentResponseDTO capture(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT, PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);
        Authorization auth = getAuthorization(paymentRequestDTO);
        Capture capture = capturePayment(auth);
        responseDTO
            .successful(true)
            .paymentTransactionType(PaymentTransactionType.CAPTURE)
            .responseMap(MessageConstants.CAPTUREID, capture.getId())
            .amount(new Money(capture.getAmount().getTotal(), capture.getAmount().getCurrency()));
        return responseDTO;
    }

    @Override
    public PaymentResponseDTO authorizeAndCapture(PaymentRequestDTO paymentRequestDTO) throws PaymentException  {
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT, PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);
        Authorization auth = authorizePayment(paymentRequestDTO);
        Capture capture = capturePayment(auth);
        responseDTO
            .successful(true)
            .paymentTransactionType(PaymentTransactionType.AUTHORIZE_AND_CAPTURE)
            .responseMap(MessageConstants.CAPTUREID, capture.getId())
            .responseMap(MessageConstants.AUTHORIZATONID, auth.getId())
            .amount(new Money(capture.getAmount().getTotal(), capture.getAmount().getCurrency()));
        return responseDTO;
    }

    @Override
    public PaymentResponseDTO reverseAuthorize(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT, PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);
        Authorization auth = getAuthorization(paymentRequestDTO);
        auth = voidAuthorization(auth);
        responseDTO
            .successful(true)
            .paymentTransactionType(PaymentTransactionType.REVERSE_AUTH)
            .amount(new Money(auth.getAmount().getTotal(), auth.getAmount().getCurrency()));
        return responseDTO;
    }

    @Override
    public PaymentResponseDTO refund(PaymentRequestDTO paymentRequestDTO) throws PaymentException  {
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT, PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);
        Capture capture = getCapture(paymentRequestDTO);
        DetailedRefund detailRefund = refundPayment(capture);
        responseDTO
            .successful(true)
            .paymentTransactionType(PaymentTransactionType.REFUND)
            .responseMap(MessageConstants.REFUNDID, detailRefund.getId())
            .amount(new Money(detailRefund.getAmount().getTotal(), detailRefund.getAmount().getCurrency()));
        return responseDTO;
    }

    @Override
    public PaymentResponseDTO voidPayment(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT, PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);
        Authorization auth = getAuthorization(paymentRequestDTO);
        auth = voidAuthorization(auth);
        responseDTO
            .successful(true)
            .paymentTransactionType(PaymentTransactionType.VOID)
            .amount(new Money(auth.getAmount().getTotal(), auth.getAmount().getCurrency()));
        return responseDTO;
    }

    protected Capture capturePayment(Authorization auth) throws PaymentException {
        Capture capture = new Capture();
        capture.setIsFinalCapture(true);
        Amount amount = new Amount();
        amount.setCurrency(auth.getAmount().getCurrency());
        amount.setTotal(auth.getAmount().getTotal());
        capture.setAmount(amount);
        PayPalCaptureResponse captureResponse = (PayPalCaptureResponse) payPalCheckoutService.call(new PayPalCaptureRequest(auth, capture, apiContext));
        return captureResponse.getCapture();
    }

    protected Authorization authorizePayment(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        Payment payment = new Payment();
        payment.setId(getPaymentId(paymentRequestDTO));
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(getPayerId(paymentRequestDTO));
        PayPalAuthorizeResponse response = (PayPalAuthorizeResponse) payPalCheckoutService.call(new PayPalAuthorizeRequest(payment, paymentExecution, apiContext));
        return response.getAuthorization();
    }

    protected Authorization voidAuthorization(Authorization auth) throws PaymentException {
        PayPalVoidResponse response = (PayPalVoidResponse) payPalCheckoutService.call(new PayPalVoidRequest(auth, apiContext));
        return response.getVoidedAuthorization();
    }

    protected DetailedRefund refundPayment(Capture capture) throws PaymentException {
        RefundRequest refund = new RefundRequest();
        refund.setAmount(capture.getAmount());
        PayPalRefundResponse response = (PayPalRefundResponse) payPalCheckoutService.call(new PayPalRefundRequest(refund, capture, apiContext));
        return response.getDetailedRefund();
    }

    protected Authorization getAuthorization(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        PayPalAuthorizationRetrievalResponse authResponse = (PayPalAuthorizationRetrievalResponse) payPalCheckoutService.call(new PayPalAuthorizationRetrievalRequest(getAuthorizationId(paymentRequestDTO), apiContext));
        return authResponse.getAuthorization();
    }

    protected Capture getCapture(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        PayPalCaptureRetrievalResponse response = (PayPalCaptureRetrievalResponse) payPalCheckoutService.call((new PayPalCaptureRetrievalRequest(getCaptureId(paymentRequestDTO), apiContext)));
        return response.getCapture();
    }

    protected String getPaymentId(PaymentRequestDTO paymentRequestDTO) {
        return (String) paymentRequestDTO.getAdditionalFields().get(MessageConstants.PAYMENTID);
    }

    protected String getPayerId(PaymentRequestDTO paymentRequestDTO) {
        return (String) paymentRequestDTO.getAdditionalFields().get(MessageConstants.PAYERID);
    }

    protected String getAuthorizationId(PaymentRequestDTO paymentRequestDTO) {
        return (String) paymentRequestDTO.getAdditionalFields().get(MessageConstants.AUTHORIZATONID);
    }
    
    protected String getCaptureId(PaymentRequestDTO paymentRequestDTO) {
        return (String) paymentRequestDTO.getAdditionalFields().get(MessageConstants.CAPTUREID);
    }
}
