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

import com.braintreegateway.TransactionRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.core.checkout.service.exception.CheckoutException;
import org.broadleafcommerce.core.checkout.service.workflow.CheckoutResponse;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.payment.domain.PaymentInfo;
import org.broadleafcommerce.core.payment.domain.PaymentResponseItem;
import org.broadleafcommerce.core.payment.service.PaymentInfoService;
import org.broadleafcommerce.core.payment.service.type.PaymentInfoType;
import org.broadleafcommerce.core.pricing.service.exception.PricingException;
import org.broadleafcommerce.core.web.controller.checkout.BroadleafCheckoutController;
import org.broadleafcommerce.core.web.order.CartState;
import org.broadleafcommerce.vendor.braintree.service.payment.BraintreeCheckoutService;
import org.broadleafcommerce.vendor.braintree.service.payment.BraintreePaymentService;
import org.broadleafcommerce.vendor.braintree.service.payment.MessageConstants;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 * User: elbertbautista
 * Date: 6/21/12
 * Time: 2:39 PM
 */
public class BroadleafBraintreeController extends BroadleafCheckoutController {

    private static final Log LOG = LogFactory.getLog(BroadleafBraintreeController.class);

    @Resource(name="blBraintreeVendorOrientedPaymentService")
    protected BraintreePaymentService braintreePaymentService;

    @Resource(name="blBraintreeCheckoutService")
    protected BraintreeCheckoutService braintreeCheckoutService;

    @Resource(name="blPaymentInfoService")
    protected PaymentInfoService paymentInfoService;


    /**
     * Construct AuthorizeAndDebit Braintree Form
     *
     * This method overrides the BroadleafCheckoutController checkout() method
     * and provides the attributes "trData" and "trURL" on the request.
     * It returns super.checkout();
     *
     * Use this endpoint to dynamically generate a Transparent Redirect Braintree Form to perform an Authorization and Capture transaction.
     * To use: Create a controller in your application and extend this class to provide an @RequestMapping value
     * By default, braintreeCheckoutService.constructAuthorizeAndDebitFields() does NOT construct the shipping and billing information in the trData parameter.
     * You can override that method to construct the shipping address from the fulfillment groups in your implementation if you wish or
     * directly pass that information in the Transparent Redirect. This is entirely up to how your UX is defined.
     *
     * @param model - The Spring MVC model
     * @param request - The Http request
     * @return String - the checkout view
     */
    @Override
    public String checkout(HttpServletRequest request, HttpServletResponse response, Model model) {
        Order order = CartState.getCart();
        if (order != null) {
            TransactionRequest trParams = new TransactionRequest();
            trParams = braintreeCheckoutService.constructAuthorizeAndDebitFields(trParams, order);
            String trData = braintreePaymentService.makeTrData(trParams);
            String trUrl = braintreePaymentService.makeTrUrl();
            model.addAttribute("trData", trData);
            model.addAttribute("trUrl", trUrl);
        }

        return super.checkout(request, response, model);
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
     * @return String
     */
    public String processBraintreeAuthorizeAndDebit(Model model, @RequestParam String id,
            HttpServletRequest request, HttpServletResponse response) throws CheckoutException, PricingException {

        Order order = CartState.getCart();
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

            orderService.save(order, false);

            initializeOrderForCheckout(order);

            CheckoutResponse checkoutResponse = braintreeCheckoutService.completeAuthorizeAndDebitCheckout(id, queryString, order);
            PaymentResponseItem paymentResponseItem = checkoutResponse.getPaymentResponse().getResponseItems().get(braintreePaymentInfo);
            if (!paymentResponseItem.getTransactionSuccess()){
                processFailedOrderCheckout(order);
                checkout(request, response, model);
                model.addAttribute("paymentException", true);
                return getCheckoutView();
            }

            return getConfirmationView(checkoutResponse.getOrder().getOrderNumber());

        }

        return getCartPageRedirect();
    }



}
