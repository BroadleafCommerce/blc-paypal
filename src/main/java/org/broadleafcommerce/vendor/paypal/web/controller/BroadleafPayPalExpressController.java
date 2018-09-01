/*
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2014 Broadleaf Commerce
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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.CurrentOrderPaymentRequestService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayConfiguration;
import org.broadleafcommerce.common.payment.service.PaymentGatewayHostedService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayWebResponseService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.common.web.payment.controller.PaymentGatewayAbstractController;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.web.order.CartState;
import org.broadleafcommerce.payment.service.gateway.ExternalCallPayPalExpressService;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalPaymentInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Controller("blPayPalExpressController")
@RequestMapping("/" + BroadleafPayPalExpressController.GATEWAY_CONTEXT_KEY)
public class BroadleafPayPalExpressController extends PaymentGatewayAbstractController {

    private static final Log LOG = LogFactory.getLog(BroadleafPayPalExpressController.class);
    protected static final String GATEWAY_CONTEXT_KEY = "paypal-express";

    @Resource(name = "blPayPalExpressWebResponseService")
    protected PaymentGatewayWebResponseService paymentGatewayWebResponseService;

    @Resource(name = "blPayPalExpressHostedService")
    protected PaymentGatewayHostedService paymentGatewayHostedService;

    @Resource(name = "blPayPalExpressConfiguration")
    protected PaymentGatewayConfiguration paymentGatewayConfiguration;

    @Resource(name = "blExternalCallPayPalExpressService")
    protected ExternalCallPayPalExpressService paypalExternalService;

    @Autowired(required = false)
    protected CurrentOrderPaymentRequestService currentOrderPaymentRequestService;

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
    // PayPal Express Default Endpoints
    // ***********************************************
    @Override
    @RequestMapping(value = "/return", method = RequestMethod.GET)
    public String returnEndpoint(Model model, HttpServletRequest request,
                                 final RedirectAttributes redirectAttributes,
                                 @PathVariable Map<String, String> pathVars) throws PaymentException {
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
     * Completes checkout for a PayPal payment. If there's already a PayPal payment we go ahead and make sure the details
     * of the payment are updated to all of the forms filled out by the customer since they could've updated shipping
     * information, added a promotion, or other various things to the order.
     * 
     * @return Redirect URL to either add the payment and checkout or just checkout
     * @throws Exception Various exceptions around retrieving the payment, updating the payment, or talking to PayPal
     */
    @RequestMapping(value = "/checkout/complete", method = RequestMethod.POST)
    public String completeCheckout() throws Exception {
        Order cart = CartState.getCart();
        PayPalPaymentInfoDTO info = paypalExternalService.updatePaymentForFulfillment(cart);
        if (info != null) {
            return "redirect:/paypal-express/return?" + MessageConstants.HTTP_PAYMENTID + "=" + info.getPaymentId() + "&" + MessageConstants.HTTP_PAYERID + "=" + info.getPayerId();
        }
        // Typically shouldn't get here. The only way to reach this is to hit the PayPal checkout endpoint without having a PayPal payment
        return baseConfirmationRedirect + "/" + initiateCheckout(cart.getId());
    }

    // ***********************************************
    // PayPal Client side REST checkout (iframe)
    // ***********************************************
    @RequestMapping(value = "/create-payment", method = RequestMethod.POST)
    public @ResponseBody Map<String, String> createPayment(@RequestParam("performCheckout") Boolean performCheckout) throws PaymentException {
        Map<String, String> response = new HashMap<>();
        Payment createdPayment = paypalExternalService.createPayment(CartState.getCart(), performCheckout);
        response.put("id", createdPayment.getId());
        return response;
    }
    
    // ***********************************************
    // PayPal Client side REST checkout (hosted page)
    // ***********************************************
    @RequestMapping(value = "/hosted/create-payment", method = RequestMethod.POST)
    public String createPaymentHostedJson(HttpServletRequest request, @RequestParam("performCheckout") Boolean performCheckout) throws PaymentException {
        Payment createdPayment = paypalExternalService.createPayment(CartState.getCart(), performCheckout);
        String redirect = getApprovalLink(createdPayment);
        if (isAjaxRequest(request)) {
            return "ajaxredirect:" + redirect;
        }
        return "redirect:" + redirect;
    }

    protected String getApprovalLink(Payment payment) {
        for (Links links : payment.getLinks()) {
            if (links.getRel().equals("approval_url")) {
                return links.getHref();
            }
        }
        return null;
    }
}
