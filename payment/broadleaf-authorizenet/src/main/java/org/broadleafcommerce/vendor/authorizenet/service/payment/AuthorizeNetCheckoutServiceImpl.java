/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.vendor.authorizenet.service.payment;

import net.authorize.ResponseField;
import net.authorize.sim.Fingerprint;
import net.authorize.sim.Result;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.core.checkout.service.CheckoutService;
import org.broadleafcommerce.core.checkout.service.exception.CheckoutException;
import org.broadleafcommerce.core.checkout.service.workflow.CheckoutResponse;
import org.broadleafcommerce.core.checkout.service.workflow.CheckoutSeed;
import org.broadleafcommerce.core.order.domain.FulfillmentGroup;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.OrderService;
import org.broadleafcommerce.core.payment.domain.CreditCardPaymentInfo;
import org.broadleafcommerce.core.payment.domain.PaymentInfo;
import org.broadleafcommerce.core.payment.domain.PaymentResponseItem;
import org.broadleafcommerce.core.payment.domain.Referenced;
import org.broadleafcommerce.core.payment.service.PaymentInfoService;
import org.broadleafcommerce.core.payment.service.SecurePaymentInfoService;
import org.broadleafcommerce.core.payment.service.type.PaymentInfoType;
import org.broadleafcommerce.profile.core.domain.*;
import org.broadleafcommerce.profile.core.service.CountryService;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.broadleafcommerce.profile.core.service.StateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: elbertbautista
 * Date: 6/27/12
 * Time: 11:42 AM
 */
@Service("blAuthorizeNetCheckoutService")
public class AuthorizeNetCheckoutServiceImpl implements AuthorizeNetCheckoutService {

    private static final Log LOG = LogFactory.getLog(AuthorizeNetCheckoutServiceImpl.class);
    public static final String BLC_CID = "blc_cid";
    public static final String BLC_OID = "blc_oid";
    public static final String BLC_TPS = "blc_tps";

    @Resource(name="blSecurePaymentInfoService")
    protected SecurePaymentInfoService securePaymentInfoService;

    @Resource(name="blPaymentInfoService")
    protected PaymentInfoService paymentInfoService;

    @Resource(name="blCheckoutService")
    protected CheckoutService checkoutService;

    @Resource(name="blCustomerService")
    protected CustomerService customerService;

    @Resource(name="blOrderService")
    protected OrderService orderService;

    @Resource(name="blStateService")
    protected StateService stateService;

    @Resource(name="blCountryService")
    protected CountryService countryService;

    @Value("${authorizenet.api.login.id}")
    protected String apiLoginId;

    @Value("${authorizenet.merchant.md5.key}")
    protected String merchantMD5Key;

    @Value("${authorizenet.transaction.key}")
    protected String transactionKey;

    @Value("${authorizenet.relay.response.url}")
    protected String relayResponseURL;

    @Value("${authorizenet.merchant.transaction.version}")
    protected String merchantTransactionVersion;

    @Value("${authorizenet.x_test_request}")
    protected String xTestRequest;

    @Value("${authorizenet.server.url}")
    protected String serverUrl;

