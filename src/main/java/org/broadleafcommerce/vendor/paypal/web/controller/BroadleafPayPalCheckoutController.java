/*-
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2023 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.vendor.paypal.web.controller;

import com.paypal.orders.LinkDescription;
import com.paypal.orders.Order;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.PaymentGatewayConfiguration;
import org.broadleafcommerce.common.payment.service.PaymentGatewayHostedService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayWebResponseService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.common.web.payment.controller.PaymentGatewayAbstractController;
import org.broadleafcommerce.vendor.paypal.service.PayPalPaymentService;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.HashMap;
import java.util.Map;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Controller("blPayPalCheckoutController")
@RequestMapping("/" + BroadleafPayPalCheckoutController.GATEWAY_CONTEXT_KEY)
public class BroadleafPayPalCheckoutController extends PaymentGatewayAbstractController {

    private static final Log LOG = LogFactory.getLog(BroadleafPayPalCheckoutController.class);
    protected static final String GATEWAY_CONTEXT_KEY = "paypal-checkout";

    @Resource(name = "blPayPalCheckoutWebResponseService")
    protected PaymentGatewayWebResponseService paymentGatewayWebResponseService;

    @Resource(name = "blPayPalCheckoutHostedService")
    protected PaymentGatewayHostedService paymentGatewayHostedService;

    @Resource(name = "blPayPalCheckoutConfiguration")
    protected PaymentGatewayConfiguration paymentGatewayConfiguration;

    @Resource(name = "blPayPalPaymentService")
    protected PayPalPaymentService paymentService;

    @Override
    public void handleProcessingException(Exception e, final RedirectAttributes redirectAttributes)
            throws PaymentException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("A Processing Exception Occurred for " + GATEWAY_CONTEXT_KEY +
                    ". Adding Error to Redirect Attributes.");
        }

        redirectAttributes.addAttribute(PAYMENT_PROCESSING_ERROR, getProcessingErrorMessage());
    }

    @Override
    public void handleUnsuccessfulTransaction(Model model, final RedirectAttributes redirectAttributes,
                                              PaymentResponseDTO responseDTO) throws PaymentException {

        if (LOG.isTraceEnabled()) {
            LOG.trace("The Transaction was unsuccessful for " + GATEWAY_CONTEXT_KEY +
                    ". Adding Error to Redirect Attributes.");
        }

        redirectAttributes.addAttribute(PAYMENT_PROCESSING_ERROR, getProcessingErrorMessage());
    }

    @Override
    public String getGatewayContextKey() {
        return GATEWAY_CONTEXT_KEY;
    }

    @Override
    public PaymentGatewayWebResponseService getWebResponseService() {
        return paymentGatewayWebResponseService;
    }

    @Override
    public PaymentGatewayConfiguration getConfiguration() {
        return paymentGatewayConfiguration;
    }

    // ***********************************************
    // PayPal Checkout Default Endpoints
    // ***********************************************
    @Override
    @RequestMapping(value = "/return", method = RequestMethod.GET)
    public String returnEndpoint(Model model, HttpServletRequest request,
                                 final RedirectAttributes redirectAttributes,
                                 @PathVariable Map<String, String> pathVars) throws PaymentException {
        if (request.getParameter(MessageConstants.CHECKOUT_COMPLETE) != null) {
            request.setAttribute(MessageConstants.CHECKOUT_COMPLETE, true);
        }
        String path = super.process(model, request, redirectAttributes);
        if (isAjaxRequest(request) && StringUtils.startsWith(path, baseRedirect)) {
            return StringUtils.replace(path, baseRedirect, "ajaxredirect:");
        }
        return path;
    }

    @Override
    @RequestMapping(value = "/error", method = RequestMethod.GET)
    public String errorEndpoint(Model model, HttpServletRequest request,
                                final RedirectAttributes redirectAttributes,
                                @PathVariable Map<String, String> pathVars)
            throws PaymentException {
        redirectAttributes.addAttribute(PAYMENT_PROCESSING_ERROR,
                request.getParameter(PAYMENT_PROCESSING_ERROR));
        return getCartViewRedirect();
    }

    @RequestMapping(value = "/cancel", method = RequestMethod.GET)
    public String cancelEndpoint(Model model, HttpServletRequest request,
                                 final RedirectAttributes redirectAttributes,
                                 @PathVariable Map<String, String> pathVars)
            throws PaymentException {
        return getOrderReviewRedirect();
    }

    // ***********************************************
    // PayPal Client side REST checkout (common)
    // ***********************************************

    /**
     * Completes checkout for a PayPal {@link Order}. If there's already a PayPal {@link Order} we go ahead and make sure the details
     * of the {@link Order} are updated to all of the forms filled out by the customer since they could've updated shipping
     * information, added a promotion, or other various things to the order.
     * 
     * @return Redirect URL to either add the order and checkout or just checkout
     * @throws PaymentException 
     */
    @RequestMapping(value = "/checkout/complete", method = RequestMethod.POST)
    public String completeCheckout() throws PaymentException {
        paymentService.updatePayPalOrderForFulfillment();
        String orderId = paymentService.getPayPalOrderIdFromCurrentOrder();
        String payerId = paymentService.getPayPalPayerIdFromCurrentOrder();
        if (StringUtils.isBlank(orderId)) {
            throw new PaymentException("Unable to complete checkout because no PayPal order id was found on the current order");
        }
        if (StringUtils.isBlank(payerId)) {
            throw new PaymentException("Unable to complete checkout because no PayPal payer id was found on the current order");
        }
        return "redirect:/paypal-checkout/return?" + MessageConstants.HTTP_ORDER_ID + "=" + orderId + "&" + MessageConstants.HTTP_PAYERID + "=" + payerId;
    }

    // ***********************************************
    // PayPal Client side REST checkout (iframe)
    // ***********************************************
    @RequestMapping(value = "/create-order", method = RequestMethod.POST)
    public @ResponseBody Map<String, String> createOrder(@RequestParam("performCheckout") Boolean performCheckout) throws PaymentException {
        Map<String, String> response = new HashMap<>();
        Order createdOrder = paymentService.createPayPalOrderForCurrentOrder(performCheckout);
        response.put("id", createdOrder.id());
        return response;
    }
    
    // ***********************************************
    // PayPal Client side REST checkout (hosted page)
    // ***********************************************
    @RequestMapping(value = "/hosted/create-order", method = RequestMethod.POST)
    public String createOrderHostedJson(HttpServletRequest request, @RequestParam("performCheckout") Boolean performCheckout) throws PaymentException {
        Order createdOrder = paymentService.createPayPalOrderForCurrentOrder(performCheckout);
        String redirect = getApprovalLink(createdOrder);
        if (isAjaxRequest(request)) {
            return "ajaxredirect:" + redirect;
        }
        return "redirect:" + redirect;
    }

    protected String getApprovalLink(Order order) {
        for (LinkDescription links : order.links()) {
            if (links.rel().equals("approve")) {
                return links.href();
            }
        }
        return null;
    }
}
