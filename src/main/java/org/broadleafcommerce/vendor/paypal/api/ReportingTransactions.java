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
package org.broadleafcommerce.vendor.paypal.api;

import org.broadleafcommerce.vendor.paypal.domain.ReportingTransactionResponse;

import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.HttpMethod;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.base.rest.PayPalResource;
import com.paypal.base.rest.RESTUtil;

import java.util.Map;

/**
 * The PayPal REST SDK does not currently contain support for
 * Reporting Transactions using the query parameters.
 *
 * This supports executing the PayPal Sync API Reporting Transactions
 * Please refer to this https://developer.paypal.com/docs/api/sync/v1/
 * @author venkat
 *
 */
public class ReportingTransactions extends PayPalResource{

    public ReportingTransactions() {

    }

    /**
     * Execute a Reporting Transactions API by passing the required query parameters to the request URI.
     * @param queryParams
     * <p>
     * Following query parameters are used:
     * start_date, end_date and transaction_amount
     * </p>
     * @param apiContext
     * {@link APIContext} used for the API call.
     * @return
     * @throws PayPalRESTException
     */
    public  ReportingTransactionResponse get(Map<String,String> queryParams, APIContext apiContext) throws PayPalRESTException {
        String pattern = "/v1/reporting/transactions";
        String resourcePath = RESTUtil.formatURIPath(pattern, null, queryParams);
        String payLoad = "";
        return (ReportingTransactionResponse)configureAndExecute(apiContext, HttpMethod.GET, resourcePath, payLoad, ReportingTransactionResponse.class);
    }



}