    @Override
    public Order findCartForCustomer(Map<String, String[]> responseMap) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        Result result = Result.createResult(apiLoginId, merchantMD5Key, responseMap);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Result Reason Text - " + result.getResponseMap().get(ResponseField.RESPONSE_REASON_TEXT.getFieldName()));
        }

        if (result.isAuthorizeNet()){
            Long customerId = Long.parseLong(result.getResponseMap().get(BLC_CID));
            Long orderId = Long.parseLong(result.getResponseMap().get(BLC_OID));
            String formTps = result.getResponseMap().get(BLC_TPS);
            String tps = createTamperProofSeal(customerId, orderId);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Customer ID - " + customerId);
                LOG.debug("Order ID - " + orderId);
                LOG.debug("Form tps - " + formTps);
                LOG.debug("tps - " + tps);
            }

            if (tps.equalsIgnoreCase(formTps)) {
                Order order = orderService.findOrderById(orderId);
                if (order != null && order.getCustomer().getId().equals(customerId)){
                    return order;
                }
            }
        }

        return null;
    }

    @Override
    public CheckoutResponse completeAuthorizeAndDebitCheckout(Order order, Map<String, String[]> responseMap) throws CheckoutException {
        Result result = Result.createResult(apiLoginId, merchantMD5Key, responseMap);
        if (order != null && result.isAuthorizeNet()) {
            Map<PaymentInfo, Referenced> payments = new HashMap<PaymentInfo, Referenced>();
            CreditCardPaymentInfo creditCardPaymentInfo = ((CreditCardPaymentInfo) securePaymentInfoService.create(PaymentInfoType.CREDIT_CARD));

            //NOTE: assumes only one payment info of type credit card on the order.
            //Start by removing any payment info of type credit card already on the order.
            orderService.removePaymentsFromOrder(order, PaymentInfoType.CREDIT_CARD);

            PaymentInfo authorizeNetPaymentInfo = paymentInfoService.create();
            authorizeNetPaymentInfo.setOrder(order);
            authorizeNetPaymentInfo.setType(PaymentInfoType.CREDIT_CARD);
            authorizeNetPaymentInfo.setAmount(new Money(result.getResponseMap().get(ResponseField.AMOUNT.getFieldName())));
            authorizeNetPaymentInfo.setReferenceNumber(result.getResponseMap().get(ResponseField.INVOICE_NUMBER.getFieldName()));
            authorizeNetPaymentInfo.getAdditionalFields().put(ResponseField.RESPONSE_CODE.getFieldName(), result.getResponseCode().getCode() + "");

            Address billingAddress = new AddressImpl();
            Address shippingAddress = new AddressImpl();
            boolean billingPopulated = false;
            boolean shippingPopulated = false;
            for (ResponseField field : ResponseField.values()) {
                if (isBillingAddressField(field) && !StringUtils.isEmpty(result.getResponseMap().get(field.getFieldName()))) {
                    populateBillingAddress(result.getResponseMap(), field, billingAddress);
                    billingPopulated = true;
                }

                if (isShippingAddressField(field) && !StringUtils.isEmpty(result.getResponseMap().get(field.getFieldName()))) {
                    populateShippingAddress(result.getResponseMap(), field, shippingAddress);
                    shippingPopulated = true;
                }

                if (!isBillingAddressField(field) && !isShippingAddressField(field) && !StringUtils.isEmpty(result.getResponseMap().get(field.getFieldName()))){
                    authorizeNetPaymentInfo.getAdditionalFields().put(field.getFieldName(), result.getResponseMap().get(field.getFieldName()));
                }
            }

            //set billing address on the payment info
            if (billingPopulated) {
                authorizeNetPaymentInfo.setAddress(billingAddress);
            }
            //set shipping info on the fulfillment group
            if (shippingPopulated) {
                populateShippingAddressOnOrder(order, shippingAddress);
            }
            //finally add the authorizenet payment info to the order
            order.getPaymentInfos().add(authorizeNetPaymentInfo);

            if (LOG.isDebugEnabled()){
                LOG.debug("Invoice Number       : " + authorizeNetPaymentInfo.getReferenceNumber());
                LOG.debug("Amount               : " + authorizeNetPaymentInfo.getAmount());
                LOG.debug("Response Code        : " + authorizeNetPaymentInfo.getAdditionalFields().get(ResponseField.RESPONSE_CODE.getFieldName()));
                LOG.debug("Response Reason Code : " + result.getReasonResponseCode().getResponseReasonCode());
                LOG.debug("Response Reason Text : " + result.getResponseMap().get(ResponseField.RESPONSE_REASON_TEXT.getFieldName()));
                LOG.debug("Transaction ID       : " + result.getResponseMap().get(ResponseField.TRANSACTION_ID.getFieldName()));
            }

            creditCardPaymentInfo.setReferenceNumber(authorizeNetPaymentInfo.getReferenceNumber());
            payments.put(authorizeNetPaymentInfo, creditCardPaymentInfo);

            CheckoutResponse checkoutResponse = checkoutService.performCheckout(order, payments);

            PaymentResponseItem responseItem = checkoutResponse.getPaymentResponse().getResponseItems().get(authorizeNetPaymentInfo);
            if (responseItem.getTransactionSuccess()) {
                if (LOG.isDebugEnabled()){
                    LOG.debug("Fill out a few customer values for anonymous customers");
                }

                Customer customer = order.getCustomer();
                if (StringUtils.isEmpty(customer.getFirstName()) && result.getResponseMap().get(ResponseField.FIRST_NAME.getFieldName()) != null) {
                    customer.setFirstName(result.getResponseMap().get(ResponseField.FIRST_NAME.getFieldName()));
                }
                if (StringUtils.isEmpty(customer.getLastName()) && result.getResponseMap().get(ResponseField.LAST_NAME.getFieldName()) != null) {
                    customer.setLastName(result.getResponseMap().get(ResponseField.LAST_NAME.getFieldName()));
                }
                if (StringUtils.isEmpty(customer.getEmailAddress()) && result.getResponseMap().get(ResponseField.EMAIL_ADDRESS.getFieldName()) != null) {
                    customer.setEmailAddress(result.getResponseMap().get(ResponseField.EMAIL_ADDRESS.getFieldName()));
                }
                customerService.saveCustomer(customer, false);
            }

            return checkoutResponse;
        }

        throw new CheckoutException("Authorize.net DPM Relay Response is Invalid. Check your application keys and hash.", new CheckoutSeed(order, null, null));
    }

    @Override
    public Map<String, String> constructAuthorizeAndDebitFields(Order order) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        if (order != null) {
            Fingerprint fingerprint = Fingerprint.createFingerprint(apiLoginId, transactionKey, System.currentTimeMillis(), order.getTotal().toString());
            Map<String, String> formFields = new HashMap<String, String>();
            formFields.put("x_invoice_num", System.currentTimeMillis()+"");
            formFields.put("x_relay_url", relayResponseURL);
            formFields.put("x_login", apiLoginId);
            formFields.put("x_fp_sequence", fingerprint.getSequence()+"");
            formFields.put("x_fp_timestamp", fingerprint.getTimeStamp()+"");
            formFields.put("x_fp_hash", fingerprint.getFingerprintHash());
            formFields.put("x_version", merchantTransactionVersion);
            formFields.put("x_method", "CC");
            formFields.put("x_type", "AUTH_CAPTURE");
            formFields.put("x_amount", order.getTotal().toString());
            formFields.put("x_test_request", xTestRequest);

            formFields.put(BLC_CID, order.getCustomer().getId().toString());
            formFields.put(BLC_OID, order.getId().toString());
            formFields.put(BLC_TPS, createTamperProofSeal(order.getCustomer().getId(), order.getId()));

            formFields.put("authorizenet_server_url", serverUrl);

            return formFields;
        }

        return null;
    }

    protected String createTamperProofSeal(Long customerId, Long orderId) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        String tamperProofSeal = customerId.toString() + orderId.toString() + apiLoginId + transactionKey;
        md5.digest(tamperProofSeal.getBytes("UTF-8"));
        tamperProofSeal = new BigInteger(1, md5.digest()).toString(16).toUpperCase();
        while(tamperProofSeal.length() < 32) {
            tamperProofSeal = "0" + tamperProofSeal;
        }
        return tamperProofSeal;
    }

    protected boolean isBillingAddressField(ResponseField field) {
        if (ResponseField.FIRST_NAME.equals(field) || ResponseField.LAST_NAME.equals(field) ||
                ResponseField.COMPANY.equals(field) ||
                ResponseField.ADDRESS.equals(field) ||
                ResponseField.CITY.equals(field) ||
                ResponseField.STATE.equals(field) ||
                ResponseField.ZIP_CODE.equals(field) ||
                ResponseField.COUNTRY.equals(field)) {
            return true;
        }
        return false;
    }

    protected boolean isShippingAddressField(ResponseField field) {
        if (ResponseField.SHIP_TO_FIRST_NAME.equals(field) || ResponseField.SHIP_TO_LAST_NAME.equals(field) ||
                ResponseField.SHIP_TO_COMPANY.equals(field) ||
                ResponseField.SHIP_TO_ADDRESS.equals(field) ||
                ResponseField.SHIP_TO_CITY.equals(field) ||
                ResponseField.SHIP_TO_STATE.equals(field) ||
                ResponseField.SHIP_TO_ZIP_CODE.equals(field) ||
                ResponseField.SHIP_TO_COUNTRY.equals(field)) {
            return true;
        }
        return false;
    }

    protected void populateBillingAddress(Map<String, String> responseMap, ResponseField field, Address address) {
        String value = responseMap.get(field.getFieldName());

        if (!StringUtils.isEmpty(value) && address != null) {
            switch (field) {
                case FIRST_NAME: address.setFirstName(value); break;
                case LAST_NAME: address.setLastName(value); break;
                case COMPANY: address.setCompanyName(value); break;
                case ADDRESS: address.setAddressLine1(value); break;
                case CITY: address.setCity(value); break;
                case STATE:
                    State state = stateService.findStateByAbbreviation(value);
                    if (state != null) {
                        address.setState(state);
                    }
                    break;
                case COUNTRY:
                    Country country = countryService.findCountryByAbbreviation(value);
                    address.setCountry(country);
                    break;
                case ZIP_CODE: address.setPostalCode(value); break;
                default: break;
            }
        }
    }

    protected void populateShippingAddress(Map<String, String> responseMap, ResponseField field, Address address) {
        String value = responseMap.get(field.getFieldName());

        if (!StringUtils.isEmpty(value) && address != null) {
            switch (field) {
                case SHIP_TO_FIRST_NAME: address.setFirstName(value); break;
                case SHIP_TO_LAST_NAME: address.setLastName(value); break;
                case SHIP_TO_COMPANY: address.setCompanyName(value); break;
                case SHIP_TO_ADDRESS: address.setAddressLine1(value); break;
                case SHIP_TO_CITY: address.setCity(value); break;
                case SHIP_TO_STATE:
                    State state = stateService.findStateByAbbreviation(value);
                    if (state != null) {
                        address.setState(state);
                    }
                    break;
                case SHIP_TO_COUNTRY:
                    Country country = countryService.findCountryByAbbreviation(value);
                    address.setCountry(country);
                    break;
                case SHIP_TO_ZIP_CODE: address.setPostalCode(value); break;
                default: break;
            }
        }
    }

    protected void populateShippingAddressOnOrder(Order order, Address shippingAddress) {
        if (order.getFulfillmentGroups() != null && order.getFulfillmentGroups().size()==1) {
            FulfillmentGroup fg = order.getFulfillmentGroups().get(0);
            fg.setAddress(shippingAddress);
        }
    }

}
