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
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreatePaymentRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreatePaymentResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalRefundRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalRefundResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalSaleRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalSaleResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalSaleRetrievalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalSaleRetrievalResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalVoidRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalVoidResponse;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Authorization;
import com.paypal.api.payments.Billing;
import com.paypal.api.payments.Capture;
import com.paypal.api.payments.DetailedRefund;
import com.paypal.api.payments.FundingInstrument;
import com.paypal.api.payments.ItemList;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RefundRequest;
import com.paypal.api.payments.Sale;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalResource;

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

    @Lookup("blPayPalApiContext")
    protected APIContext getApiContext() {
        return null;
    }

    @Override
    public PaymentResponseDTO authorize(PaymentRequestDTO paymentRequestDTO) throws PaymentException  {
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT, PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);
        PayPalResource auth = authorizePayment(paymentRequestDTO);
        if (auth instanceof Payment) {
            Payment payment = (Payment) auth;
            Transaction transaction = payment.getTransactions().get(0);
            if (transaction != null) {
                Amount amount = transaction.getAmount();
                responseDTO
                        .successful(true)
                        .rawResponse(payment.toJSON())
                        .paymentTransactionType(PaymentTransactionType.AUTHORIZE)
                        .responseMap(MessageConstants.AUTHORIZATONID, payment.getId())
                        .amount(new Money(amount.getTotal(), amount.getCurrency()));
            }
        } else {
            Authorization authorization = (Authorization) auth;
            responseDTO
                    .successful(true)
                    .rawResponse(authorization.toJSON())
                    .paymentTransactionType(PaymentTransactionType.AUTHORIZE)
                    .responseMap(MessageConstants.AUTHORIZATONID, authorization.getId())
                    .amount(new Money(authorization.getAmount().getTotal(), authorization.getAmount().getCurrency()));
        }

        return responseDTO;
    }

    @Override
    public PaymentResponseDTO capture(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT, PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);
        Authorization auth = getAuthorization(paymentRequestDTO);
        Capture capture = capturePayment(paymentRequestDTO, auth);
        responseDTO
            .successful(true)
            .rawResponse(capture.toJSON())
            .paymentTransactionType(PaymentTransactionType.CAPTURE)
            .responseMap(MessageConstants.CAPTUREID, capture.getId())
            .amount(new Money(capture.getAmount().getTotal(), capture.getAmount().getCurrency()));
        return responseDTO;
    }

    @Override
    public PaymentResponseDTO authorizeAndCapture(PaymentRequestDTO paymentRequestDTO) throws PaymentException  {
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT, PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);
        PayPalResource sale = salePayment(paymentRequestDTO);
        if (sale instanceof Payment) {
            Payment payment = (Payment) sale;
            Transaction transaction = payment.getTransactions().get(0);
            if (transaction != null) {
                Amount amount = transaction.getAmount();
                responseDTO
                        .successful(true)
                        .rawResponse(payment.toJSON())
                        .paymentTransactionType(PaymentTransactionType.AUTHORIZE)
                        .responseMap(MessageConstants.AUTHORIZATONID, payment.getId())
                        .amount(new Money(amount.getTotal(), amount.getCurrency()));
            }
        } else {
            Sale s = (Sale) sale;
            responseDTO
                    .successful(true)
                    .rawResponse(sale.toJSON())
                    .paymentTransactionType(PaymentTransactionType.AUTHORIZE_AND_CAPTURE)
                    .responseMap(MessageConstants.SALEID, s.getId())
                    .amount(new Money(s.getAmount().getTotal(), s.getAmount().getCurrency()));
        }

        return responseDTO;
    }

    @Override
    public PaymentResponseDTO reverseAuthorize(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT, PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);
        Authorization auth = getAuthorization(paymentRequestDTO);
        auth = voidAuthorization(auth);
        responseDTO
            .successful(true)
            .rawResponse(auth.toJSON())
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
                    .rawResponse(sale.toJSON())
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
            .rawResponse(auth.toJSON())
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
        PayPalCaptureResponse captureResponse = (PayPalCaptureResponse) payPalCheckoutService.call(new PayPalCaptureRequest(auth, capture, getApiContext()));
        return captureResponse.getCapture();
    }

    protected PayPalResource authorizePayment(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        Payment payment = new Payment();
        payment.setId(getPaymentId(paymentRequestDTO));
        payment.setTransactions(generateAuthorizeTransactions(paymentRequestDTO));
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(getPayerId(paymentRequestDTO));

        if (isBillingAgreementRequest(paymentRequestDTO)) {
            payment.setIntent("authorize");
            payment.setPayer(generateAuthorizePayer(paymentRequestDTO));
            PayPalCreatePaymentResponse response = (PayPalCreatePaymentResponse) payPalCheckoutService.call(new PayPalCreatePaymentRequest(payment, getApiContext()));
            return response.getPayment();
        }

        PayPalAuthorizeResponse response = (PayPalAuthorizeResponse) payPalCheckoutService.call(new PayPalAuthorizeRequest(payment, paymentExecution, getApiContext()));
        return response.getAuthorization();
    }

    protected Payer generateAuthorizePayer(PaymentRequestDTO paymentRequestDTO) {
        if (isBillingAgreementRequest(paymentRequestDTO)) {
            return generateBillingAgreementPayer(paymentRequestDTO);
        }

        return null;
    }

    protected boolean isBillingAgreementRequest(PaymentRequestDTO paymentRequestDTO) {
        return paymentRequestDTO.getAdditionalFields().containsKey(MessageConstants.BILLINGAGREEMENTID);
    }

    protected List<Transaction> generateAuthorizeTransactions(PaymentRequestDTO paymentRequestDTO) {
        return generateTransactions(paymentRequestDTO);
    }

    protected List<Transaction> generateTransactions(PaymentRequestDTO paymentRequestDTO) {
        Amount amount = payPalCheckoutService.getPayPalAmountFromOrder(paymentRequestDTO);

        // Transaction information
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription(payPalCheckoutService.getConfiguration().getPaymentDescription());
        transaction.setCustom(paymentRequestDTO.getOrderId());

        ItemList itemList = payPalCheckoutService.getPayPalItemListFromOrder(paymentRequestDTO, true);
        if (itemList != null) {
            transaction.setItemList(itemList);
        }

        // Add transaction to a list
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);
        return transactions;
    }

    protected PayPalResource salePayment(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        Payment payment = new Payment();
        payment.setId(getPaymentId(paymentRequestDTO));
        payment.setTransactions(generateSaleTransactions(paymentRequestDTO));
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(getPayerId(paymentRequestDTO));

        if (isBillingAgreementRequest(paymentRequestDTO)) {
            payment.setIntent("sale");
            payment.setPayer(generateSalePayer(paymentRequestDTO));
            PayPalCreatePaymentResponse response = (PayPalCreatePaymentResponse) payPalCheckoutService.call(new PayPalCreatePaymentRequest(payment, getApiContext()));
            return response.getPayment();
        }

        PayPalSaleResponse response = (PayPalSaleResponse) payPalCheckoutService.call(new PayPalSaleRequest(payment, paymentExecution, getApiContext()));
        return response.getSale();
    }

    protected Payer generateSalePayer(PaymentRequestDTO paymentRequestDTO) {
        if (isBillingAgreementRequest(paymentRequestDTO)) {
            return generateBillingAgreementPayer(paymentRequestDTO);
        }

        return null;
    }

    protected List<Transaction> generateSaleTransactions(PaymentRequestDTO paymentRequestDTO) {
        return generateTransactions(paymentRequestDTO);
    }

    protected Payer generateBillingAgreementPayer(PaymentRequestDTO paymentRequestDTO) {
        Payer payer = new Payer();
        payer.setPaymentMethod(MessageConstants.PAYER_PAYMENTMETHOD_PAYPAL);
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
        PayPalVoidResponse response = (PayPalVoidResponse) payPalCheckoutService.call(new PayPalVoidRequest(auth, getApiContext()));
        return response.getVoidedAuthorization();
    }

    protected DetailedRefund refundPayment(PaymentRequestDTO paymentRequestDTO, Capture capture) throws PaymentException {
        RefundRequest refund = new RefundRequest();
        Amount amount = new Amount();
        amount.setCurrency(paymentRequestDTO.getOrderCurrencyCode());
        amount.setTotal(paymentRequestDTO.getTransactionTotal());
        refund.setAmount(amount);
        PayPalRefundResponse response = (PayPalRefundResponse) payPalCheckoutService.call(new PayPalRefundRequest(refund, capture, getApiContext()));
        return response.getDetailedRefund();
    }

    protected DetailedRefund refundPayment(PaymentRequestDTO paymentRequestDTO, Sale sale) throws PaymentException {
        RefundRequest refund = new RefundRequest();
        Amount amount = new Amount();
        amount.setCurrency(paymentRequestDTO.getOrderCurrencyCode());
        amount.setTotal(paymentRequestDTO.getTransactionTotal());
        refund.setAmount(amount);
        PayPalRefundResponse response = (PayPalRefundResponse) payPalCheckoutService.call(new PayPalRefundRequest(refund, sale, getApiContext()));
        return response.getDetailedRefund();
    }

    protected Authorization getAuthorization(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        PayPalAuthorizationRetrievalResponse authResponse = (PayPalAuthorizationRetrievalResponse) payPalCheckoutService.call(new PayPalAuthorizationRetrievalRequest(getAuthorizationId(paymentRequestDTO), getApiContext()));
        return authResponse.getAuthorization();
    }

    protected Sale getSale(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        PayPalSaleRetrievalResponse saleResponse = (PayPalSaleRetrievalResponse) payPalCheckoutService.call(new PayPalSaleRetrievalRequest(getSaleId(paymentRequestDTO), getApiContext()));
        return saleResponse.getSale();
    }

    protected Capture getCapture(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        PayPalCaptureRetrievalResponse response = (PayPalCaptureRetrievalResponse) payPalCheckoutService.call((new PayPalCaptureRetrievalRequest(getCaptureId(paymentRequestDTO), getApiContext())));
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
