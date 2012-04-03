package org.broadleafcommerce.vendor.braintree.service.payment;

import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.common.vendor.service.monitor.ServiceStatusDetectable;
import org.broadleafcommerce.common.vendor.service.type.ServiceStatusType;
import org.broadleafcommerce.vendor.braintree.service.payment.type.BraintreeMethodType;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: chadharchar
 * Date: 4/2/12
 * Time: 9:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class BraintreePaymentServiceImpl implements BraintreePaymentService, ServiceStatusDetectable<BraintreePaymentRequest>{

    private static final Log LOG = LogFactory.getLog(BraintreePaymentServiceImpl.class);

    protected String serverUrl;
    protected Integer failureReportingThreshold;
    protected Integer failureCount = 0;
    protected Boolean isUp = true;
    protected BraintreeGatewayRequest gatewayRequest;

    protected synchronized void clearStatus() {
        isUp = true;
        failureCount = 0;
    }

    protected synchronized void incrementFailure() {
        if (failureCount >= failureReportingThreshold) {
            isUp = false;
        } else {
            failureCount++;
        }
    }

    @Override
    public Result<Transaction> process(BraintreePaymentRequest paymentRequest) throws PaymentException {
        Result<Transaction> result;

        try {
           result = communicateWithVendor(paymentRequest);
        } catch (Exception e) {
            incrementFailure();
            throw new PaymentException(e);
        }
        clearStatus();

        return result;
    }

    protected Result<Transaction> communicateWithVendor(BraintreePaymentRequest paymentRequest) throws IOException {
        Result<Transaction> result;
        BraintreeMethodType methodType = paymentRequest.getMethodType();
        if(methodType == BraintreeMethodType.CONFIRM){
            result = gatewayRequest.buildRequest().transparentRedirect().confirmTransaction(paymentRequest.getQueryString());
        } else if(methodType == BraintreeMethodType.SUBMIT) {
            //TODO: implement partial submission
            result = gatewayRequest.buildRequest().transaction().submitForSettlement(paymentRequest.getTransactionID());
        } else if(methodType == BraintreeMethodType.VOID) {
            result = gatewayRequest.buildRequest().transaction().voidTransaction(paymentRequest.getTransactionID());
        } else if(methodType == BraintreeMethodType.REFUND) {
            //TODO: implement partial refund
            result = gatewayRequest.buildRequest().transaction().refund(paymentRequest.getTransactionID());
        } else {
            result = null;
        }
        return result;
    }
    
    public String makeTrData(TransactionRequest trParams){
        String returnUrl = gatewayRequest.getRedirectUrl() + "" + gatewayRequest.getTransactionType();
        return gatewayRequest.buildRequest().transparentRedirect().trData(trParams, returnUrl);
    }
    
    public String makeTrUrl(){
        return gatewayRequest.buildRequest().transparentRedirect().url();
    }

    public Integer getFailureReportingThreshold() {
        return failureReportingThreshold;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public synchronized ServiceStatusType getServiceStatus() {
        if (isUp) {
            return ServiceStatusType.UP;
        } else {
            return ServiceStatusType.DOWN;
        }
    }

    public void setFailureReportingThreshold(Integer failureReportingThreshold) {
        this.failureReportingThreshold = failureReportingThreshold;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getServiceName() {
        return getClass().getName();
    }

    public BraintreeGatewayRequest getGatewayRequest() {
        return gatewayRequest;
    }

    public void setGatewayRequest(BraintreeGatewayRequest gatewayRequest) {
        this.gatewayRequest = gatewayRequest;
    }
}
