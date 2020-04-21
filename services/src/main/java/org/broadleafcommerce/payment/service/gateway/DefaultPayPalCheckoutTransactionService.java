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
package org.broadleafcommerce.payment.service.gateway;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.vendor.paypal.service.PayPalPaymentService;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalAuthorizationRetrievalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalAuthorizationRetrievalResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalAuthorizeRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalAuthorizeResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCaptureRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCaptureResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCaptureRetrievalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCaptureRetrievalResponse;
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

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.domain.PaymentResponse;
import com.broadleafcommerce.paymentgateway.domain.enums.DefaultPaymentTypes;
import com.broadleafcommerce.paymentgateway.domain.enums.DefaultTransactionTypes;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Authorization;
import com.paypal.api.payments.Billing;
import com.paypal.api.payments.Capture;
import com.paypal.api.payments.DetailedRefund;
import com.paypal.api.payments.Error;
import com.paypal.api.payments.FundingInstrument;
import com.paypal.api.payments.ItemList;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RefundRequest;
import com.paypal.api.payments.RelatedResources;
import com.paypal.api.payments.Sale;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.base.rest.PayPalResource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@RequiredArgsConstructor
public class DefaultPayPalCheckoutTransactionService implements PayPalCheckoutTransactionService {

    protected static final Log LOG =
            LogFactory.getLog(DefaultPayPalCheckoutTransactionService.class);

    private final PayPalCheckoutExternalCallService paypalCheckoutService;
    private final PayPalPaymentService payPalPaymentService;

    @Override
    public PaymentResponse authorize(PaymentRequest paymentRequest) throws PaymentException {
        PaymentResponse paymentResponse =
                new PaymentResponse(DefaultPaymentTypes.THIRD_PARTY_ACCOUNT,
                        PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);

        try {
            PayPalResource auth = authorizePayment(paymentRequest);
            if (auth instanceof Payment) {
                Payment payment = (Payment) auth;
                Transaction transaction = payment.getTransactions().get(0);
                if (transaction != null) {
                    Amount amount = transaction.getAmount();
                    paymentResponse
                            .successful(true)
                            .rawResponse(payment.toJSON())
                            .transactionType(DefaultTransactionTypes.AUTHORIZE)
                            .responseMap(MessageConstants.AUTHORIZATONID, payment.getId())
                            .amount(new BigDecimal(amount.getTotal()))
                            .currencyContext(amount.getCurrency());
                }
            } else {
                Authorization authorization = (Authorization) auth;
                Amount amount = authorization.getAmount();
                paymentResponse
                        .successful(true)
                        .rawResponse(authorization.toJSON())
                        .transactionType(DefaultTransactionTypes.AUTHORIZE)
                        .responseMap(MessageConstants.AUTHORIZATONID, authorization.getId())
                        .amount(new BigDecimal(amount.getTotal()))
                        .currencyContext(amount.getCurrency());
            }
        } catch (PaymentException ex) {
            if (ex.getCause() instanceof PayPalRESTException) {
                PayPalRESTException restException = (PayPalRESTException) ex.getCause();
                paymentResponse
                        .successful(false)
                        .rawResponse(restException.toString())
                        .transactionType(DefaultTransactionTypes.AUTHORIZE);
                populateErrorResponseMap(paymentResponse, restException);
                return paymentResponse;
            }
            throw ex;
        }
        return paymentResponse;
    }

    @Override
    public PaymentResponse capture(PaymentRequest paymentRequest) throws PaymentException {
        PaymentResponse responseDTO = new PaymentResponse(DefaultPaymentTypes.THIRD_PARTY_ACCOUNT,
                PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);

        try {
            Authorization auth = getAuthorization(paymentRequest);
            Capture capture = capturePayment(paymentRequest, auth);
            Amount amount = capture.getAmount();

            responseDTO
                    .successful(true)
                    .rawResponse(capture.toJSON())
                    .transactionType(DefaultTransactionTypes.CAPTURE)
                    .responseMap(MessageConstants.CAPTUREID, capture.getId())
                    .amount(new BigDecimal(amount.getTotal()))
                    .currencyContext(amount.getCurrency());
        } catch (PaymentException ex) {
            if (ex.getCause() instanceof PayPalRESTException) {
                PayPalRESTException restException = (PayPalRESTException) ex.getCause();
                responseDTO
                        .successful(false)
                        .rawResponse(restException.toString())
                        .transactionType(DefaultTransactionTypes.CAPTURE);
                populateErrorResponseMap(responseDTO, restException);
                return responseDTO;
            }
            throw ex;
        }
        return responseDTO;
    }

