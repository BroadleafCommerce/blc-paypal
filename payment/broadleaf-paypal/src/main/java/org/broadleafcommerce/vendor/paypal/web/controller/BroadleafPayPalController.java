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

package org.broadleafcommerce.vendor.paypal.web.controller;

import org.apache.commons.lang.StringUtils;
import org.broadleafcommerce.core.checkout.service.exception.CheckoutException;
import org.broadleafcommerce.core.checkout.service.workflow.CheckoutResponse;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.CartService;
import org.broadleafcommerce.core.payment.domain.PaymentInfo;
import org.broadleafcommerce.core.payment.domain.PaymentResponseItem;
import org.broadleafcommerce.core.payment.service.exception.PaymentException;
import org.broadleafcommerce.core.payment.service.type.PaymentInfoType;
import org.broadleafcommerce.core.payment.service.workflow.CompositePaymentResponse;
import org.broadleafcommerce.core.pricing.service.exception.PricingException;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.web.core.CustomerState;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCheckoutService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


/**
 * Created with IntelliJ IDEA.
 * User: elbertbautista
 * Date: 6/13/12
 * Time: 4:41 PM
 *
 * This is a default controller to handle the most common PayPal Express Checkout use cases.
 * This was created to make integration with PayPal seamless and a matter of configuring your properties file.
 * For custom implementations, this class should not be used. Re-implement and extend <code>PayPalCheckoutService</code>.
 * To use, this class must be auto-scanned in your application context.
 *
 * This controller supports the following:
 *  - Single Payment Type flow using the PayPal Express Checkout API
 *  - Using Broadleaf to collect shipping information (default) or use PayPal to collect shipping information.
 *  - Forward to a verification screen or order confirmation screen upon redirect from PayPal
 *
 * This controller depends on the following properties being set:
 *  - paypal.return.url = this must be set to "/broadleaf-commerce/paypal/process" in order to use this Default Controller. This is already set by default. PayPal redirects the user back to here once the customer has completed everything on PayPal's side.
 *  - paypal.order.verify.url = set this property in order to redirect to a Verification Page first before completing checkout
 *  - paypal.order.confirm.url = set this to the location of the confirmation page
 *  - paypal.order.confirm.identifier = this is the request parameter variable that you can pass to the confirmation page so it can look up your order.
 *  - paypal.order.confirm.useOrderNumber = this is a boolean to use order.getOrderNumber() to pass to the confirmation page, otherwise it uses order.getOrderId()
 *
 */
public class BroadleafPayPalController {

    @Resource(name="blPayPalCheckoutService")
    protected PayPalCheckoutService payPalCheckoutService;

    @Resource(name="blCartService")
    protected CartService cartService;

    @Resource(name="blCustomerState")
    protected CustomerState customerState;

    protected String orderVerificationView;

    protected String orderConfirmationView;

    protected String orderConfirmationIdentifier;

    protected boolean useOrderNumber = false;

    /**
     * The default endpoint to initiate a PayPal Express Checkout.
     * To use: Create a controller in your application and extend this class.
     *
     * @param request - The Http request
     * @return ModelAndView
     */
    public ModelAndView paypalCheckout(HttpServletRequest request) throws PaymentException {
        Customer customer = customerState.getCustomer(request);
        final Order cart = cartService.findCartForCustomer(customer);
        if (cart != null) {
            CompositePaymentResponse compositePaymentResponse = payPalCheckoutService.initiateExpressCheckout(cart);

            for (PaymentInfo paymentInfo : compositePaymentResponse.getPaymentResponse().getResponseItems().keySet()) {
                if (PaymentInfoType.PAYPAL.equals(paymentInfo.getType())) {
                    PaymentResponseItem responseItem = compositePaymentResponse.getPaymentResponse().getResponseItems().get(paymentInfo);
                    if (responseItem.getTransactionSuccess()) {
                        return new ModelAndView("redirect:" + responseItem.getAdditionalFields().get(MessageConstants.REDIRECTURL));
                    }
                }
            }

        }

        return null;
    }

    /**
     * The default endpoint that PayPal redirects to on callback.
     * This is the ${paypal.return.url} configured in your properties file
     * To use: Create a controller in your application and extend this class.
     *
     * @param request - The Http request
     * @param token - A PayPal variable sent back as a request parameter
     * @param payerID - A PayPal variable sent back as a request parameter
     * @return ModelAndView
     */
    public ModelAndView paypalProcess(HttpServletRequest request,
                                      @RequestParam String token,
                                      @RequestParam("PayerID") String payerID) throws CheckoutException, PricingException {
        Customer customer = customerState.getCustomer(request);
        final Order cart = cartService.findCartForCustomer(customer);
        if (cart != null) {
            //save the payer id and token on the payment info
            PaymentInfo payPalPaymentInfo = null;
            for (PaymentInfo paymentInfo : cart.getPaymentInfos()) {
                if (paymentInfo.getType() == PaymentInfoType.PAYPAL) {
                    //There should only be one payment info of type paypal in the order
                    payPalPaymentInfo = paymentInfo;
                    paymentInfo.getAdditionalFields().put(MessageConstants.PAYERID, payerID);
                    paymentInfo.getAdditionalFields().put(MessageConstants.TOKEN, token);
                    break;
                }
            }

            if (payPalPaymentInfo != null) {
                cartService.save(cart, false);

                if (StringUtils.isEmpty(orderVerificationView) || "?".equals(orderVerificationView)) {
                    CheckoutResponse checkoutResponse = payPalCheckoutService.completeExpressCheckout(token, payerID, cart);
                    PaymentResponseItem responseItem = checkoutResponse.getPaymentResponse().getResponseItems().get(payPalPaymentInfo);
                    if (responseItem.getTransactionSuccess()) {
                        String orderIdentifier = checkoutResponse.getOrder().getId().toString();
                        if (useOrderNumber) {
                            orderIdentifier = checkoutResponse.getOrder().getOrderNumber();
                        }
                        return new ModelAndView("redirect:" + orderConfirmationView + "?" + orderConfirmationIdentifier + "=" + orderIdentifier);
                    }
                } else {
                    return new ModelAndView("redirect:" + orderVerificationView);
                }
            }
        }

        return null;
    }

    public String getOrderVerificationView() {
        return orderVerificationView;
    }

    public void setOrderVerificationView(String orderVerificationView) {
        this.orderVerificationView = orderVerificationView;
    }

    public String getOrderConfirmationView() {
        return orderConfirmationView;
    }

    public void setOrderConfirmationView(String orderConfirmationView) {
        this.orderConfirmationView = orderConfirmationView;
    }

    public String getOrderConfirmationIdentifier() {
        return orderConfirmationIdentifier;
    }

    public void setOrderConfirmationIdentifier(String orderConfirmationIdentifier) {
        this.orderConfirmationIdentifier = orderConfirmationIdentifier;
    }

    public boolean isUseOrderNumber() {
        return useOrderNumber;
    }

    public void setUseOrderNumber(boolean useOrderNumber) {
        this.useOrderNumber = useOrderNumber;
    }
}
