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

package org.broadleafcommerce.vendor.braintree.web.controller;

import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.web.controller.BroadleafAbstractController;
import org.broadleafcommerce.core.checkout.service.exception.CheckoutException;
import org.broadleafcommerce.core.checkout.service.workflow.CheckoutResponse;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.CartService;
import org.broadleafcommerce.core.order.service.type.OrderStatus;
import org.broadleafcommerce.core.payment.domain.CreditCardPaymentInfo;
import org.broadleafcommerce.core.payment.domain.PaymentInfo;
import org.broadleafcommerce.core.payment.domain.PaymentResponseItem;
import org.broadleafcommerce.core.payment.domain.Referenced;
import org.broadleafcommerce.core.payment.service.PaymentInfoService;
import org.broadleafcommerce.core.payment.service.SecurePaymentInfoService;
import org.broadleafcommerce.core.payment.service.exception.PaymentException;
import org.broadleafcommerce.core.payment.service.type.PaymentInfoType;
import org.broadleafcommerce.core.pricing.service.exception.PricingException;
import org.broadleafcommerce.core.web.checkout.model.CheckoutForm;
import org.broadleafcommerce.profile.core.domain.Address;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.web.core.CustomerState;
import org.broadleafcommerce.vendor.braintree.service.payment.BraintreeCheckoutService;
import org.broadleafcommerce.vendor.braintree.service.payment.BraintreePaymentService;
import org.broadleafcommerce.vendor.braintree.service.payment.MessageConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: elbertbautista
 * Date: 6/21/12
 * Time: 2:39 PM
 */
public class BroadleafBraintreeController extends BroadleafAbstractController {

    private static final Log LOG = LogFactory.getLog(BroadleafBraintreeController.class);

    @Resource(name="blCartService")
    protected CartService cartService;

    @Resource(name="blSecurePaymentInfoService")
    protected SecurePaymentInfoService securePaymentInfoService;

    @Resource(name="blBraintreeVendorOrientedPaymentService")
    protected BraintreePaymentService braintreePaymentService;

    @Resource(name="blBraintreeCheckoutService")
    protected BraintreeCheckoutService braintreeCheckoutService;

    @Resource(name="blPaymentInfoService")
    protected PaymentInfoService paymentInfoService;

    @Value("${braintree.form.view}")
    protected String braintreeFormView;

    @Value("${braintree.verify.view}")
    protected String braintreeVerifyView;

    @Value("${braintree.confirm.view}")
    protected String braintreeConfirmView;


    /**
     * The endpoint to dynamically build a Transparent Redirect Braintree Form to perform an Authorization and Capture transaction.
     * To use: Create a controller in your application and extend this class to provide an @RequestMapping value
     * By default, braintreeCheckoutService.constructAuthorizeAndDebitFields() does NOT pass in any shipping information.
     * You can override that method to construct the shipping address from the fulfillment groups in your implementation if you wish.
     *
     * @param model - The Spring MVC model
     * @param request - The Http request
     * @return String - /some/path/to/braintreeFormView (if it is an AJAX request, it will return /some/path/to/ajax/braintreeFormView)
     */
    public String constructAuthorizeAndDebitBraintreeForm(Model model, HttpServletRequest request) throws PaymentException {
        Order order = cartService.findCartForCustomer(CustomerState.getCustomer());
        if (order != null) {
            TransactionRequest trParams = new TransactionRequest();
            trParams = braintreeCheckoutService.constructAuthorizeAndDebitFields(trParams, order);
            String trData = braintreePaymentService.makeTrData(trParams);
            String trUrl = braintreePaymentService.makeTrUrl();
            model.addAttribute("trData", trData);
            model.addAttribute("trUrl", trUrl);
        }

        return ajaxRender(braintreeFormView,request, model);
    }

    /**
     * The default endpoint that Braintree redirects to on callback.
     * This is the ${braintree.redirectUrl} configured in your properties file
     * Braintree will send a query string on the request that will need to be confirmed for the transaction to be settled.
     * To use: Create a controller in your application and extend this class to provide an @RequestMapping value
     *
     * @param id - A Braintree identifier on the query string
     * @param model - The Spring MVC model
     * @param request - The Http request
     * @return ModelAndView
     */
    public String processBraintreeAuthorizeAndDebit(Model model, @RequestParam String id, HttpServletRequest request) throws CheckoutException, PricingException {
        Order order = cartService.findCartForCustomer(CustomerState.getCustomer());
        if (order != null) {
            String queryString = request.getQueryString();
            PaymentInfo braintreePaymentInfo = null;
            for (PaymentInfo paymentInfo : order.getPaymentInfos()) {
                if (paymentInfo.getType() == PaymentInfoType.CREDIT_CARD) {
                    //There should only be one payment info of type credit card in the order
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

            cartService.save(order, false);
            if (StringUtils.isEmpty(braintreeVerifyView) || "?".equals(braintreeVerifyView)) {
                CheckoutResponse checkoutResponse = braintreeCheckoutService.completeAuthorizeAndDebitCheckout(id, queryString, order);
                PaymentResponseItem paymentResponseItem = checkoutResponse.getPaymentResponse().getResponseItems().get(braintreePaymentInfo);
                if (paymentResponseItem.getTransactionSuccess()){
                    return ajaxRender(braintreeConfirmView, request, model);
                }

            } else {
                return "redirect:" + braintreeVerifyView;
            }

        }

        return null;
    }

}
