package org.broadleafcommerce.vendor.braintree.service.payment;

import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.common.vendor.service.type.ServiceStatusType;
import org.broadleafcommerce.vendor.braintree.service.payment.type.BraintreeMethodType;

/**
 * Created by IntelliJ IDEA.
 * User: chadharchar
 * Date: 4/2/12
 * Time: 9:14 AM
 * To change this template use File | Settings | File Templates.
 */
public interface BraintreePaymentService {

    public Result<Transaction> process(BraintreePaymentRequest paymentRequest) throws PaymentException;
    
    public String makeTrData(TransactionRequest request);

    public String makeTrUrl();

    public ServiceStatusType getServiceStatus();

    public Integer getFailureReportingThreshold();

    public void setFailureReportingThreshold(Integer failureReportingThreshold);

    public String getServerUrl();

    public void setServerUrl(String serverUrl);
}
