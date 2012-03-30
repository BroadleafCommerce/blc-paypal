package org.broadleafcommerce.vendor.braintree.module;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionCloneRequest;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.time.SystemTime;
import org.broadleafcommerce.core.payment.domain.PaymentResponseItem;
import org.broadleafcommerce.core.payment.domain.PaymentResponseItemImpl;
import org.broadleafcommerce.core.payment.service.PaymentContext;
import org.broadleafcommerce.core.payment.service.exception.PaymentException;
import org.broadleafcommerce.core.payment.service.module.PaymentModule;
import org.broadleafcommerce.core.payment.service.type.PaymentInfoType;
import org.broadleafcommerce.vendor.braintree.service.payment.BraintreeGatewayRequest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: chadharchar
 * Date: 3/19/12
 * Time: 9:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class BraintreePaymentModule implements PaymentModule {

    private BraintreeGatewayRequest gatewayRequest;

    @Override
    public PaymentResponseItem authorize(PaymentContext paymentContext) throws PaymentException {
        //authorize transaction
        BraintreeGateway gateway = gatewayRequest.buildRequest();
        String queryString = paymentContext.getPaymentInfo().getAdditionalFields().get("queryString");
        Result<Transaction> result = gateway.transparentRedirect().confirmTransaction(queryString);
        paymentContext.getPaymentInfo().getAdditionalFields().remove("queryString");
        PaymentResponseItem responseItem = buildBasicResponse(result, paymentContext);
        if(responseItem.getTransactionSuccess()) {
            paymentContext.getPaymentInfo().getAdditionalFields().put("braintreeId", responseItem.getTransactionId());
        }
        return responseItem;
    }

    @Override
    public PaymentResponseItem reverseAuthorize(PaymentContext paymentContext) throws PaymentException {
        //void transaction after submitted for settlement
        BraintreeGateway gateway = gatewayRequest.buildRequest();
        String braintreeId = paymentContext.getPaymentInfo().getAdditionalFields().get("braintreeId");
        Result<Transaction> result = gateway.transaction().voidTransaction(braintreeId);
        PaymentResponseItem responseItem = buildBasicResponse(result, paymentContext);

        return responseItem;
    }



    @Override
    public PaymentResponseItem debit(PaymentContext paymentContext) throws PaymentException {
        //submit for settlement
        BraintreeGateway gateway = gatewayRequest.buildRequest();
        String braintreeId = paymentContext.getPaymentInfo().getAdditionalFields().get("braintreeId");
        Result<Transaction> result = gateway.transaction().submitForSettlement(braintreeId);
        PaymentResponseItem responseItem = buildBasicResponse(result, paymentContext);
        
        return responseItem;
    }

    @Override
    public PaymentResponseItem authorizeAndDebit(PaymentContext paymentContext) throws PaymentException {
        //submit form to braintree then submit transaction for settlement
        PaymentResponseItem responseItem = authorize(paymentContext);
        if(responseItem.getTransactionSuccess()) {
            responseItem = debit(paymentContext);
        }
        return responseItem;
    }

    @Override
    public PaymentResponseItem credit(PaymentContext paymentContext) throws PaymentException {
        //refund transaction
        BraintreeGateway gateway = gatewayRequest.buildRequest();
        String braintreeId = paymentContext.getPaymentInfo().getAdditionalFields().get("braintreeId");
        Result<Transaction> result = gateway.transaction().refund(braintreeId);
        PaymentResponseItem responseItem = buildBasicResponse(result, paymentContext);

        return responseItem;
    }

    @Override
    public PaymentResponseItem voidPayment(PaymentContext paymentContext) throws PaymentException {
        //void transaction before submitted for settlement
        return reverseAuthorize(paymentContext);
    }

    public PaymentResponseItem cloneTransaction(PaymentContext paymentContext) throws  PaymentException {

        BraintreeGateway gateway = gatewayRequest.buildRequest();
        BigDecimal amount = new BigDecimal(paymentContext.getPaymentInfo().getAdditionalFields().get("amount"));
        TransactionCloneRequest request = new TransactionCloneRequest().
                amount(amount).
                options().submitForSettlement(true).done();

        Result<Transaction> result = gateway.transaction().cloneTransaction(paymentContext.getPaymentInfo().getAdditionalFields().get("braintreeId"), request);
        PaymentResponseItem responseItem = buildBasicResponse(result, paymentContext);
        return responseItem;
    }

    private PaymentResponseItem buildBasicResponse(Result<Transaction> result, PaymentContext paymentContext) {

        PaymentResponseItem responseItem = new PaymentResponseItemImpl();
        responseItem.setTransactionSuccess(result.isSuccess());
        responseItem.setTransactionTimestamp(SystemTime.asDate());
        Map<String, String> map = new HashMap<String, String>();
        map.put("message", result.getMessage());
        if(result.isSuccess()) {
            setTargetResponse(result, paymentContext, responseItem, map);
        } else if(result.getTransaction() != null){
            setTransactionResponse(result, paymentContext, responseItem, map);
        } else {
            responseItem.setAmountPaid(new Money(0));
        }

        responseItem.setAdditionalFields(map);

        return responseItem;
    }

    private void setTargetResponse(Result<Transaction> result, PaymentContext paymentContext, PaymentResponseItem responseItem, Map<String, String> map) {
        responseItem.setAmountPaid(new Money(result.getTarget().getAmount()));
        responseItem.setTransactionId(result.getTarget().getId());
        responseItem.setAuthorizationCode(result.getTarget().getProcessorAuthorizationCode());
        responseItem.setProcessorResponseCode(result.getTarget().getProcessorResponseCode());
        responseItem.setProcessorResponseText(result.getTarget().getProcessorResponseText());
        responseItem.setAvsCode(result.getTarget().getAvsStreetAddressResponseCode());
        responseItem.setCvvCode(result.getTarget().getCvvResponseCode());
        responseItem.setPaymentInfoReferenceNumber(result.getTarget().getCreditCard().getMaskedNumber());
        map.put("cardType", result.getTarget().getCreditCard().getCardType());
        map.put("expirationMonth", result.getTarget().getCreditCard().getExpirationMonth());
        map.put("expirationYear", result.getTarget().getCreditCard().getExpirationYear());
        paymentContext.getPaymentInfo().setReferenceNumber(result.getTarget().getCreditCard().getMaskedNumber());
    }

    private void setTransactionResponse(Result<Transaction> result, PaymentContext paymentContext, PaymentResponseItem responseItem, Map<String, String> map) {
        responseItem.setAmountPaid(new Money(result.getTransaction().getAmount()));
        responseItem.setTransactionId(result.getTransaction().getId());
        responseItem.setAuthorizationCode(result.getTransaction().getProcessorAuthorizationCode());
        responseItem.setProcessorResponseCode(result.getTransaction().getProcessorResponseCode());
        responseItem.setProcessorResponseText(result.getTransaction().getProcessorResponseText());
        responseItem.setAvsCode(result.getTransaction().getAvsStreetAddressResponseCode());
        responseItem.setCvvCode(result.getTransaction().getCvvResponseCode());
        responseItem.setPaymentInfoReferenceNumber(result.getTransaction().getCreditCard().getMaskedNumber());
        map.put("cardType", result.getTransaction().getCreditCard().getCardType());
        map.put("expirationMonth", result.getTransaction().getCreditCard().getExpirationMonth());
        map.put("expirationYear", result.getTransaction().getCreditCard().getExpirationYear());
        paymentContext.getPaymentInfo().setReferenceNumber(result.getTransaction().getCreditCard().getMaskedNumber());
    }

    @Override
    public PaymentResponseItem balance(PaymentContext paymentContext) throws PaymentException {
        throw new PaymentException("The balance method is not supported by this vendor.braintree.payment.module");
    }

    @Override
    public Boolean isValidCandidate(PaymentInfoType paymentType) {
        return paymentType == PaymentInfoType.CREDIT_CARD;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public BraintreeGatewayRequest getGatewayRequest() {
        return gatewayRequest;
    }

    public void setGatewayRequest(BraintreeGatewayRequest gatewayRequest) {
        this.gatewayRequest = gatewayRequest;
    }
}
