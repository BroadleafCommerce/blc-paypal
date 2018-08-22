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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
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
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalExpressPaymentGatewayType;
import org.springframework.stereotype.Service;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Authorization;
import com.paypal.api.payments.Capture;
import com.paypal.api.payments.DetailedRefund;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RefundRequest;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

import javax.annotation.Resource;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalExpressTransactionService")
public class PayPalExpressTransactionServiceImpl extends AbstractPaymentGatewayTransactionService implements PaymentGatewayTransactionService {

    protected static final Log LOG = LogFactory.getLog(PayPalExpressTransactionServiceImpl.class);

    @Resource(name = "blExternalCallPayPalExpressService")
    protected ExternalCallPayPalExpressService payPalExpressService;

    @Resource(name = "blPayPalApiContext")
    protected APIContext apiContext;

    @Override
    public PaymentResponseDTO authorize(PaymentRequestDTO paymentRequestDTO) throws PaymentException  {
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT, PayPalExpressPaymentGatewayType.PAYPAL_EXPRESS);
        Payment authPayment;
        try {
            authPayment = authorizePayment(getPaymentId(paymentRequestDTO), getPayerId(paymentRequestDTO));
        } catch (PayPalRESTException e) {
            throw new PaymentException(e);
        }
        Authorization auth = authPayment.getTransactions().get(0).getRelatedResources().get(0).getAuthorization();
        responseDTO
            .successful(true)
            .paymentTransactionType(PaymentTransactionType.AUTHORIZE)
            .responseMap(MessageConstants.AUTHORIZATONID, auth.getId())
            .amount(new Money(auth.getAmount().getTotal(), auth.getAmount().getCurrency()));
        return responseDTO;
    }

    @Override
    public PaymentResponseDTO capture(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT, PayPalExpressPaymentGatewayType.PAYPAL_EXPRESS);
        Capture capture;
        try {
            capture = capturePayment(getAuthorizationId(paymentRequestDTO));
        } catch (PayPalRESTException e) {
            throw new PaymentException(e);
        }
        responseDTO
            .successful(true)
            .paymentTransactionType(PaymentTransactionType.CAPTURE)
            .responseMap(MessageConstants.CAPTUREID, capture.getId())
            .amount(new Money(capture.getAmount().getTotal(), capture.getAmount().getCurrency()));
        return responseDTO;
    }

    @Override
    public PaymentResponseDTO authorizeAndCapture(PaymentRequestDTO paymentRequestDTO) throws PaymentException  {
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT, PayPalExpressPaymentGatewayType.PAYPAL_EXPRESS);
        Authorization auth;
        Capture capture;
        try {
            Payment authPayment = authorizePayment(getPaymentId(paymentRequestDTO), getPayerId(paymentRequestDTO));
            auth = authPayment.getTransactions().get(0).getRelatedResources().get(0).getAuthorization();
            capture = capturePayment(auth);
        } catch (PayPalRESTException e) {
            throw new PaymentException(e);
        }
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
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT, PayPalExpressPaymentGatewayType.PAYPAL_EXPRESS);
        Authorization auth;
        try {
            auth = Authorization.get(apiContext, getAuthorizationId(paymentRequestDTO));
            auth = auth.doVoid(apiContext);
        } catch (PayPalRESTException e) {
            throw new PaymentException(e);
        }
        responseDTO
            .successful(true)
            .paymentTransactionType(PaymentTransactionType.REVERSE_AUTH)
            .amount(new Money(auth.getAmount().getTotal(), auth.getAmount().getCurrency()));
        return responseDTO;
    }

    @Override
    public PaymentResponseDTO refund(PaymentRequestDTO paymentRequestDTO) throws PaymentException  {
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT, PayPalExpressPaymentGatewayType.PAYPAL_EXPRESS);
        DetailedRefund detailRefund;
        try {
            Capture capture = Capture.get(apiContext, getCaptureId(paymentRequestDTO));
            RefundRequest refund = new RefundRequest();
            refund.setAmount(capture.getAmount());
            detailRefund = capture.refund(apiContext, refund);
        } catch (PayPalRESTException e) {
            throw new PaymentException(e);
        }
        responseDTO
            .successful(true)
            .paymentTransactionType(PaymentTransactionType.REFUND)
            .responseMap(MessageConstants.REFUNDID, detailRefund.getId())
            .amount(new Money(detailRefund.getAmount().getTotal(), detailRefund.getAmount().getCurrency()));
        return responseDTO;
    }

    @Override
    public PaymentResponseDTO voidPayment(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT, PayPalExpressPaymentGatewayType.PAYPAL_EXPRESS);
        Authorization auth;
        try {
            auth = Authorization.get(apiContext, getAuthorizationId(paymentRequestDTO));
            auth = auth.doVoid(apiContext);
        } catch (PayPalRESTException e) {
            throw new PaymentException(e);
        }
        responseDTO
            .successful(true)
            .paymentTransactionType(PaymentTransactionType.VOID)
            .amount(new Money(auth.getAmount().getTotal(), auth.getAmount().getCurrency()));
        return responseDTO;
    }

    protected Payment authorizePayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);
        return payment.execute(apiContext, paymentExecution);
    }
    
    protected Capture capturePayment(Authorization auth) throws PayPalRESTException {
        Capture capture = new Capture();
        capture.setIsFinalCapture(true);
        Amount amount = new Amount();
        amount.setCurrency(auth.getAmount().getCurrency());
        amount.setTotal(auth.getAmount().getTotal());
        capture.setAmount(amount);
        LOG.info("Auth about to be captured :\n" + ToStringBuilder.reflectionToString(auth, ToStringStyle.MULTI_LINE_STYLE));
        LOG.info("Capture about to be captured :\n " + ToStringBuilder.reflectionToString(capture, ToStringStyle.MULTI_LINE_STYLE));
        return auth.capture(apiContext, capture);
    }
   
    protected Capture capturePayment(String authorizationId) throws PayPalRESTException {
        Authorization auth = Authorization.get(apiContext, authorizationId);
        return capturePayment(auth);
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
