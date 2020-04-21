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

import org.broadleafcommerce.vendor.paypal.domain.ReportingTransactionResponse;

import com.paypal.base.rest.PayPalRESTException;

import java.util.Map;

public interface PayPalSyncTransactionService {

    /**
     * <p>
     * This returns all the matching transactions wrapped in the
     * {@link ReportingTransactionResponse} based on the @param queryFilter
     * </p>
     * <p>
     * The queryFilter mapping keys required are : </br>
     * </br>
     * &nbsp;<b>start_date</b> : Filters the transactions in the response by a start date and time.
     * The date format required is,
     * <a href="https://tools.ietf.org/html/rfc3339#section-5.6">Internet date and time format</a>.
     * Seconds are required. Fractional seconds are optional.</br>
     * &nbsp;<b>end_date</b> : Filters the transactions in the response by a start date and time.
     * The date format required is,
     * <a href="https://tools.ietf.org/html/rfc3339#section-5.6">Internet date and time format</a>.
     * Seconds are required. Fractional seconds are optional.</br>
     * &nbsp;<b>transaction_amount</b> : Filters the transactions in the response by a gross
     * transaction amount range. Specify the range as <start-range> TO <end-range>.For example, to
     * search for transactions from $5.00 to $10.05, specify [500 TO 1005].</br>
     * &nbsp;<b>page</b> : Page number derives the page to be retrieved from the returned result
     * set.
     * </p>
     * 
     * @param queryFilter
     * @return
     * @throws PayPalRESTException
     */
    ReportingTransactionResponse lookupTransactionsByQueryParams(Map<String, String> queryFilter)
            throws PayPalRESTException;

}