    @Override
    public PaymentResponse authorizeAndCapture(PaymentRequest paymentRequest)
            throws PaymentException {
        PaymentResponse responseDTO = new PaymentResponse(DefaultPaymentTypes.THIRD_PARTY_ACCOUNT,
                PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);

        try {
            PayPalResource salePayment = salePayment(paymentRequest);
            if (salePayment instanceof Payment) {
                Payment payment = (Payment) salePayment;
                Transaction transaction = payment.getTransactions().get(0);
                Payer payer = payment.getPayer();
                if (transaction != null && payer != null) {
                    Amount amount = transaction.getAmount();
                    List<Transaction> transactions = payment.getTransactions();
                    String saleId = null;
                    if (transactions != null) {
                        for (Transaction tx : transactions) {
                            List<RelatedResources> relatedResources = tx.getRelatedResources();
                            if (relatedResources != null) {
                                for (RelatedResources rr : relatedResources) {
                                    if (rr.getSale() != null) {
                                        saleId = rr.getSale().getId();
                                    }
                                }
                            }
                        }
                    }

                    String billingAgreementId = null;
                    if (payer.getFundingInstruments() != null) {
                        for (FundingInstrument fi : payer.getFundingInstruments()) {
                            if (fi.getBilling() != null) {
                                billingAgreementId = fi.getBilling().getBillingAgreementId();
                            }
                        }
                    }

                    String payerEmail = null;
                    String payerFirstName = null;
                    String payerLastName = null;
                    if (payer.getPayerInfo() != null) {
                        payerEmail = payer.getPayerInfo().getEmail();
                        payerFirstName = payer.getPayerInfo().getFirstName();
                        payerLastName = payer.getPayerInfo().getLastName();
                    }

                    responseDTO
                            .successful(true)
                            .rawResponse(payment.toJSON())
                            .transactionType(DefaultTransactionTypes.AUTHORIZE_AND_CAPTURE)
                            .responseMap(MessageConstants.PAYMENTID, payment.getId())
                            .responseMap(MessageConstants.SALEID, saleId)
                            .responseMap(MessageConstants.BILLINGAGREEMENTID, billingAgreementId)
                            .responseMap(MessageConstants.PAYER_INFO_EMAIL, payerEmail)
                            .responseMap(MessageConstants.PAYER_INFO_FIRST_NAME, payerFirstName)
                            .responseMap(MessageConstants.PAYER_INFO_LAST_NAME, payerLastName)
                            .amount(new BigDecimal(amount.getTotal()))
                            .currencyContext(amount.getCurrency());
                }
            } else {
                Sale sale = (Sale) salePayment;
                Amount amount = sale.getAmount();
                responseDTO
                        .successful(true)
                        .rawResponse(sale.toJSON())
                        .transactionType(DefaultTransactionTypes.AUTHORIZE_AND_CAPTURE)
                        .responseMap(MessageConstants.SALEID, sale.getId())
                        .responseMap(MessageConstants.BILLINGAGREEMENTID,
                                sale.getBillingAgreementId())
                        .amount(new BigDecimal(amount.getTotal()))
                        .currencyContext(amount.getCurrency());
            }
        } catch (PaymentException ex) {
            if (ex.getCause() instanceof PayPalRESTException) {
                PayPalRESTException restException = (PayPalRESTException) ex.getCause();
                responseDTO
                        .successful(false)
                        .rawResponse(restException.toString())
                        .transactionType(DefaultTransactionTypes.AUTHORIZE_AND_CAPTURE);
                populateErrorResponseMap(responseDTO, restException);
                return responseDTO;
            }
            throw ex;
        }

        return responseDTO;
    }

    @Override
    public PaymentResponse reverseAuthorize(PaymentRequest paymentRequest) throws PaymentException {
        PaymentResponse responseDTO = new PaymentResponse(DefaultPaymentTypes.THIRD_PARTY_ACCOUNT,
                PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);

        try {
            Authorization auth = getAuthorization(paymentRequest);
            auth = voidAuthorization(auth, paymentRequest);
            Amount amount = auth.getAmount();
            responseDTO
                    .successful(true)
                    .rawResponse(auth.toJSON())
                    .transactionType(DefaultTransactionTypes.REVERSE_AUTH)
                    .amount(new BigDecimal(amount.getTotal()))
                    .currencyContext(amount.getCurrency());
        } catch (PaymentException ex) {
            if (ex.getCause() instanceof PayPalRESTException) {
                PayPalRESTException restException = (PayPalRESTException) ex.getCause();
                responseDTO
                        .successful(false)
                        .rawResponse(restException.toString())
                        .transactionType(DefaultTransactionTypes.REVERSE_AUTH);
                populateErrorResponseMap(responseDTO, restException);
                return responseDTO;
            }
            throw ex;
        }

        return responseDTO;
    }

