/*-
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2019 Broadleaf Commerce
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

import org.apache.commons.lang.StringUtils;
import org.broadleafcommerce.common.payment.PaymentGatewayType;
import org.broadleafcommerce.common.payment.PaymentType;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.vendor.paypal.api.ReportingTransactions;
import org.broadleafcommerce.vendor.paypal.domain.ReportingTransactionResponse;
import org.broadleafcommerce.vendor.paypal.domain.TransactionDetail;
import org.broadleafcommerce.vendor.paypal.domain.TransactionInfo;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;

import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

/**
 * This takes the {@link PaymentRequestDTO} and calls the {@link ReportingTransactions#get(Map, APIContext)} to read all the
 * {@link ReportingTransactionResponse} and then filters the results by matching the {@link TransactionInfo#getPaypal_reference_id()}
 * and {@link TransactionInfo#getCustom_field()}
 *
 * Note: in the PayPal payload, the {@link TransactionInfo#getCustom_field()} is used to capture and custom info that we want.
 * This class assumes that a BLC-produced transaction id is being sent to PayPal.
 *
 * @author venkat
 *
 */
public class PayPalSyncTransactionServiceImpl implements PayPalSyncTransactionService {

    @Resource(name = "blExternalCallPayPalCheckoutService")
    protected ExternalCallPayPalCheckoutService payPalCheckoutService;

    @Override
    public PaymentResponseDTO lookupTransactionByQueryParams(PaymentRequestDTO paymentRequestDTO) throws PayPalRESTException {
        APIContext apiContext = payPalCheckoutService.constructAPIContext(paymentRequestDTO);
        Map<String,String> queryParamsMap = prepareQueryParameters(paymentRequestDTO);
        PaymentResponseDTO paymentResponseDTO = new PaymentResponseDTO(PaymentType.CREDIT_CARD, PaymentGatewayType.PASSTHROUGH);

        ReportingTransactionResponse reportingTransactions = executeTransactionSearch(apiContext,queryParamsMap);

        if (null != reportingTransactions && null != reportingTransactions.getTransaction_details()) {
            List<TransactionDetail> transactionDetails = reportingTransactions.getTransaction_details();

            for (TransactionDetail transactionDetail : transactionDetails) {
                  TransactionInfo transactionInfo = transactionDetail.getTransaction_info();

                  if (hasMatchingBillingAgreementID(paymentRequestDTO, transactionInfo)
                          && hasMatchingTransactionId(paymentRequestDTO, transactionInfo)) {
                      String transactionStatus = transactionInfo.getTransaction_status();

                      paymentResponseDTO.getResponseMap().put(MessageConstants.TRANSACTION_STATUS, transactionStatus);
                  }
            }
        }

        return paymentResponseDTO;
    }

    /**
     * Prepare the query parameters to pass to the Reporting Transaction API from the {@link PaymentRequestDTO}
     * @param paymentRequestDTO
     * @return
     */
    protected Map<String,String> prepareQueryParameters(PaymentRequestDTO paymentRequestDTO) {
        Map<String,String> queryParamsMap = new HashMap<>();

        Map<String, Object> additionalFields = paymentRequestDTO.getAdditionalFields();
        if (additionalFields.get("start_date") != null) {
            queryParamsMap.put("start_date", additionalFields.get("start_date").toString());
        }
        if (additionalFields.get("end_date") != null) {
            queryParamsMap.put("end_date", additionalFields.get("end_date").toString());
        }
        if (additionalFields.get("transaction_amount") != null) {
            queryParamsMap.put("transaction_amount", additionalFields.get("transaction_amount").toString());
        }

        return queryParamsMap;
    }

    @Override
    public ReportingTransactionResponse lookupTransactionsByQueryParams(
            Map<String, String> queryFilter) throws PayPalRESTException {
        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO();
        APIContext apiContext = payPalCheckoutService.constructAPIContext(paymentRequestDTO);

        return executeTransactionSearch(apiContext, queryFilter);
    }

    /**
     * This calls the {@link ReportingTransactions#get(Map, APIContext)} to fetch the reporting transactions of type
     * {@link ReportingTransactions}
     * @param apiContext
     * @param queryParamsMap
     * @return
     * @throws PayPalRESTException
     */
    protected ReportingTransactionResponse executeTransactionSearch(APIContext apiContext,
                                                                    Map<String,String> queryParamsMap) throws PayPalRESTException {
        ReportingTransactions reportingTransactions = new ReportingTransactions();

        return reportingTransactions.get(queryParamsMap, apiContext);
    }

    /**
     * Get the BillingAgreementID from the {@link PaymentRequestDTO#getAdditionalFields()} having key {@link MessageConstants#BILLINGAGREEMENTID}
     * @param paymentRequestDTO
     * @return
     */
    protected String getBillingAgreementId(PaymentRequestDTO paymentRequestDTO) {
        Map<String, Object> additionalFields = paymentRequestDTO.getAdditionalFields();

        if (null != additionalFields.get(MessageConstants.BILLINGAGREEMENTID)) {
            return additionalFields.get(MessageConstants.BILLINGAGREEMENTID).toString();
        }

        return null;
    }

    /**
     * Get the BLC-defined transactionId from the {@link PaymentRequestDTO#getAdditionalFields()}
     * having key {@link MessageConstants#CUSTOM_FIELD}.
     *
     * @param paymentRequestDTO
     * @return
     */
    protected String getTransactionId(PaymentRequestDTO paymentRequestDTO) {
        Map<String, Object> additionalFields = paymentRequestDTO.getAdditionalFields();

        if (null != additionalFields.get(MessageConstants.CUSTOM_FIELD)) {
            return additionalFields.get(MessageConstants.CUSTOM_FIELD).toString();
        }

        return null;
    }

    /**
     * Return true
     *  if {@link TransactionInfo#getPaypal_reference_id()} matches the value of the
     *  {@link PaymentRequestDTO#getAdditionalFields()} mapping key {@link MessageConstants#BILLINGAGREEMENTID}
     * @param paymentRequestDTO
     * @param transactionInfo
     * @return
     */
    protected boolean hasMatchingBillingAgreementID(PaymentRequestDTO paymentRequestDTO, TransactionInfo transactionInfo) {
        String paypalReferenceId = transactionInfo.getPaypal_reference_id();
        String billingAgreementId = getBillingAgreementId(paymentRequestDTO);

        return StringUtils.equalsIgnoreCase(paypalReferenceId, billingAgreementId);
    }

    /**
     * Return true
     *  if {@link TransactionInfo#getCustom_field()} matches the value of the
     *  {@link PaymentRequestDTO#getAdditionalFields()} mapping key {@link MessageConstants#CUSTOM_FIELD}
     * @param paymentRequestDTO
     * @param transactionInfo
     * @return
     */
    protected boolean hasMatchingTransactionId(PaymentRequestDTO paymentRequestDTO, TransactionInfo transactionInfo) {
        String customField = transactionInfo.getCustom_field();
        String transactionId = getTransactionId(paymentRequestDTO);

        return StringUtils.equalsIgnoreCase(customField, transactionId);
    }

}
