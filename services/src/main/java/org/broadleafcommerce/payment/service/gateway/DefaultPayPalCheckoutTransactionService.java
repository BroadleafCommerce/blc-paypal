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

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.lang.Nullable;
import org.springframework.retry.support.RetryTemplate;

import com.broadleafcommerce.money.util.MonetaryUtils;
import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.domain.PaymentResponse;
import com.broadleafcommerce.paymentgateway.domain.enums.DefaultPaymentTypes;
import com.broadleafcommerce.paymentgateway.domain.enums.DefaultTransactionFailureTypes;
import com.broadleafcommerce.paymentgateway.domain.enums.DefaultTransactionTypes;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Authorization;
import com.paypal.api.payments.Billing;
import com.paypal.api.payments.Capture;
import com.paypal.api.payments.DetailedRefund;
import com.paypal.api.payments.Error;
import com.paypal.api.payments.FundingInstrument;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RefundRequest;
import com.paypal.api.payments.RelatedResources;
import com.paypal.api.payments.Sale;
import com.paypal.api.payments.Transaction;
import com.paypal.api.payments.Transactions;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.base.rest.PayPalResource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@CommonsLog
@RequiredArgsConstructor
public class DefaultPayPalCheckoutTransactionService implements PayPalCheckoutTransactionService {

    @Getter(AccessLevel.PROTECTED)
    private final PayPalCheckoutExternalCallService paypalCheckoutService;

    @Getter(AccessLevel.PROTECTED)
    private final PayPalPaymentService payPalPaymentService;

    @Getter(AccessLevel.PROTECTED)
    private final PayPalCheckoutRestConfigurationProperties configProperties;

    @Getter(AccessLevel.PROTECTED)
    private final RetryTemplate retryTemplate;

    @Override
    public PaymentResponse authorize(PaymentRequest paymentRequest) {
        PaymentResponse paymentResponse =
                new PaymentResponse(DefaultPaymentTypes.THIRD_PARTY_ACCOUNT,
                        PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT)
                                .transactionType(DefaultTransactionTypes.AUTHORIZE);

        try {
            recordTransactionReferenceIdOnPayment(paymentRequest);

            PayPalResource auth = authorizePayment(paymentRequest);
            if (auth instanceof Payment) {
                Payment payment = (Payment) auth;
                Transaction transaction = getTransaction(payment);
                Amount amount = transaction.getAmount();
                Authorization authorization = getAuthorization(transaction);

                paymentResponse
                        .successful(true)
                        .amount(MonetaryUtils.toAmount(amount.getTotal(), amount.getCurrency()))
                        .dateRecorded(Instant.parse(payment.getCreateTime()))
                        .transactionReferenceId(transaction.getCustom())
                        .responseMap(MessageConstants.AUTHORIZATONID, authorization.getId())
                        .rawResponse(payment.toJSON());
            } else {
                Authorization authorization = (Authorization) auth;
                Amount amount = authorization.getAmount();
                paymentResponse
                        .successful(true)
                        .amount(MonetaryUtils.toAmount(amount.getTotal(), amount.getCurrency()))
                        .dateRecorded(Instant.parse(authorization.getCreateTime()))
                        .transactionReferenceId(authorization.getReferenceId())
                        .responseMap(MessageConstants.AUTHORIZATONID, authorization.getId())
                        .rawResponse(authorization.toJSON());
            }
        } catch (Exception e) {
            processException(e, paymentResponse, paymentRequest);
        }
        return paymentResponse;
    }

    @Override
    public PaymentResponse capture(PaymentRequest paymentRequest) {
        PaymentResponse paymentResponse =
                new PaymentResponse(DefaultPaymentTypes.THIRD_PARTY_ACCOUNT,
                        PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT)
                                .transactionType(DefaultTransactionTypes.CAPTURE);

        try {
            Authorization auth = getAuthorization(paymentRequest);
            Capture capture = capturePayment(auth, paymentRequest);
            Amount amount = capture.getAmount();

            paymentResponse
                    .successful(true)
                    .rawResponse(capture.toJSON())
                    .responseMap(MessageConstants.CAPTUREID, capture.getId())
                    .amount(MonetaryUtils.toAmount(amount.getTotal(), amount.getCurrency()));
        } catch (PaymentException e) {
            processException(e, paymentResponse, paymentRequest);
        }
        return paymentResponse;
    }