    @Override
    public PaymentResponse refund(PaymentRequest paymentRequest) throws PaymentException {
        PaymentResponse responseDTO = new PaymentResponse(DefaultPaymentTypes.THIRD_PARTY_ACCOUNT,
                PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);

        try {
            Capture capture = null;
            Sale sale = null;
            if (getCaptureId(paymentRequest) != null) {
                capture = getCapture(paymentRequest);
            } else if (getSaleId(paymentRequest) != null) {
                sale = getSale(paymentRequest);
            }

            if (capture != null) {
                DetailedRefund detailRefund = refundPayment(paymentRequest, capture);
                Amount amount = detailRefund.getAmount();
                responseDTO
                        .successful(true)
                        .rawResponse(detailRefund.toJSON())
                        .transactionType(DefaultTransactionTypes.REFUND)
                        .responseMap(MessageConstants.REFUNDID, detailRefund.getId())
                        .responseMap(MessageConstants.CAPTUREID, detailRefund.getCaptureId())
                        .amount(new BigDecimal(amount.getTotal()))
                        .currencyContext(amount.getCurrency());
                return responseDTO;
            } else if (sale != null) {
                DetailedRefund detailRefund = refundPayment(paymentRequest, sale);
                Amount amount = detailRefund.getAmount();
                responseDTO
                        .successful(true)
                        .rawResponse(detailRefund.toJSON())
                        .transactionType(DefaultTransactionTypes.REFUND)
                        .responseMap(MessageConstants.REFUNDID, detailRefund.getId())
                        .responseMap(MessageConstants.SALEID, detailRefund.getSaleId())
                        .amount(new BigDecimal(amount.getTotal()))
                        .currencyContext(amount.getCurrency());
                return responseDTO;
            }
        } catch (PaymentException ex) {
            if (ex.getCause() instanceof PayPalRESTException) {
                PayPalRESTException restException = (PayPalRESTException) ex.getCause();
                responseDTO
                        .successful(false)
                        .rawResponse(restException.toString())
                        .transactionType(DefaultTransactionTypes.REFUND);
                populateErrorResponseMap(responseDTO, restException);
                return responseDTO;
            }
            throw ex;
        }

        throw new PaymentException(
                "Unable to perform refund. Unable to find corresponding capture or sale transaction.");
    }

    @Override
    public PaymentResponse voidPayment(PaymentRequest paymentRequest) throws PaymentException {
        PaymentResponse responseDTO = new PaymentResponse(DefaultPaymentTypes.THIRD_PARTY_ACCOUNT,
                PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);

        try {
            Authorization auth = getAuthorization(paymentRequest);
            auth = voidAuthorization(auth, paymentRequest);
            Amount amount = auth.getAmount();
            responseDTO
                    .successful(true)
                    .rawResponse(auth.toJSON())
                    .transactionType(DefaultTransactionTypes.VOID)
                    .amount(new BigDecimal(amount.getTotal()))
                    .currencyContext(amount.getCurrency());
        } catch (PaymentException ex) {
            if (ex.getCause() instanceof PayPalRESTException) {
                PayPalRESTException restException = (PayPalRESTException) ex.getCause();
                responseDTO
                        .successful(false)
                        .rawResponse(restException.toString())
                        .transactionType(DefaultTransactionTypes.VOID);
                populateErrorResponseMap(responseDTO, restException);
                return responseDTO;
            }
            throw ex;
        }
        return responseDTO;
    }

    private final Capture capturePayment(PaymentRequest paymentRequest, Authorization auth)
            throws PaymentException {
        Capture capture = new Capture();
        capture.setIsFinalCapture(true);
        Amount amount = new Amount();
        amount.setCurrency(paymentRequest.getCurrencyCode());
        amount.setTotal(Objects.toString(paymentRequest.getTransactionTotal(), null));
        capture.setAmount(amount);
        PayPalCaptureResponse captureResponse = (PayPalCaptureResponse) paypalCheckoutService.call(
                new PayPalCaptureRequest(auth,
                        capture,
                        paypalCheckoutService.constructAPIContext(paymentRequest)));
        return captureResponse.getCapture();
    }

