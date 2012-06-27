/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.payment.service.module;

import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.time.SystemTime;
import org.broadleafcommerce.core.order.domain.FulfillmentGroup;
import org.broadleafcommerce.core.payment.domain.PaymentInfo;
import org.broadleafcommerce.core.payment.domain.PaymentResponseItem;
import org.broadleafcommerce.core.payment.domain.PaymentResponseItemImpl;
import org.broadleafcommerce.core.payment.service.PaymentContext;
import org.broadleafcommerce.core.payment.service.exception.PaymentException;
import org.broadleafcommerce.core.payment.service.module.PaymentModule;
import org.broadleafcommerce.core.payment.service.type.PaymentInfoType;
import org.broadleafcommerce.profile.core.domain.Address;
import org.broadleafcommerce.profile.core.domain.AddressImpl;
import org.broadleafcommerce.profile.core.service.CountryService;
import org.broadleafcommerce.profile.core.service.StateService;
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

    private StateService stateService;

    private CountryService countryService;

    @Override
    public PaymentResponseItem authorize(PaymentContext paymentContext) throws PaymentException {
        //authorize transaction
        throw new PaymentException("The authorize method is not supported by this org.broadleafcommerce.payment.service.module.BraintreePaymentModule");

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

    public void setTargetResponse(Result<Transaction> result, PaymentContext paymentContext, PaymentResponseItem responseItem, Map<String, String> map) {
        responseItem.setAmountPaid(new Money(result.getTarget().getAmount()));
        responseItem.setTransactionId(result.getTarget().getId());
        responseItem.setAuthorizationCode(result.getTarget().getProcessorAuthorizationCode());
        responseItem.setProcessorResponseCode(result.getTarget().getProcessorResponseCode());
        responseItem.setProcessorResponseText(result.getTarget().getProcessorResponseText());
        responseItem.setAvsCode(result.getTarget().getAvsStreetAddressResponseCode());
        responseItem.setCvvCode(result.getTarget().getCvvResponseCode());
        map.put(MessageConstants.CARDTYPE, result.getTarget().getCreditCard().getCardType());
        map.put(MessageConstants.EXPIRATIONMONTH, result.getTarget().getCreditCard().getExpirationMonth());
        map.put(MessageConstants.EXPIRATIONYEAR, result.getTarget().getCreditCard().getExpirationYear());
        map.put(MessageConstants.LASTFOUR, result.getTarget().getCreditCard().getLast4());

        setBillingInfo(result.getTarget(), paymentContext);
        setShippingInfo(result.getTarget(), paymentContext);
    }

    public void setBillingInfo(Transaction result, PaymentContext paymentContext) {
        if (result.getBillingAddress() != null) {
            Address billingAddress = new AddressImpl();
            billingAddress.setFirstName(result.getBillingAddress().getFirstName());
            billingAddress.setLastName(result.getBillingAddress().getLastName());
            billingAddress.setCompanyName(result.getBillingAddress().getCompany());
            billingAddress.setAddressLine1(result.getBillingAddress().getStreetAddress());
            billingAddress.setAddressLine2(result.getBillingAddress().getExtendedAddress());
            billingAddress.setCity(result.getBillingAddress().getLocality());
            if (result.getBillingAddress().getRegion() != null && stateService.findStateByAbbreviation(result.getBillingAddress().getRegion()) != null ) {
                billingAddress.setState(stateService.findStateByAbbreviation(result.getBillingAddress().getRegion()));
            }
            billingAddress.setCountry(countryService.findCountryByAbbreviation(result.getBillingAddress().getCountryCodeAlpha2()));
            billingAddress.setPostalCode(result.getBillingAddress().getPostalCode());
            paymentContext.getPaymentInfo().setAddress(billingAddress);
        }


    }

    public void setShippingInfo(Transaction result, PaymentContext paymentContext) {
        if (result.getShippingAddress() != null && paymentContext.getPaymentInfo().getOrder().getFulfillmentGroups().size() == 1) {
            // If you pass the shipping address to Braintree, there has to be an existing fulfillment group on the order.
            // This must be done because of pricing considerations.
            // The fulfillment group must be constructed when adding to the cart or sometime before calling the gateway. This depends on UX.
            // This default implementation assumes one fulfillment group per order because braintree only supports a single shipping address.
            // Override this method if necessary.
            FulfillmentGroup fulfillmentGroup = paymentContext.getPaymentInfo().getOrder().getFulfillmentGroups().get(0);
            if (fulfillmentGroup != null) {
                Address shippingAddress = new AddressImpl();
                shippingAddress.setFirstName(result.getShippingAddress().getFirstName());
                shippingAddress.setLastName(result.getShippingAddress().getLastName());
                shippingAddress.setCompanyName(result.getShippingAddress().getCompany());
                shippingAddress.setAddressLine1(result.getShippingAddress().getStreetAddress());
                shippingAddress.setAddressLine2(result.getShippingAddress().getExtendedAddress());
                shippingAddress.setCity(result.getShippingAddress().getLocality());
                if (result.getShippingAddress().getRegion() != null && stateService.findStateByAbbreviation(result.getShippingAddress().getRegion()) != null ) {
                    shippingAddress.setState(stateService.findStateByAbbreviation(result.getShippingAddress().getRegion()));
                }
                shippingAddress.setCountry(countryService.findCountryByAbbreviation(result.getShippingAddress().getCountryCodeAlpha2()));
                shippingAddress.setPostalCode(result.getShippingAddress().getPostalCode());
                fulfillmentGroup.setAddress(shippingAddress);
            }

        }
    }

    public void setTransactionResponse(Result<Transaction> result, PaymentContext paymentContext, PaymentResponseItem responseItem, Map<String, String> map) {
        responseItem.setAmountPaid(new Money(result.getTransaction().getAmount()));
        responseItem.setTransactionId(result.getTransaction().getId());
        responseItem.setAuthorizationCode(result.getTransaction().getProcessorAuthorizationCode());
        responseItem.setProcessorResponseCode(result.getTransaction().getProcessorResponseCode());
        responseItem.setProcessorResponseText(result.getTransaction().getProcessorResponseText());
        responseItem.setAvsCode(result.getTransaction().getAvsStreetAddressResponseCode());
        responseItem.setCvvCode(result.getTransaction().getCvvResponseCode());
        map.put(MessageConstants.CARDTYPE, result.getTransaction().getCreditCard().getCardType());
        map.put(MessageConstants.EXPIRATIONMONTH, result.getTransaction().getCreditCard().getExpirationMonth());
        map.put(MessageConstants.EXPIRATIONYEAR, result.getTransaction().getCreditCard().getExpirationYear());
        map.put(MessageConstants.LASTFOUR, result.getTransaction().getCreditCard().getLast4());

        setBillingInfo(result.getTransaction(), paymentContext);
        setShippingInfo(result.getTransaction(), paymentContext);
    }

    @Override
    public PaymentResponseItem balance(PaymentContext paymentContext) throws PaymentException {
        throw new PaymentException("The balance method is not supported by this org.broadleafcommerce.payment.service.module.BraintreePaymentModule");
    }

    @Override
    public Boolean isValidCandidate(PaymentInfoType paymentType) {
        return paymentType == PaymentInfoType.CREDIT_CARD;
    }

    public BraintreePaymentService getBraintreePaymentService() {
        return braintreePaymentService;
    }

    public void setBraintreePaymentService(BraintreePaymentService braintreePaymentService) {
        this.braintreePaymentService = braintreePaymentService;
    }

    public StateService getStateService() {
        return stateService;
    }

    public void setStateService(StateService stateService) {
        this.stateService = stateService;
    }

    public CountryService getCountryService() {
        return countryService;
    }

    public void setCountryService(CountryService countryService) {
        this.countryService = countryService;
    }
}