    @Override
    public PaymentResponse authorizeAndCapture(PaymentRequest paymentRequest) {
        PaymentResponse paymentResponse =
                new PaymentResponse(DefaultPaymentTypes.THIRD_PARTY_ACCOUNT,
                        PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT)
                                .transactionType(DefaultTransactionTypes.AUTHORIZE_AND_CAPTURE);

        try {
            recordTransactionReferenceIdOnPayment(paymentRequest);

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

                    paymentResponse
                            .successful(true)
                            .rawResponse(payment.toJSON())
                            .responseMap(MessageConstants.PAYMENTID, payment.getId())
                            .responseMap(MessageConstants.SALEID, saleId)
                            .responseMap(MessageConstants.BILLINGAGREEMENTID, billingAgreementId)
                            .responseMap(MessageConstants.PAYER_INFO_EMAIL, payerEmail)
                            .responseMap(MessageConstants.PAYER_INFO_FIRST_NAME, payerFirstName)
                            .responseMap(MessageConstants.PAYER_INFO_LAST_NAME, payerLastName)
                            .amount(MonetaryUtils.toAmount(amount.getTotal(),
                                    amount.getCurrency()));
                }
            } else {
                Sale sale = (Sale) salePayment;
                Amount amount = sale.getAmount();
                paymentResponse
                        .successful(true)
                        .rawResponse(sale.toJSON())
                        .responseMap(MessageConstants.SALEID, sale.getId())
                        .responseMap(MessageConstants.BILLINGAGREEMENTID,
                                sale.getBillingAgreementId())
                        .amount(MonetaryUtils.toAmount(amount.getTotal(), amount.getCurrency()));
            }
        } catch (PaymentException e) {
            processException(e, paymentResponse, paymentRequest);
        }

        return paymentResponse;
    }

    @Override
    public PaymentResponse reverseAuthorize(PaymentRequest paymentRequest) {
        PaymentResponse paymentResponse =
                new PaymentResponse(DefaultPaymentTypes.THIRD_PARTY_ACCOUNT,
                        PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT)
                                .transactionType(DefaultTransactionTypes.REVERSE_AUTH);

        try {
            Authorization auth = getAuthorization(paymentRequest);
            auth = voidAuthorization(auth, paymentRequest);
            Amount amount = auth.getAmount();
            paymentResponse
                    .successful(true)
                    .amount(MonetaryUtils.toAmount(amount.getTotal(), amount.getCurrency()))
                    .dateRecorded(Instant.parse(auth.getUpdateTime()))
                    .rawResponse(auth.toJSON());
        } catch (PaymentException e) {
            processException(e, paymentResponse, paymentRequest);
        }

        return paymentResponse;
    }

    @Override
    public PaymentResponse refund(PaymentRequest paymentRequest) {
        PaymentResponse paymentResponse =
                new PaymentResponse(DefaultPaymentTypes.THIRD_PARTY_ACCOUNT,
                        PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT)
                                .transactionType(DefaultTransactionTypes.REFUND);

        try {
            Capture capture = null;
            Sale sale = null;
            if (getCaptureId(paymentRequest) != null) {
                capture = getCapture(paymentRequest);
            } else if (getSaleId(paymentRequest) != null) {
                sale = getSale(paymentRequest);
            }

            if (capture != null) {
                DetailedRefund detailRefund = refundPayment(capture, paymentRequest);
                Amount amount = detailRefund.getAmount();
                paymentResponse
                        .successful(true)
                        .rawResponse(detailRefund.toJSON())
                        .responseMap(MessageConstants.REFUNDID, detailRefund.getId())
                        .responseMap(MessageConstants.CAPTUREID, detailRefund.getCaptureId())
                        .amount(MonetaryUtils.toAmount(amount.getTotal(), amount.getCurrency()));
                return paymentResponse;
            } else if (sale != null) {
                DetailedRefund detailRefund = refundPayment(sale, paymentRequest);
                Amount amount = detailRefund.getAmount();
                paymentResponse
                        .successful(true)
                        .rawResponse(detailRefund.toJSON())
                        .responseMap(MessageConstants.REFUNDID, detailRefund.getId())
                        .responseMap(MessageConstants.SALEID, detailRefund.getSaleId())
                        .amount(MonetaryUtils.toAmount(amount.getTotal(), amount.getCurrency()));
                return paymentResponse;
            }
        } catch (PaymentException e) {
            processException(e, paymentResponse, paymentRequest);
        }

        throw new PaymentException(
                "Unable to perform refund. Unable to find corresponding capture or sale transaction.");
    }

    @Override
    public PaymentResponse voidPayment(PaymentRequest paymentRequest) {
        PaymentResponse paymentResponse =
                new PaymentResponse(DefaultPaymentTypes.THIRD_PARTY_ACCOUNT,
                        PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT)
                                .transactionType(DefaultTransactionTypes.VOID);


        try {
            Authorization auth = getAuthorization(paymentRequest);
            auth = voidAuthorization(auth, paymentRequest);
            Amount amount = auth.getAmount();
            paymentResponse
                    .successful(true)
                    .rawResponse(auth.toJSON())
                    .amount(MonetaryUtils.toAmount(amount.getTotal(),
                            auth.getAmount().getCurrency()));
        } catch (PaymentException e) {
            processException(e, paymentResponse, paymentRequest);
        }
        return paymentResponse;
    }

    /**
     * Executes a {@link DefaultTransactionTypes#CAPTURE} for the provided {@link Authorization}
     * object
     *
     * @param auth The authorization that should be captured
     * @param paymentRequest The request payload that should be used to form the transaction
     * @return a {@link Capture} object representing the final state of the transaction
     */
    protected Capture capturePayment(Authorization auth, PaymentRequest paymentRequest) {
        APIContext apiContext = paypalCheckoutService.constructAPIContext(paymentRequest);

        Capture capture = new Capture();
        capture.setIsFinalCapture(true);
        Amount amount = new Amount();
        amount.setCurrency(paymentRequest.getOrderSubtotal().getCurrency().getCurrencyCode());
        amount.setTotal(Objects.toString(paymentRequest.getTransactionTotal(), null));
        capture.setAmount(amount);

        PayPalCaptureResponse response = retryTemplate.execute(context -> {
            PayPalCaptureRequest captureRequest =
                    new PayPalCaptureRequest(auth, capture, apiContext);
            return (PayPalCaptureResponse) paypalCheckoutService.call(captureRequest);
        });
        return response.getCapture();
    }

    /**
     * Saves the {@link PaymentRequest#getTransactionReferenceId()} on the {@link Payment}
     *
     * @param paymentRequest the object that holds the transactionReferenceId & a reference to the
     *        Payment
     * @throws PaymentException thrown if the Payment update fails
     */
    protected void recordTransactionReferenceIdOnPayment(
            @lombok.NonNull PaymentRequest paymentRequest) {
        String transactionReferenceId = paymentRequest.getTransactionReferenceId();

        if (StringUtils.isNotBlank(transactionReferenceId)) {
            String paymentId =
                    (String) paymentRequest.getAdditionalField(MessageConstants.PAYMENTID);

            payPalPaymentService.updatePaymentCustom(paymentId, transactionReferenceId);
        }
    }

    /**
     * Executes an {@link DefaultTransactionTypes#AUTHORIZE} transaction based on the provided
     * {@link PaymentRequest}
     *
     * @param paymentRequest The request payload that should be used to form the transaction
     * @return a {@link PayPalResource} representing the final state of the transaction
     */
    protected PayPalResource authorizePayment(PaymentRequest paymentRequest) {
        Payment payment = new Payment();
        payment.setId(getPaymentId(paymentRequest));
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(getPayerId(paymentRequest));
        paymentExecution.setTransactions(generateAuthorizeTransactions(paymentRequest));

        APIContext apiContext = paypalCheckoutService.constructAPIContext(paymentRequest);

        if (isBillingAgreementRequest(paymentRequest)) {
            payment.setIntent("authorize");
            payment.setPayer(generateAuthorizePayer(paymentRequest));

            PayPalCreatePaymentResponse response = retryTemplate.execute(context -> {
                PayPalCreatePaymentRequest createPaymentRequest =
                        new PayPalCreatePaymentRequest(payment, apiContext);
                return (PayPalCreatePaymentResponse) paypalCheckoutService
                        .call(createPaymentRequest);
            });
            return response.getPayment();
        }

        PayPalAuthorizeResponse response = retryTemplate.execute(context -> {
            PayPalAuthorizeRequest authorizeRequest =
                    new PayPalAuthorizeRequest(payment, paymentExecution, apiContext);
            return (PayPalAuthorizeResponse) paypalCheckoutService.call(authorizeRequest);
        });
        return response.getAuthorizedPayment();
    }

    /**
     * Generates a {@link Payer} for an {@link DefaultTransactionTypes#AUTHORIZE} transaction based
     * on the provided {@link PaymentRequest}
     *
     * @param paymentRequest The request payload that should be used to form the payer
     * @return a payer based on the payment request
     */
    @Nullable
    protected Payer generateAuthorizePayer(PaymentRequest paymentRequest) {
        if (isBillingAgreementRequest(paymentRequest)) {
            return generateBillingAgreementPayer(paymentRequest);
        }

        return null;
    }

    private boolean isBillingAgreementRequest(PaymentRequest paymentRequest) {
        return paymentRequest.getAdditionalFields()
                .containsKey(MessageConstants.BILLINGAGREEMENTID);
    }

    /**
     * Generates a list of {@link Transactions} for an {@link DefaultTransactionTypes#AUTHORIZE}
     * transaction based on the provided {@link PaymentRequest}
     *
     * @param paymentRequest The request payload that should be used to form the transactions
     * @return a list of transactions based on the payment request
     */
    protected List<Transactions> generateAuthorizeTransactions(PaymentRequest paymentRequest) {
        return generateTransactions(paymentRequest);
    }

    /**
     * Generates a list of {@link Transactions} based on the provided {@link PaymentRequest}
     *
     * @param paymentRequest The request payload that should be used to form the transactions
     * @return a list of transactions based on the payment request
     */
    protected List<Transactions> generateTransactions(PaymentRequest paymentRequest) {

        // Transaction information
        Transactions transaction = new Transactions();
        Amount amount = paypalCheckoutService.getPayPalAmountFromOrder(paymentRequest);
        transaction.setAmount(amount);

        return Collections.singletonList(transaction);
    }

    private Transaction getTransaction(Payment payment) {
        return payment.getTransactions().stream()
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    private Authorization getAuthorization(Transaction transaction) {
        return transaction.getRelatedResources().stream()
                .map(RelatedResources::getAuthorization)
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    /**
     * Executes an {@link DefaultTransactionTypes#AUTHORIZE_AND_CAPTURE} transaction based on the
     * provided {@link PaymentRequest}
     *
     * @param paymentRequest The request payload that should be used to form the transaction
     * @return a {@link PayPalResource} representing the final state of the transaction
     */
    protected PayPalResource salePayment(PaymentRequest paymentRequest) {
        Payment payment = new Payment();
        payment.setId(getPaymentId(paymentRequest));
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(getPayerId(paymentRequest));
        paymentExecution.setTransactions(generateSaleTransactions(paymentRequest));

        APIContext apiContext = paypalCheckoutService.constructAPIContext(paymentRequest);

        if (isBillingAgreementRequest(paymentRequest)) {
            payment.setIntent("sale");
            payment.setPayer(generateSalePayer(paymentRequest));

            PayPalCreatePaymentResponse response = retryTemplate.execute(context -> {
                PayPalCreatePaymentRequest createPaymentRequest =
                        new PayPalCreatePaymentRequest(payment, apiContext);
                return (PayPalCreatePaymentResponse) paypalCheckoutService
                        .call(createPaymentRequest);
            });
            return response.getPayment();
        }

        PayPalSaleResponse response = retryTemplate.execute(context -> {
            PayPalSaleRequest saleRequest =
                    new PayPalSaleRequest(payment, paymentExecution, apiContext);
            return (PayPalSaleResponse) paypalCheckoutService.call(saleRequest);
        });
        return response.getSale();
    }

    /**
     * Generates a {@link Payer} for an {@link DefaultTransactionTypes#AUTHORIZE_AND_CAPTURE}
     * transaction based on the provided {@link PaymentRequest}
     *
     * @param paymentRequest The request payload that should be used to form the payer
     * @return a payer based on the payment request
     */
    @Nullable
    protected Payer generateSalePayer(PaymentRequest paymentRequest) {
        return generateAuthorizePayer(paymentRequest);
    }

    /**
     * Generates a list of {@link Transactions} for an
     * {@link DefaultTransactionTypes#AUTHORIZE_AND_CAPTURE} transaction based on the provided
     * {@link PaymentRequest}
     *
     * @param paymentRequest The request payload that should be used to form the transactions
     * @return a list of transactions based on the payment request
     */
    protected List<Transactions> generateSaleTransactions(PaymentRequest paymentRequest) {
        return generateTransactions(paymentRequest);
    }

    /**
     * Generates a {@link Payer} based on the provided {@link PaymentRequest}
     *
     * @param paymentRequest The request payload that should be used to form the payer
     * @return a payer based on the payment request
     */
    protected Payer generateBillingAgreementPayer(PaymentRequest paymentRequest) {
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

    /**
     * Executes a {@link DefaultTransactionTypes#REVERSE_AUTH} for the provided
     * {@link Authorization} object
     *
     * @param auth The authorization that should be reversed
     * @param paymentRequest The request payload that should be used to form the transaction
     * @return an updated {@link Authorization} representing the final state of the transaction
     */
    protected Authorization voidAuthorization(Authorization auth, PaymentRequest paymentRequest) {
        APIContext apiContext = paypalCheckoutService.constructAPIContext(paymentRequest);

        PayPalVoidResponse response = retryTemplate.execute(context -> {
            PayPalVoidRequest voidRequest = new PayPalVoidRequest(auth, apiContext);
            return (PayPalVoidResponse) paypalCheckoutService.call(voidRequest);
        });
        return response.getVoidedAuthorization();
    }

    /**
     * Executes a {@link DefaultTransactionTypes#REFUND} for the provided {@link Capture} object
     *
     * @param capture The capture that should be refunded
     * @param paymentRequest The request payload that should be used to form the transaction
     * @return a {@link DetailedRefund} representing the final state of the transaction
     */
    protected DetailedRefund refundPayment(Capture capture, PaymentRequest paymentRequest) {
        APIContext apiContext = paypalCheckoutService.constructAPIContext(paymentRequest);

        RefundRequest refund = new RefundRequest();
        Amount amount = new Amount();
        amount.setCurrency(paymentRequest.getOrderSubtotal().getCurrency().getCurrencyCode());
        amount.setTotal(Objects.toString(paymentRequest.getTransactionTotal(), null));
        refund.setAmount(amount);

        PayPalRefundResponse response = retryTemplate.execute(context -> {
            PayPalRefundRequest refundRequest =
                    new PayPalRefundRequest(refund, capture, apiContext);
            return (PayPalRefundResponse) paypalCheckoutService.call(refundRequest);
        });
        return response.getDetailedRefund();
    }

    /**
     * Executes a {@link DefaultTransactionTypes#REFUND} for the provided {@link Sale} object
     *
     * @param sale The sale that should be refunded
     * @param paymentRequest The request payload that should be used to form the transaction
     * @return a {@link DetailedRefund} representing the final state of the transaction
     */
    protected DetailedRefund refundPayment(Sale sale, PaymentRequest paymentRequest) {
        APIContext apiContext = paypalCheckoutService.constructAPIContext(paymentRequest);

        RefundRequest refund = new RefundRequest();
        Amount amount = new Amount();
        amount.setCurrency(paymentRequest.getOrderSubtotal().getCurrency().getCurrencyCode());
        amount.setTotal(Objects.toString(paymentRequest.getTransactionTotal(), null));
        refund.setAmount(amount);

        PayPalRefundResponse response = retryTemplate.execute(context -> {
            PayPalRefundRequest refundRequest = new PayPalRefundRequest(refund, sale, apiContext);
            return (PayPalRefundResponse) paypalCheckoutService.call(refundRequest);
        });
        return response.getDetailedRefund();
    }

    private Authorization getAuthorization(PaymentRequest paymentRequest) {
        APIContext apiContext = paypalCheckoutService.constructAPIContext(paymentRequest);
        String authorizationId = getAuthorizationId(paymentRequest);

        PayPalAuthorizationRetrievalResponse response = retryTemplate.execute(context -> {
            PayPalAuthorizationRetrievalRequest authorizationRetrievalRequest =
                    new PayPalAuthorizationRetrievalRequest(authorizationId, apiContext);
            return (PayPalAuthorizationRetrievalResponse) paypalCheckoutService
                    .call(authorizationRetrievalRequest);
        });
        return response.getAuthorization();
    }

    private Sale getSale(PaymentRequest paymentRequest) {
        APIContext apiContext = paypalCheckoutService.constructAPIContext(paymentRequest);
        String saleId = getSaleId(paymentRequest);

        PayPalSaleRetrievalResponse response = retryTemplate.execute(context -> {
            PayPalSaleRetrievalRequest saleRetrievalRequest =
                    new PayPalSaleRetrievalRequest(saleId, apiContext);
            return (PayPalSaleRetrievalResponse) paypalCheckoutService.call(saleRetrievalRequest);
        });
        return response.getSale();
    }

    private Capture getCapture(PaymentRequest paymentRequest) {
        APIContext apiContext = paypalCheckoutService.constructAPIContext(paymentRequest);
        String captureId = getCaptureId(paymentRequest);

        PayPalCaptureRetrievalResponse response = retryTemplate.execute(context -> {
            PayPalCaptureRetrievalRequest captureRetrievalRequest =
                    new PayPalCaptureRetrievalRequest(captureId, apiContext);
            return (PayPalCaptureRetrievalResponse) paypalCheckoutService
                    .call(captureRetrievalRequest);
        });
        return response.getCapture();
    }

    private String getPaymentId(PaymentRequest paymentRequest) {
        return (String) paymentRequest.getAdditionalFields().get(MessageConstants.PAYMENTID);
    }

    private String getPayerId(PaymentRequest paymentRequest) {
        return (String) paymentRequest.getAdditionalFields().get(MessageConstants.PAYERID);
    }

    private String getAuthorizationId(PaymentRequest paymentRequest) {
        return (String) paymentRequest.getAdditionalFields().get(MessageConstants.AUTHORIZATONID);
    }

    @Nullable
    private String getSaleId(PaymentRequest paymentRequest) {
        return (String) paymentRequest.getAdditionalFields().get(MessageConstants.SALEID);
    }

    @Nullable
    private String getCaptureId(PaymentRequest paymentRequest) {
        return (String) paymentRequest.getAdditionalFields().get(MessageConstants.CAPTUREID);
    }

    /**
     * This method is responsible for levering the exception, paymentRequest, & transactionType to
     * populate the paymentResponse instead of allowing the exception to be thrown.
     *
     * @param e the exception indicating a failed payment gateway transaction
     * @param paymentResponse the object that will hold the transaction results
     * @param paymentRequest the request that was used to execute the transaction
     */
    protected void processException(Exception e,
            PaymentResponse paymentResponse,
            PaymentRequest paymentRequest) {
        paymentResponse.successful(false);
        paymentResponse.dateRecorded(Instant.now());
        paymentResponse.transactionReferenceId(paymentRequest.getTransactionReferenceId());

        Throwable cause = e.getCause();
        if (cause instanceof PayPalRESTException) {
            PayPalRESTException restException = (PayPalRESTException) cause;
            Error details = restException.getDetails();
            String errorCode = details.getName();

            paymentResponse.gatewayResponseCode(errorCode);
            paymentResponse.rawResponse(restException.toString());
            populateErrorResponseMap(paymentResponse, restException);

            int httpResponseCode = restException.getResponsecode();
            if (400 == httpResponseCode) {
                if (isPaymentDecline(errorCode)) {
                    paymentResponse
                            .failureType(DefaultTransactionFailureTypes.PROCESSING_FAILURE.name());
                } else {
                    paymentResponse
                            .failureType(DefaultTransactionFailureTypes.INVALID_REQUEST.name());

                    String errorMessage = String.format(
                            "An invalid request was supplied to PayPal's API. Exception message: %s",
                            cause.getMessage());

                    paymentResponse.responseMap(MessageConstants.ERROR_MESSAGE, errorMessage);
                    log.error(errorMessage);
                }
            } else if (401 == httpResponseCode) {
                paymentResponse
                        .failureType(
                                DefaultTransactionFailureTypes.GATEWAY_CREDENTIALS_ERROR.name());

                String errorMessage = String.format(
                        "Authentication with PayPal's API failed. Maybe you changed client id or client secret recently. Exception message: %s",
                        cause.getMessage());

                paymentResponse.responseMap(MessageConstants.ERROR_MESSAGE, errorMessage);
                log.error(errorMessage);
            } else if (403 == httpResponseCode) {
                paymentResponse
                        .failureType(
                                DefaultTransactionFailureTypes.GATEWAY_CREDENTIALS_ERROR.name());

                String errorMessage = String.format(
                        "PayPal authorization failed due to insufficient permissions.. Exception message: %s",
                        cause.getMessage());

                paymentResponse.responseMap(MessageConstants.ERROR_MESSAGE, errorMessage);
                log.error(errorMessage);
            } else if (408 == httpResponseCode) {
                paymentResponse.failureType(DefaultTransactionFailureTypes.NETWORK_ERROR.name());

                String errorMessage = String.format(
                        "Network communication with Stripe failed. Exception message: %s",
                        cause.getMessage());

                paymentResponse.responseMap(MessageConstants.ERROR_MESSAGE, errorMessage);
                log.error(errorMessage);
            } else if (429 == httpResponseCode && "RATE_LIMIT_REACHED".equals(errorCode)) {
                paymentResponse
                        .failureType(DefaultTransactionFailureTypes.API_RATE_LIMIT_ERROR.name());

                String errorMessage = String.format(
                        "Too many requests made to the PayPal API too quickly. Exception message: %s",
                        cause.getMessage());

                paymentResponse.responseMap(MessageConstants.ERROR_MESSAGE, errorMessage);
                log.warn(errorMessage);
            } else if (500 == httpResponseCode || 503 == httpResponseCode) {
                paymentResponse.failureType(DefaultTransactionFailureTypes.GATEWAY_ERROR.name());
            } else {
                paymentResponse.failureType(DefaultTransactionFailureTypes.INVALID_REQUEST.name());

                String errorMessage = String.format(
                        "An invalid request was supplied to PayPal's API. Exception message: %s",
                        cause.getMessage());

                paymentResponse.responseMap(MessageConstants.ERROR_MESSAGE, errorMessage);
                log.error(errorMessage);
            }
        } else {
            paymentResponse.failureType(DefaultTransactionFailureTypes.INTERNAL_ERROR.name());
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Since PayPal uses a 400 HTTP response for many different types of failures, this method
     * leverages the errorCode to determine if there was an error in the processing of the payment.
     * For example, a credit card decline should be considered a processing failure, whereas a
     * request validation issue would not.
     *
     * @param errorCode the error code returned by PayPal
     * @return whether or not the failure was due to the payment method being declined
     */
    protected boolean isPaymentDecline(String errorCode) {
        List<String> paymentDeclineCodes = configProperties.getPaymentDeclineCodes();
        return paymentDeclineCodes.contains(errorCode);
    }

    private void populateErrorResponseMap(PaymentResponse responseDTO,
            PayPalRESTException restException) {
        Error error = restException.getDetails();
        if (error != null) {
            responseDTO.responseMap(MessageConstants.ERROR_MESSAGE, error.getMessage());
            responseDTO.responseMap(MessageConstants.ERROR_DEBUG_ID, error.getDebugId());
        }
    }

    @Override
    public String getGatewayType() {
        return PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT.name();
    }

}
