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

import org.broadleafcommerce.vendor.paypal.api.ReportingTransactions;
import org.broadleafcommerce.vendor.paypal.domain.ReportingTransactionResponse;
import org.broadleafcommerce.vendor.paypal.domain.TransactionInfo;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

import java.util.Map;

import lombok.RequiredArgsConstructor;

/**
 * This takes the {@link PaymentRequest} and calls the
 * {@link ReportingTransactions#get(Map, APIContext)} to read all the
 * {@link ReportingTransactionResponse} and then filters the results by matching the
 * {@link TransactionInfo#getPaypal_reference_id()} and {@link TransactionInfo#getCustom_field()}
 *
 * Note: in the PayPal payload, the {@link TransactionInfo#getCustom_field()} is used to capture and
 * custom info that we want. This class assumes that a BLC-produced transaction id is being sent to
 * PayPal.
 *
 * @author venkat
 *
 */
@RequiredArgsConstructor
public class DefaultPayPalSyncTransactionService implements PayPalSyncTransactionService {

    private final PayPalCheckoutExternalCallService paypalCheckoutService;

    @Override
    public ReportingTransactionResponse lookupTransactionsByQueryParams(
            Map<String, String> queryFilter) throws PayPalRESTException {
        PaymentRequest paymentRequest = new PaymentRequest();
        APIContext apiContext = paypalCheckoutService.constructAPIContext(paymentRequest);
        return executeTransactionSearch(apiContext, queryFilter);
    }

    /**
     * This calls the {@link ReportingTransactions#get(Map, APIContext)} to fetch the reporting
     * transactions of type {@link ReportingTransactions}
     * 
     * @param apiContext
     * @param queryParamsMap
     * @return
     * @throws PayPalRESTException
     */
    protected ReportingTransactionResponse executeTransactionSearch(APIContext apiContext,
            Map<String, String> queryParamsMap) throws PayPalRESTException {
        ReportingTransactions reportingTransactions = new ReportingTransactions();

        return reportingTransactions.get(queryParamsMap, apiContext);
    }

}