    private final PayPalResource authorizePayment(PaymentRequest paymentRequest)
            throws PaymentException {
        Payment payment = new Payment();
        payment.setId(getPaymentId(paymentRequest));
        payment.setTransactions(generateAuthorizeTransactions(paymentRequest));
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(getPayerId(paymentRequest));

        if (isBillingAgreementRequest(paymentRequest)) {
            payment.setIntent("authorize");
            payment.setPayer(generateAuthorizePayer(paymentRequest));
            PayPalCreatePaymentResponse response =
                    (PayPalCreatePaymentResponse) paypalCheckoutService.call(
                            new PayPalCreatePaymentRequest(payment,
                                    paypalCheckoutService.constructAPIContext(paymentRequest)));
            return response.getPayment();
        }


        payPalPaymentService.updatePayPalPaymentForFulfillment(paymentRequest);

        PayPalAuthorizeResponse response = (PayPalAuthorizeResponse) paypalCheckoutService.call(
                new PayPalAuthorizeRequest(payment,
                        paymentExecution,
                        paypalCheckoutService.constructAPIContext(paymentRequest)));
        return response.getAuthorization();
    }

    private final Payer generateAuthorizePayer(PaymentRequest paymentRequest) {
        if (isBillingAgreementRequest(paymentRequest)) {
            return generateBillingAgreementPayer(paymentRequest);
        }

        return null;
    }

    private final boolean isBillingAgreementRequest(PaymentRequest paymentRequest) {
        return paymentRequest.getAdditionalFields()
                .containsKey(MessageConstants.BILLINGAGREEMENTID);
    }

    private final List<Transaction> generateAuthorizeTransactions(PaymentRequest paymentRequest) {
        return generateTransactions(paymentRequest);
    }

