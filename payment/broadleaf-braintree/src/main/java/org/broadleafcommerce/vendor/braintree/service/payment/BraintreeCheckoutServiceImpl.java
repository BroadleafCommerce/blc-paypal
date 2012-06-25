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

package org.broadleafcommerce.vendor.braintree.service.payment;

import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import org.apache.commons.lang.StringUtils;
import org.broadleafcommerce.core.checkout.service.CheckoutService;
import org.broadleafcommerce.core.checkout.service.exception.CheckoutException;
import org.broadleafcommerce.core.checkout.service.workflow.CheckoutResponse;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.type.OrderStatus;
import org.broadleafcommerce.core.payment.domain.CreditCardPaymentInfo;
import org.broadleafcommerce.core.payment.domain.PaymentInfo;
import org.broadleafcommerce.core.payment.domain.PaymentResponseItem;
import org.broadleafcommerce.core.payment.domain.Referenced;
import org.broadleafcommerce.core.payment.service.PaymentInfoService;
import org.broadleafcommerce.core.payment.service.SecurePaymentInfoService;
import org.broadleafcommerce.core.payment.service.type.PaymentInfoType;
import org.broadleafcommerce.profile.core.domain.Address;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: elbertbautista
 * Date: 6/21/12
 * Time: 3:27 PM
 */
@Service("blBraintreeCheckoutService")
public class BraintreeCheckoutServiceImpl implements BraintreeCheckoutService {

    @Resource(name="blPaymentInfoService")
    protected PaymentInfoService paymentInfoService;

    @Resource(name="blSecurePaymentInfoService")
    protected SecurePaymentInfoService securePaymentInfoService;

    @Resource(name="blCheckoutService")
    protected CheckoutService checkoutService;

    @Resource(name="blCustomerService")
    protected CustomerService customerService;

    public CheckoutResponse completeAuthorizeAndDebitCheckout(String id, String queryString, Order order) throws CheckoutException {
        Map<PaymentInfo, Referenced> payments = new HashMap<PaymentInfo, Referenced>();
        CreditCardPaymentInfo creditCardPaymentInfo = ((CreditCardPaymentInfo) securePaymentInfoService.create(PaymentInfoType.CREDIT_CARD));

        PaymentInfo braintreePaymentInfo = null;
        for (PaymentInfo paymentInfo : order.getPaymentInfos()) {
            if (paymentInfo.getType() == PaymentInfoType.CREDIT_CARD) {
                //Assumes only one payment info of type credit card in the order
                paymentInfo.setReferenceNumber(id);
                paymentInfo.getAdditionalFields().put(MessageConstants.QUERYSTRING, queryString);
                braintreePaymentInfo = paymentInfo;
                break;
            }
        }

        if (braintreePaymentInfo == null) {
            braintreePaymentInfo = paymentInfoService.create();
            braintreePaymentInfo.setOrder(order);
            braintreePaymentInfo.setType(PaymentInfoType.CREDIT_CARD);
            braintreePaymentInfo.setReferenceNumber(id);
            braintreePaymentInfo.getAdditionalFields().put(MessageConstants.QUERYSTRING, queryString);
            order.getPaymentInfos().add(braintreePaymentInfo);
        }

        creditCardPaymentInfo.setReferenceNumber(id);
        payments.put(braintreePaymentInfo, creditCardPaymentInfo);

        order.setStatus(OrderStatus.SUBMITTED);
        order.setSubmitDate(Calendar.getInstance().getTime());

        CheckoutResponse checkoutResponse = checkoutService.performCheckout(order, payments);

        PaymentResponseItem responseItem = checkoutResponse.getPaymentResponse().getResponseItems().get(braintreePaymentInfo);
        if (responseItem.getTransactionSuccess()) {
            //Fill out a few customer values for anonymous customers
            Customer customer = order.getCustomer();
            if (StringUtils.isEmpty(customer.getFirstName())) {
                customer.setFirstName(responseItem.getCustomer().getFirstName());
            }
            if (StringUtils.isEmpty(customer.getLastName())) {
                customer.setLastName(responseItem.getCustomer().getLastName());
            }
            if (StringUtils.isEmpty(customer.getEmailAddress())) {
                customer.setEmailAddress(responseItem.getCustomer().getEmailAddress());
            }
            customerService.saveCustomer(customer, false);
        }

        return checkoutResponse;
    }

    public TransactionRequest constructAuthorizeAndDebitFields(TransactionRequest trParams, Order order) {
        //BroadleafPaymentModule automatically calls DEBIT and submits the transaction for settlement, therefore we have to say submit for settlement = false in trData
        trParams = constructProtectedFields(trParams, order, false);

       // you can override this and call constructShippingFields to pass as well.

        return trParams;
    }

    public TransactionRequest constructProtectedFields(TransactionRequest trParams, Order order, boolean submitForSettlement) {
        return  trParams.
                type(Transaction.Type.SALE).
                amount(order.getTotal().getAmount()).
                orderId(order.getId().toString()).
                options().
                submitForSettlement(submitForSettlement).done();
    }

    public TransactionRequest constructShippingFields(TransactionRequest trParams, Address shippingAddress){
        String stateAbbr = "";
        if (shippingAddress.getState() != null) {
            stateAbbr = shippingAddress.getState().getAbbreviation();
        }

        return  trParams.
                shippingAddress().
                firstName(shippingAddress.getFirstName()).
                lastName(shippingAddress.getLastName()).
                company(shippingAddress.getCompanyName()).
                streetAddress(shippingAddress.getAddressLine1()).
                extendedAddress(shippingAddress.getAddressLine2()).
                locality(shippingAddress.getCity()).
                region(stateAbbr).
                postalCode(shippingAddress.getPostalCode()).
                countryCodeAlpha2(shippingAddress.getCountry().getAbbreviation()).
                done();
    }

    public TransactionRequest constructBillingFields(TransactionRequest trParams, Address billingAddress){
        String stateAbbr = "";
        if (billingAddress.getState() != null) {
            stateAbbr = billingAddress.getState().getAbbreviation();
        }

        return  trParams.
                billingAddress().
                firstName(billingAddress.getFirstName()).
                lastName(billingAddress.getLastName()).
                company(billingAddress.getCompanyName()).
                streetAddress(billingAddress.getAddressLine1()).
                extendedAddress(billingAddress.getAddressLine2()).
                locality(billingAddress.getCity()).
                region(stateAbbr).
                postalCode(billingAddress.getPostalCode()).
                countryCodeAlpha2(billingAddress.getCountry().getAbbreviation()).
                done();
    }

}
