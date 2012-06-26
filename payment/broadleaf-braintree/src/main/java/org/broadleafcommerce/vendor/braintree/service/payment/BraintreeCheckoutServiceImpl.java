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

    /**
     * Completes a checkout transaction by confirming the callback query string of a Transparent Redirect.
     *
     * This call executes a complete checkout by calling checkoutService.performCheckout().
     * Make sure the payments and fulfillment groups are fully set up before invoking.
     *
     * @param id - a request parameter on the Braintree callback, used as a unique identifier for the PaymentInfo
     * @param queryString - the query string that Braintree sends back on callback
     * @param order - The order
     * @return CheckoutResponse - the response from Braintree
     */
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

    /**
     * Use this method to generate the Transparent Redirect Form parameters needed to perform an Authorization and Debit transaction.
     *
     * @param trParams - Braintree Parameters
     * @param order - The order
     * @return TransactionRequest - Braintree transaction request
     */
    public TransactionRequest constructAuthorizeAndDebitFields(TransactionRequest trParams, Order order) {
        //BroadleafPaymentModule automatically calls DEBIT and submits the transaction for settlement, therefore we have to say submit for settlement = false in trData
        trParams = constructProtectedFields(trParams, order, false);
        return trParams;
    }

    /**
     * Use this method to generate the Transparent Redirect Form parameters needed to perform an Authorization and Debit transaction from a saved payment token
     * This requires that the Braintree merchant has subscribed to the "Vault" feature
     *
     * @param trParams - Braintree Parameters
     * @param order - The order
     * @param paymentMethodToken - The payment token that is saved in Braintree's Vault
     * @return TransactionRequest - Braintree transaction request
     */
    public TransactionRequest constructAuthorizeAndDebitFieldsFromToken(TransactionRequest trParams, Order order, String paymentMethodToken){
        trParams = constructProtectedFields(trParams, order, false);
        //TODO verify paymentMethodToken to saved CustomerPayments for the Customer on the Order
        trParams = trParams.paymentMethodToken(paymentMethodToken);
        return trParams;
    }

    /**
     * Use this method to generate the Transparent Redirect Form parameters needed to save a customer to the "Vault"
     * This requires that the Braintree merchant has subscribed to the "Vault" feature
     *
     * This can also be accomplished through non protected fields:
     * <input type="text" name="customer[first_name]" />
     * <input type="text" name="customer[email]" />
     * <input type="text" name="customer[credit_card][cardholder_name]" />
     * <input type="text" name="customer[credit_card][billing_address][street_address]" />
     * <input type="text" name="customer[credit_card][billing_address][postal_code]" />
     * see: https://www.braintreepayments.com/docs/java/customers/tr_fields
     *
     * @param trParams - Braintree Parameters
     * @param order - The order
     * @return TransactionRequest - Braintree transaction request
     */
    public TransactionRequest constructSaveVaultCustomerFields(TransactionRequest trParams, Order order) {
        return trParams.
                customer().
                id(order.getCustomer().getId().toString()).
                firstName(order.getCustomer().getFirstName()).
                lastName(order.getCustomer().getLastName()).
                email(order.getCustomer().getEmailAddress()).done().
                options().storeInVaultOnSuccess(true).done();

    }

    /**
     * Use this method to generate the mandatory "trData" fields that are needed to perform a Transparent Redirect
     * see: https://www.braintreepayments.com/docs/java/transactions/tr_fields
     *
     * Note: Braintree handles multi-currency on an individual merchant basis
     * Each merchant account can only process for a single currency. So setting which merchant account to use will also determine which currency the transaction is processed with.
     * To support multi-currency: overload this method to includes the merchant_id that handles the orders currency
     *
     * @param trParams - Braintree Parameters
     * @param order - The order
     * @param submitForSettlement - a flag indicating whether or not to automatically submit the transaction for settlement.
     * @return TransactionRequest - Braintree transaction request
     */
    public TransactionRequest constructProtectedFields(TransactionRequest trParams, Order order, boolean submitForSettlement) {
        return  trParams.
                type(Transaction.Type.SALE).
                amount(order.getTotal().getAmount()).
                orderId(order.getId().toString()).
                options().
                submitForSettlement(submitForSettlement).done();
    }

    /**
     * Use this method to generate the "trData" fields for the shipping address.
     *
     * This can also be accomplished through non protected fields:
     * <input type="text" name="transaction[shipping][first_name]" />
     * <input type="text" name="transaction[shipping][last_name]" />
     * see: https://www.braintreepayments.com/docs/java/transactions/tr_fields
     *
     * @param trParams - Braintree Parameters
     * @param shippingAddress - shipping address
     * @return TransactionRequest - Braintree transaction request
     */
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

    /**
     * Use this method to generate the "trData" fields for the billing address.
     *
     * This can also be accomplished through non protected fields:
     * <input type="text" name="transaction[billing][first_name]" />
     * <input type="text" name="transaction[billing][last_name]" />
     * see: https://www.braintreepayments.com/docs/java/transactions/tr_fields
     *
     * @param trParams - Braintree Parameters
     * @param billingAddress - billing address
     * @return TransactionRequest - Braintree transaction request
     */
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
