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
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalSaleRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalSaleResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalSaleRetrievalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalSaleRetrievalResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalVoidRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalVoidResponse;
import org.springframework.stereotype.Service;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Authorization;
import com.paypal.api.payments.Billing;
import com.paypal.api.payments.Capture;
import com.paypal.api.payments.DetailedRefund;
import com.paypal.api.payments.FundingInstrument;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RefundRequest;
import com.paypal.api.payments.Sale;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import java.util.ArrayList;
import java.util.List;
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
        Capture capture = capturePayment(paymentRequestDTO, auth);
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
        Sale sale = salePayment(paymentRequestDTO);
        responseDTO
            .successful(true)
            .paymentTransactionType(PaymentTransactionType.AUTHORIZE_AND_CAPTURE)
            .responseMap(MessageConstants.SALEID, sale.getId())
            .amount(new Money(sale.getAmount().getTotal(), sale.getAmount().getCurrency()));
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
        Sale sale = getSale(paymentRequestDTO);

        if (capture != null) {
            DetailedRefund detailRefund = refundPayment(paymentRequestDTO, capture);
            responseDTO
                    .successful(true)
                    .paymentTransactionType(PaymentTransactionType.REFUND)
                    .responseMap(MessageConstants.REFUNDID, detailRefund.getId())
                    .amount(new Money(detailRefund.getAmount().getTotal(), detailRefund.getAmount().getCurrency()));
            return responseDTO;
        } else if (sale != null){
            DetailedRefund detailRefund = refundPayment(paymentRequestDTO, sale);
            responseDTO
                    .successful(true)
                    .paymentTransactionType(PaymentTransactionType.REFUND)
                    .responseMap(MessageConstants.REFUNDID, detailRefund.getId())
                    .amount(new Money(detailRefund.getAmount().getTotal(), detailRefund.getAmount().getCurrency()));
            return responseDTO;
        }

        throw new PaymentException("Unable to perform refund. Unable to find corresponding capture or sale transaction.");
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

    protected Capture capturePayment(PaymentRequestDTO paymentRequestDTO, Authorization auth) throws PaymentException {
        Capture capture = new Capture();
        capture.setIsFinalCapture(true);
        Amount amount = new Amount();
        amount.setCurrency(paymentRequestDTO.getOrderCurrencyCode());
        amount.setTotal(paymentRequestDTO.getTransactionTotal());
        capture.setAmount(amount);
        PayPalCaptureResponse captureResponse = (PayPalCaptureResponse) payPalCheckoutService.call(new PayPalCaptureRequest(auth, capture, apiContext));
        return captureResponse.getCapture();
    }

    protected Authorization authorizePayment(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        Payment payment = new Payment();
        payment.setId(getPaymentId(paymentRequestDTO));
        payment.setPayer(generateAuthorizePayer(paymentRequestDTO));
        payment.setTransactions(generateAuthorizeTransactions(paymentRequestDTO));
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(getPayerId(paymentRequestDTO));
        PayPalAuthorizeResponse response = (PayPalAuthorizeResponse) payPalCheckoutService.call(new PayPalAuthorizeRequest(payment, paymentExecution, apiContext));
        return response.getAuthorization();
    }

    protected Payer generateAuthorizePayer(PaymentRequestDTO paymentRequestDTO) {
        if (paymentRequestDTO.getAdditionalFields().containsKey(MessageConstants.BILLINGAGREEMENTID)) {
            return generateBillingAgreementPayer(paymentRequestDTO);
        }

        return null;
    }

    protected List<Transaction> generateAuthorizeTransactions(PaymentRequestDTO paymentRequestDTO) {
        return null;
    }

    protected Sale salePayment(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        Payment payment = new Payment();
        payment.setId(getPaymentId(paymentRequestDTO));
        payment.setPayer(generateSalePayer(paymentRequestDTO));
        payment.setTransactions(generateSaleTransactions(paymentRequestDTO));
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(getPayerId(paymentRequestDTO));
        PayPalSaleResponse response = (PayPalSaleResponse) payPalCheckoutService.call(new PayPalSaleRequest(payment, paymentExecution, apiContext));
        return response.getSale();
    }

    protected Payer generateSalePayer(PaymentRequestDTO paymentRequestDTO) {
        if (paymentRequestDTO.getAdditionalFields().containsKey(MessageConstants.BILLINGAGREEMENTID)) {
            return generateBillingAgreementPayer(paymentRequestDTO);
        }

        return null;
    }

    protected List<Transaction> generateSaleTransactions(PaymentRequestDTO paymentRequestDTO) {
        return null;
    }

    protected Payer generateBillingAgreementPayer(PaymentRequestDTO paymentRequestDTO) {
        Payer payer = new Payer();
        List<FundingInstrument> fundingInstruments = new ArrayList<>();
        FundingInstrument billingAgreement = new FundingInstrument();
        Billing billing = new Billing();
        billing.setBillingAgreementId((String)paymentRequestDTO.getAdditionalFields().get(MessageConstants.BILLINGAGREEMENTID));
        billingAgreement.setBilling(billing);
        fundingInstruments.add(billingAgreement);
        payer.setFundingInstruments(fundingInstruments);
        return payer;
    }

    protected Authorization voidAuthorization(Authorization auth) throws PaymentException {
        PayPalVoidResponse response = (PayPalVoidResponse) payPalCheckoutService.call(new PayPalVoidRequest(auth, apiContext));
        return response.getVoidedAuthorization();
    }

    protected DetailedRefund refundPayment(PaymentRequestDTO paymentRequestDTO, Capture capture) throws PaymentException {
        RefundRequest refund = new RefundRequest();
        Amount amount = new Amount();
        amount.setCurrency(paymentRequestDTO.getOrderCurrencyCode());
        amount.setTotal(paymentRequestDTO.getTransactionTotal());
        refund.setAmount(amount);
        PayPalRefundResponse response = (PayPalRefundResponse) payPalCheckoutService.call(new PayPalRefundRequest(refund, capture, apiContext));
        return response.getDetailedRefund();
    }

    protected DetailedRefund refundPayment(PaymentRequestDTO paymentRequestDTO, Sale sale) throws PaymentException {
        RefundRequest refund = new RefundRequest();
        Amount amount = new Amount();
        amount.setCurrency(paymentRequestDTO.getOrderCurrencyCode());
        amount.setTotal(paymentRequestDTO.getTransactionTotal());
        refund.setAmount(amount);
        PayPalRefundResponse response = (PayPalRefundResponse) payPalCheckoutService.call(new PayPalRefundRequest(refund, sale, apiContext));
        return response.getDetailedRefund();
    }

    protected Authorization getAuthorization(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        PayPalAuthorizationRetrievalResponse authResponse = (PayPalAuthorizationRetrievalResponse) payPalCheckoutService.call(new PayPalAuthorizationRetrievalRequest(getAuthorizationId(paymentRequestDTO), apiContext));
        return authResponse.getAuthorization();
    }

    protected Sale getSale(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        PayPalSaleRetrievalResponse saleResponse = (PayPalSaleRetrievalResponse) payPalCheckoutService.call(new PayPalSaleRetrievalRequest(getSaleId(paymentRequestDTO), apiContext));
        return saleResponse.getSale();
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

    protected String getSaleId(PaymentRequestDTO paymentRequestDTO) {
        return (String) paymentRequestDTO.getAdditionalFields().get(MessageConstants.SALEID);
    }
    
    protected String getCaptureId(PaymentRequestDTO paymentRequestDTO) {
        return (String) paymentRequestDTO.getAdditionalFields().get(MessageConstants.CAPTUREID);
    }
}
