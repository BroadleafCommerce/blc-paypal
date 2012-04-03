package org.broadleafcommerce.vendor.braintree.module;

import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.time.SystemTime;
import org.broadleafcommerce.core.payment.domain.PaymentInfo;
import org.broadleafcommerce.core.payment.domain.PaymentResponseItem;
import org.broadleafcommerce.core.payment.domain.PaymentResponseItemImpl;
import org.broadleafcommerce.core.payment.service.PaymentContext;
import org.broadleafcommerce.core.payment.service.exception.PaymentException;
import org.broadleafcommerce.core.payment.service.module.PaymentModule;
import org.broadleafcommerce.core.payment.service.type.PaymentInfoType;
import org.broadleafcommerce.vendor.braintree.service.payment.BraintreePaymentRequest;
import org.broadleafcommerce.vendor.braintree.service.payment.BraintreePaymentService;
import org.broadleafcommerce.vendor.braintree.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.braintree.service.payment.type.BraintreeMethodType;
import org.broadleafcommerce.vendor.braintree.service.payment.type.BraintreeRefundType;
import org.springframework.util.Assert;

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

    private BraintreePaymentService braintreePaymentService;

    @Override
    public PaymentResponseItem authorize(PaymentContext paymentContext) throws PaymentException {
        //authorize transaction
        throw new PaymentException("The authorize method is not supported by this vendor.braintree.payment.module");

    }

    @Override
    public PaymentResponseItem reverseAuthorize(PaymentContext paymentContext) throws PaymentException {
        //void transaction after submitted for settlement
        BraintreePaymentRequest request = buildBasicRequest(paymentContext, BraintreeMethodType.VOID);

        Assert.isTrue(paymentContext.getPaymentInfo().getAdditionalFields().containsKey(MessageConstants.BRAINTREEID), "Must pass a BRAINTREEID value on the additionalFields of the PaymentInfo instance.");

        Result<Transaction> result;
        try {
            result = getBraintreePaymentService().process(request);
        } catch (org.broadleafcommerce.common.vendor.service.exception.PaymentException e) {
            throw new PaymentException(e);
        }

        PaymentResponseItem responseItem = buildBasicResponse(result, paymentContext);

        return responseItem;
    }



    @Override
    public PaymentResponseItem debit(PaymentContext paymentContext) throws PaymentException {
        //submit for settlement
        BraintreePaymentRequest request = buildBasicRequest(paymentContext, BraintreeMethodType.SUBMIT);

        Assert.isTrue(paymentContext.getPaymentInfo().getAdditionalFields().containsKey(MessageConstants.BRAINTREEID), "Must pass a BRAINTREEID value on the additionalFields of the PaymentInfo instance.");

        Result<Transaction> result;
        try {
            result = getBraintreePaymentService().process(request);
        } catch (org.broadleafcommerce.common.vendor.service.exception.PaymentException e) {
            throw new PaymentException(e);
        }

        PaymentResponseItem responseItem = buildBasicResponse(result, paymentContext);

        return responseItem;
    }

    @Override
    public PaymentResponseItem authorizeAndDebit(PaymentContext paymentContext) throws PaymentException {
        //submit form to braintree then submit transaction for settlement

        BraintreePaymentRequest request = buildBasicRequest(paymentContext, BraintreeMethodType.CONFIRM);

        Assert.isTrue(paymentContext.getPaymentInfo().getAdditionalFields().containsKey(MessageConstants.QUERYSTRING), "Must pass a QUERYSTRING value on the additionalFields of the PaymentInfo instance.");
        PaymentInfo paymentInfo = paymentContext.getPaymentInfo();
        Result<Transaction> result;
        try {
            result = getBraintreePaymentService().process(request);
            paymentInfo.getAdditionalFields().remove(MessageConstants.QUERYSTRING);
        } catch (org.broadleafcommerce.common.vendor.service.exception.PaymentException e) {
            throw new PaymentException(e);
        }

        PaymentResponseItem responseItem = buildBasicResponse(result, paymentContext);

        if(responseItem.getTransactionSuccess()) {
            paymentContext.getPaymentInfo().getAdditionalFields().put(MessageConstants.BRAINTREEID, responseItem.getTransactionId());
            responseItem = debit(paymentContext);
        }
        return responseItem;
    }

    @Override
    public PaymentResponseItem credit(PaymentContext paymentContext) throws PaymentException {
        //refund transaction
        BraintreePaymentRequest request = buildBasicRequest(paymentContext, BraintreeMethodType.REFUND);

        Assert.isTrue(paymentContext.getPaymentInfo().getAdditionalFields().containsKey(MessageConstants.BRAINTREEID), "Must pass a BRAINTREEID value on the additionalFields of the PaymentInfo instance.");

        Result<Transaction> result;
        try {
            result = getBraintreePaymentService().process(request);
        } catch (org.broadleafcommerce.common.vendor.service.exception.PaymentException e) {
            throw new PaymentException(e);
        }

        PaymentResponseItem responseItem = buildBasicResponse(result, paymentContext);

        return responseItem;
    }

    @Override
    public PaymentResponseItem voidPayment(PaymentContext paymentContext) throws PaymentException {
        //void transaction before submitted for settlement
        return reverseAuthorize(paymentContext);
    }

    public BraintreePaymentRequest buildBasicRequest(PaymentContext paymentContext, BraintreeMethodType methodType) {
        BraintreePaymentRequest request = new BraintreePaymentRequest();
        request.setMethodType(methodType);
        if(methodType == BraintreeMethodType.CONFIRM){
            request.setQueryString(paymentContext.getPaymentInfo().getAdditionalFields().get(MessageConstants.QUERYSTRING));
        } else {
            request.setTransactionID(paymentContext.getPaymentInfo().getAdditionalFields().get(MessageConstants.BRAINTREEID));
        }

        if(methodType == BraintreeMethodType.REFUND){
            request.setRefundType(BraintreeRefundType.FULL);
        }
        return request;
    }

    private PaymentResponseItem buildBasicResponse(Result<Transaction> result, PaymentContext paymentContext) {

        PaymentResponseItem responseItem = new PaymentResponseItemImpl();
        responseItem.setTransactionSuccess(result.isSuccess());
        responseItem.setTransactionTimestamp(SystemTime.asDate());
        Map<String, String> map = new HashMap<String, String>();
        map.put(MessageConstants.MESSAGE, result.getMessage());
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
        map.put(MessageConstants.CARDTYPE, result.getTarget().getCreditCard().getCardType());
        map.put(MessageConstants.EXPIRATIONMONTH, result.getTarget().getCreditCard().getExpirationMonth());
        map.put(MessageConstants.EXPIRATIONYEAR, result.getTarget().getCreditCard().getExpirationYear());
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
        map.put(MessageConstants.CARDTYPE, result.getTransaction().getCreditCard().getCardType());
        map.put(MessageConstants.EXPIRATIONMONTH, result.getTransaction().getCreditCard().getExpirationMonth());
        map.put(MessageConstants.EXPIRATIONYEAR, result.getTransaction().getCreditCard().getExpirationYear());
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

    public BraintreePaymentService getBraintreePaymentService() {
        return braintreePaymentService;
    }

    public void setBraintreePaymentService(BraintreePaymentService braintreePaymentService) {
        this.braintreePaymentService = braintreePaymentService;
    }
}
