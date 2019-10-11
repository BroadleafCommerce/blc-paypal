package org.broadleafcommerce.vendor.paypal.api;

import org.broadleafcommerce.vendor.paypal.domain.ReportingTransactionResponse;

import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.HttpMethod;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.base.rest.PayPalResource;
import com.paypal.base.rest.RESTUtil;

import java.util.Map;

/**
 * The PayPal REST SDK does not currently contain support for Reporting Transactions using the query
 * parameters.
 *
 * This supports executing the PayPal Sync API Reporting Transactions Please refer to this
 * https://developer.paypal.com/docs/api/sync/v1/
 * 
 * @author venkat
 *
 */
public class ReportingTransactions extends PayPalResource {

    public ReportingTransactions() {

    }

    /**
     * Execute a Reporting Transactions API by passing the required query parameters to the request
     * URI.
     * 
     * @param queryParams
     *        <p>
     *        Following query parameters are used: start_date, end_date and transaction_amount
     *        </p>
     * @param apiContext {@link APIContext} used for the API call.
     * @return
     * @throws PayPalRESTException
     */
    public ReportingTransactionResponse get(Map<String, String> queryParams, APIContext apiContext)
            throws PayPalRESTException {
        String pattern = "/v1/reporting/transactions";
        String resourcePath = RESTUtil.formatURIPath(pattern, null, queryParams);
        String payLoad = "";
        return (ReportingTransactionResponse) configureAndExecute(apiContext, HttpMethod.GET,
                resourcePath, payLoad, ReportingTransactionResponse.class);
    }



}
