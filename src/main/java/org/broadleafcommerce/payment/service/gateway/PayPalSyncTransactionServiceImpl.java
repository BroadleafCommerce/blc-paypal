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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 * This takes the {@link PaymentRequestDTO} and call the {@link ReportingTransactions#get(Map, APIContext)} to read all the
 * {@link ReportingTransactionResponse} and then filter the result with matching  {@link TransactionInfo#getPaypal_reference_id()}
 * and {@link TransactionInfo#getCustom_field()}
 * @author venkat
 *
 */
public class PayPalSyncTransactionServiceImpl implements PayPalSyncTransactionService{

    private static final Log LOG = LogFactory.getLog(PayPalSyncTransactionServiceImpl.class);

    @Resource(name = "blExternalCallPayPalCheckoutService")
    protected ExternalCallPayPalCheckoutService payPalCheckoutService;

    @Override
    public PaymentResponseDTO findTransactionByQueryParams(PaymentRequestDTO paymentRequestDTO) {
        APIContext apiContext = payPalCheckoutService.constructAPIContext(paymentRequestDTO);
        Map<String,String> queryParamsMap=prepareQueryParameters(paymentRequestDTO);
        PaymentResponseDTO paymentResponseDTO=new PaymentResponseDTO(PaymentType.CREDIT_CARD, PaymentGatewayType.PASSTHROUGH);
        try {
            ReportingTransactionResponse reportingTransactions = executeTransactionSearchCall(apiContext,queryParamsMap);
            if(null!=reportingTransactions && null!=reportingTransactions.getTransaction_details()) {
                List<TransactionDetail> transaction_details = reportingTransactions.getTransaction_details();
                for (TransactionDetail transactionDetail : transaction_details) {
                      TransactionInfo transactionInfo = transactionDetail.getTransaction_info();
                      if(havingMatchBillingAgreementIDAndCustomField(paymentRequestDTO,transactionInfo)) {
                          paymentResponseDTO.getResponseMap().put(MessageConstants.TRANSACTION_STATUS, transactionInfo.getTransaction_status());
                      }
                }
            }
        } catch (PayPalRESTException e) {
            Integer pageNo = 1;
            if(null!=paymentRequestDTO.getAdditionalFields().get("PAGE_NO")) {
                pageNo = Integer.valueOf(paymentRequestDTO.getAdditionalFields().get("PAGE_NO").toString());
            }
            LOG.error("pageNo "+pageNo);
            LOG.error("Exception occured while fetching the Reporting transactions ",
                    e);
        }
        return paymentResponseDTO;
    }
    /**
     * Prepare the query parameters to pass to the Reporting Transaction API from the {@link PaymentRequestDTO}
     * @param paymentRequestDTO
     * @return
     */
    protected Map<String,String> prepareQueryParameters(PaymentRequestDTO paymentRequestDTO){
        Map<String,String> queryParamsMap=new HashMap<>();
        if(paymentRequestDTO.getAdditionalFields().get("start_date")!=null) {
            queryParamsMap.put("start_date", paymentRequestDTO.getAdditionalFields().get("start_date").toString());
        }
        if(paymentRequestDTO.getAdditionalFields().get("end_date")!=null) {
            queryParamsMap.put("end_date", paymentRequestDTO.getAdditionalFields().get("end_date").toString());
        }
        if(paymentRequestDTO.getAdditionalFields().get("transaction_amount")!=null) {
            queryParamsMap.put("transaction_amount", paymentRequestDTO.getAdditionalFields().get("transaction_amount").toString());
        }
        return queryParamsMap;
    }

    @Override
    public ReportingTransactionResponse getAllTransactionsByMatchingQueryParams(
            Map<String, String> queryFilter) {
        PaymentRequestDTO paymentRequestDTO=new PaymentRequestDTO();
        APIContext apiContext = payPalCheckoutService.constructAPIContext(paymentRequestDTO);
        ReportingTransactionResponse reportingTransactionsResponse=null;
        try {
            reportingTransactionsResponse = executeTransactionSearchCall(apiContext,queryFilter);
        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }
        return reportingTransactionsResponse;
    }

    /**
     * This calls the {@link ReportingTransactions#get(Map, APIContext)} to fetch the reporting transactions of type
     * {@link ReportingTransactions}
     * @param apiContext
     * @param queryParamsMap
     * @return
     * @throws PayPalRESTException
     */
    protected ReportingTransactionResponse executeTransactionSearchCall(APIContext apiContext,Map<String,String> queryParamsMap) throws PayPalRESTException {
        ReportingTransactions reeportingTransactions=new ReportingTransactions();
        return reeportingTransactions.get(queryParamsMap, apiContext);
    }

    /**
     * Get the BillingAgreementID from the {@link PaymentRequestDTO#getAdditionalFields()} having key {@link MessageConstants#BILLINGAGREEMENTID}
     * @param paymentRequestDTO
     * @return
     */
    protected String getBillingAgreementId(PaymentRequestDTO paymentRequestDTO) {
        if(null!=paymentRequestDTO.getAdditionalFields().get(MessageConstants.BILLINGAGREEMENTID)) {
            return paymentRequestDTO.getAdditionalFields().get(MessageConstants.BILLINGAGREEMENTID).toString();
        }
        return null;
    }

    /**
     * Get the BillingAgreementID from the {@link PaymentRequestDTO#getAdditionalFields()} having key {@link MessageConstants#CUSTOM_FIELD}
     * @param paymentRequestDTO
     * @return
     */
    protected String getCustomField(PaymentRequestDTO paymentRequestDTO) {
        if(null!=paymentRequestDTO.getAdditionalFields().get(MessageConstants.CUSTOM_FIELD)) {
            return paymentRequestDTO.getAdditionalFields().get(MessageConstants.CUSTOM_FIELD).toString();
        }
        return null;
    }

    /**
     * Return true
     *  if {@link TransactionInfo#getPaypal_reference_id()} matches the value of the {@link PaymentRequestDTO#getAdditionalFields()} mapping key {@link MessageConstants#BILLINGAGREEMENTID}
     *  and if {@link TransactionInfo#getCustom_field()} matches the value of the {@link PaymentRequestDTO#getAdditionalFields()} mapping key {@link MessageConstants#CUSTOM_FIELD}
     * @param paymentRequestDTO
     * @param transactionInfo
     * @return
     */
    protected boolean havingMatchBillingAgreementIDAndCustomField(PaymentRequestDTO paymentRequestDTO,TransactionInfo transactionInfo) {
        return transactionInfo.getPaypal_reference_id().equalsIgnoreCase(getBillingAgreementId(paymentRequestDTO))
                && (null!=getCustomField(paymentRequestDTO) && (getCustomField(paymentRequestDTO).equalsIgnoreCase(transactionInfo.getCustom_field())));


    }

}