    private final List<Transaction> generateTransactions(PaymentRequest paymentRequest) {
        Amount amount = paypalCheckoutService.getPayPalAmountFromOrder(paymentRequest);

        // Transaction information
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction
                .setDescription(
                        paypalCheckoutService.getConfigProperties().getPaymentDescription());
        transaction.setCustom(paymentRequest.getOrderId());

        ItemList itemList = paypalCheckoutService.getPayPalItemList(paymentRequest, true);
        if (itemList != null) {
            transaction.setItemList(itemList);
        }

        // Add transaction to a list
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);
        return transactions;
    }

    private final PayPalResource salePayment(PaymentRequest paymentRequest)
            throws PaymentException {
        Payment payment = new Payment();
        payment.setId(getPaymentId(paymentRequest));
        payment.setTransactions(generateSaleTransactions(paymentRequest));
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(getPayerId(paymentRequest));

        if (isBillingAgreementRequest(paymentRequest)) {
            payment.setIntent("sale");
            payment.setPayer(generateSalePayer(paymentRequest));
            PayPalCreatePaymentResponse response =
                    (PayPalCreatePaymentResponse) paypalCheckoutService.call(
                            new PayPalCreatePaymentRequest(payment,
                                    paypalCheckoutService.constructAPIContext(paymentRequest)));
            return response.getPayment();
        }

        PayPalSaleResponse response = (PayPalSaleResponse) paypalCheckoutService.call(
                new PayPalSaleRequest(payment,
                        paymentExecution,
                        paypalCheckoutService.constructAPIContext(paymentRequest)));
        return response.getSale();
    }

    private final Payer generateSalePayer(PaymentRequest paymentRequest) {
        if (isBillingAgreementRequest(paymentRequest)) {
            return generateBillingAgreementPayer(paymentRequest);
        }

        return null;
    }

    private final List<Transaction> generateSaleTransactions(PaymentRequest paymentRequest) {
        return generateTransactions(paymentRequest);
    }

    private final Payer generateBillingAgreementPayer(PaymentRequest paymentRequest) {
        Payer payer = new Payer();
        payer.setPaymentMethod(MessageConstants.PAYER_PAYMENTMETHOD_PAYPAL);
        List<FundingInstrument> fundingInstruments = new ArrayList<>();
        FundingInstrument billingAgreement = new FundingInstrument();
        Billing billing = new Billing();
        billing.setBillingAgreementId((String) paymentRequest.getAdditionalFields()
                .get(MessageConstants.BILLINGAGREEMENTID));
        billingAgreement.setBilling(billing);
        fundingInstruments.add(billingAgreement);
        payer.setFundingInstruments(fundingInstruments);
        return payer;
    }

    private final Authorization voidAuthorization(Authorization auth, PaymentRequest paymentRequest)
            throws PaymentException {
        PayPalVoidResponse response = (PayPalVoidResponse) paypalCheckoutService.call(
                new PayPalVoidRequest(auth,
                        paypalCheckoutService.constructAPIContext(paymentRequest)));
        return response.getVoidedAuthorization();
    }

    private final DetailedRefund refundPayment(PaymentRequest paymentRequest, Capture capture)
            throws PaymentException {
        RefundRequest refund = new RefundRequest();
        Amount amount = new Amount();
        amount.setCurrency(paymentRequest.getCurrencyCode());
        amount.setTotal(Objects.toString(paymentRequest.getTransactionTotal(), null));
        refund.setAmount(amount);
        PayPalRefundResponse response = (PayPalRefundResponse) paypalCheckoutService.call(
                new PayPalRefundRequest(refund,
                        capture,
                        paypalCheckoutService.constructAPIContext(paymentRequest)));
        return response.getDetailedRefund();
    }

    private final DetailedRefund refundPayment(PaymentRequest paymentRequest, Sale sale)
            throws PaymentException {
        RefundRequest refund = new RefundRequest();
        Amount amount = new Amount();
        amount.setCurrency(paymentRequest.getCurrencyCode());
        amount.setTotal(Objects.toString(paymentRequest.getTransactionTotal(), null));
        refund.setAmount(amount);
        PayPalRefundResponse response = (PayPalRefundResponse) paypalCheckoutService.call(
                new PayPalRefundRequest(refund,
                        sale,
                        paypalCheckoutService.constructAPIContext(paymentRequest)));
        return response.getDetailedRefund();
    }

    private final Authorization getAuthorization(PaymentRequest paymentRequest)
            throws PaymentException {
        PayPalAuthorizationRetrievalResponse authResponse =
                (PayPalAuthorizationRetrievalResponse) paypalCheckoutService.call(
                        new PayPalAuthorizationRetrievalRequest(getAuthorizationId(paymentRequest),
                                paypalCheckoutService.constructAPIContext(paymentRequest)));
        return authResponse.getAuthorization();
    }

    private final Sale getSale(PaymentRequest paymentRequest) throws PaymentException {
        PayPalSaleRetrievalResponse saleResponse =
                (PayPalSaleRetrievalResponse) paypalCheckoutService.call(
                        new PayPalSaleRetrievalRequest(getSaleId(paymentRequest),
                                paypalCheckoutService.constructAPIContext(paymentRequest)));
        return saleResponse.getSale();
    }

    private final Capture getCapture(PaymentRequest paymentRequest) throws PaymentException {
        PayPalCaptureRetrievalResponse response =
                (PayPalCaptureRetrievalResponse) paypalCheckoutService
                        .call((new PayPalCaptureRetrievalRequest(getCaptureId(paymentRequest),
                                paypalCheckoutService.constructAPIContext(paymentRequest))));
        return response.getCapture();
    }

    private final String getPaymentId(PaymentRequest paymentRequest) {
        return (String) paymentRequest.getAdditionalFields().get(MessageConstants.PAYMENTID);
    }

    private final String getPayerId(PaymentRequest paymentRequest) {
        return (String) paymentRequest.getAdditionalFields().get(MessageConstants.PAYERID);
    }

    private final String getAuthorizationId(PaymentRequest paymentRequest) {
        return (String) paymentRequest.getAdditionalFields().get(MessageConstants.AUTHORIZATONID);
    }

    private final String getSaleId(PaymentRequest paymentRequest) {
        return (String) paymentRequest.getAdditionalFields().get(MessageConstants.SALEID);
    }

    private final String getCaptureId(PaymentRequest paymentRequest) {
        return (String) paymentRequest.getAdditionalFields().get(MessageConstants.CAPTUREID);
    }

    private final void populateErrorResponseMap(PaymentResponse responseDTO,
            PayPalRESTException restException) {
        Error error = restException.getDetails();
        if (error != null) {
            responseDTO.responseMap(MessageConstants.EXCEPTION_NAME, error.getName())
                    .responseMap(MessageConstants.EXCEPTION_MESSAGE, error.getMessage())
                    .responseMap(MessageConstants.EXCEPTION_DEBUG_ID, error.getDebugId());
        }
    }

    @Override
    public String getGatewayType() {
        return PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT.name();
    }

}
