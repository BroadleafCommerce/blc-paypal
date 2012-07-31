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

package org.broadleafcommerce.vendor.authorizenet.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.core.checkout.service.exception.CheckoutException;
import org.broadleafcommerce.core.checkout.service.workflow.CheckoutResponse;
import org.broadleafcommerce.core.order.domain.NullOrderImpl;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.payment.domain.PaymentInfo;
import org.broadleafcommerce.core.payment.domain.PaymentResponseItem;
import org.broadleafcommerce.core.payment.service.type.PaymentInfoType;
import org.broadleafcommerce.core.pricing.service.exception.PricingException;
import org.broadleafcommerce.core.web.controller.checkout.BroadleafCheckoutController;
import org.broadleafcommerce.core.web.order.CartState;
import org.broadleafcommerce.vendor.authorizenet.service.payment.AuthorizeNetCheckoutService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: elbertbautista
 * Date: 6/27/12
 * Time: 11:54 AM
 */
public class BroadleafAuthorizeNetController extends BroadleafCheckoutController {

    private static final Log LOG = LogFactory.getLog(BroadleafAuthorizeNetController.class);

    @Resource(name="blAuthorizeNetCheckoutService")
    protected AuthorizeNetCheckoutService authorizeNetCheckoutService;

    @Value("${authorizenet.error.url}")
    protected String authorizeNetErrorUrl;

    @Value("${authorizenet.confirm.url}")
    protected String authorizeNetConfirmUrl;

    @Override
    public String checkout(HttpServletRequest request, HttpServletResponse response, Model model) {
        Order order = CartState.getCart();
        if (!(order instanceof NullOrderImpl)) {
            try {
                Map<String, String> formFields = authorizeNetCheckoutService.constructAuthorizeAndDebitFields(order);
                for (String key :formFields.keySet()) {
                    model.addAttribute(key, formFields.get(key));
                }
            } catch (NoSuchAlgorithmException e) {
                LOG.error("Error Creating Authorize.net Checkout Form " + e);
            } catch (UnsupportedEncodingException e) {
                LOG.error("Error Creating Authorize.net Checkout Form " + e);
            }
        }

        return super.checkout(request, response, model);
    }

    public @ResponseBody String processAuthorizeNetAuthorizeAndDebit(HttpServletRequest request, HttpServletResponse response, Model model) throws NoSuchAlgorithmException, UnsupportedEncodingException, PricingException {
        Order order = authorizeNetCheckoutService.findCartForCustomer(request.getParameterMap());
        if (!(order instanceof NullOrderImpl)) {
            try {

                initializeOrderForCheckout(order);

                CheckoutResponse checkoutResponse = authorizeNetCheckoutService.completeAuthorizeAndDebitCheckout(order, request.getParameterMap());

                PaymentInfo authorizeNetPaymentInfo = null;
                for (PaymentInfo paymentInfo : checkoutResponse.getPaymentResponse().getResponseItems().keySet()){
                    if (PaymentInfoType.CREDIT_CARD.equals(paymentInfo.getType())){
                        authorizeNetPaymentInfo = paymentInfo;
                    }
                }

                PaymentResponseItem paymentResponseItem = checkoutResponse.getPaymentResponse().getResponseItems().get(authorizeNetPaymentInfo);
                if (paymentResponseItem.getTransactionSuccess()){
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Transaction success for order " + checkoutResponse.getOrder().getOrderNumber());
                        LOG.debug("Response for Authorize.net to relay to client: ");
                        LOG.debug(buildRelayResponse(authorizeNetConfirmUrl + "/" + checkoutResponse.getOrder().getOrderNumber()));
                    }
                    return buildRelayResponse(authorizeNetConfirmUrl + "/" + checkoutResponse.getOrder().getOrderNumber());
                }
            } catch (CheckoutException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Checkout Exception occurred processing Authorize.net relay response (params: [" + requestParamToString(request) + "])" + e);
                }
            }
        } else {
            if (LOG.isFatalEnabled()) {
                LOG.fatal("The order could not be determined from the Authorize.net relay response (params: [" + requestParamToString(request) + "]). NOTE: The transaction may have completed successfully. Check your application keys and hash.");
            }
        }

        processFailedOrderCheckout(order);
        return buildRelayResponse(authorizeNetErrorUrl);
    }

    private String buildRelayResponse (String receiptUrl) {
        StringBuffer response = new StringBuffer();
        response.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n \"http://www.w3.org/TR/html4/loose.dtd\">");
        response.append("<html>");
        response.append("<head>");
        response.append("</head>");
        response.append("<body>");
        response.append("<script type=\"text/javascript\">");
        response.append("var referrer = document.referrer;");
        response.append("if (referrer.substr(0,7)==\"http://\") referrer = referrer.substr(7);");
        response.append("if (referrer.substr(0,8)==\"https://\") referrer = referrer.substr(8);");
        response.append("if(referrer && referrer.indexOf(document.location.hostname) != 0) {");
        response.append("document.location = \"" + receiptUrl +"\";");
        response.append("}");
        response.append("</script>");
        response.append("<noscript>");
        response.append("<meta http-equiv=\"refresh\" content=\"0;url=" + receiptUrl + "\">");
        response.append("</noscript>");
        response.append("</body>");
        response.append("</html>");

        return response.toString();
    }

    private String requestParamToString(HttpServletRequest request) {
        StringBuffer requestMap = new StringBuffer();
        for (String key : (Set<String>)request.getParameterMap().keySet()) {
            requestMap.append(key + ": " + request.getParameter(key) + ", ");
        }
        return requestMap.toString();
    }

}
