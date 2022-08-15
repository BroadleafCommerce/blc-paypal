/*-
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2022 Broadleaf Commerce
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
import org.broadleafcommerce.common.payment.service.PaymentGatewayConfiguration;
import org.broadleafcommerce.common.payment.service.PaymentGatewayHostedService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayWebResponseService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.common.web.payment.controller.PaymentGatewayAbstractController;
import org.broadleafcommerce.vendor.paypal.api.AgreementToken;
import org.broadleafcommerce.vendor.paypal.service.PayPalAgreementTokenService;
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
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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

    @Resource(name = "blPayPalAgreementTokenService")
    protected PayPalAgreementTokenService agreementTokenService;

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
     * Completes checkout for a PayPal payment. If there's already a PayPal payment we go ahead and make sure the details
     * of the payment are updated to all of the forms filled out by the customer since they could've updated shipping
     * information, added a promotion, or other various things to the order.
     * 
     * @return Redirect URL to either add the payment and checkout or just checkout
     * @throws PaymentException 
     */
    @RequestMapping(value = "/checkout/complete", method = RequestMethod.POST)
    public String completeCheckout() throws PaymentException {
        paymentService.updatePayPalPaymentForFulfillment();
        String paymentId = paymentService.getPayPalPaymentIdFromCurrentOrder();
        String payerId = paymentService.getPayPalPayerIdFromCurrentOrder();
        if (StringUtils.isBlank(paymentId)) {
            throw new PaymentException("Unable to complete checkout because no PayPal payment id was found on the current order");
        }
        if (StringUtils.isBlank(payerId)) {
            throw new PaymentException("Unable to complete checkout because no PayPal payer id was found on the current order");
        }
        return "redirect:/paypal-checkout/return?" + MessageConstants.HTTP_PAYMENTID + "=" + paymentId + "&" + MessageConstants.HTTP_PAYERID + "=" + payerId;
    }

    @RequestMapping(value = "/billing-agreement-token/complete", method = RequestMethod.POST)
    public String completeBillingAgreementTokenCheckout() throws PaymentException {
        String billingToken = agreementTokenService.getPayPalAgreementTokenFromCurrentOrder();
        if (StringUtils.isBlank(billingToken)) {
            throw new PaymentException("Unable to complete checkout because no PayPal Billing Token was found on the current order");
        }
        return "redirect:/paypal-checkout/return?" + MessageConstants.HTTP_BILLINGTOKEN + "=" + billingToken + "&" + MessageConstants.CHECKOUT_COMPLETE + "=true";
    }

    // ***********************************************
    // PayPal Client side REST checkout (iframe)
    // ***********************************************
    @RequestMapping(value = "/create-payment", method = RequestMethod.POST)
    public @ResponseBody Map<String, String> createPayment(@RequestParam("performCheckout") Boolean performCheckout) throws PaymentException {
        Map<String, String> response = new HashMap<>();
        Payment createdPayment = paymentService.createPayPalPaymentForCurrentOrder(performCheckout);
        response.put("id", createdPayment.getId());
        return response;
    }

    @RequestMapping(value = "/create-billing-agreement-token", method = RequestMethod.POST)
    public @ResponseBody Map<String, String> createBillingAgreementToken(@RequestParam("performCheckout") Boolean performCheckout) throws PaymentException, URISyntaxException {
        Map<String, String> response = new HashMap<>();
        AgreementToken agreementToken = agreementTokenService.createPayPalAgreementTokenForCurrentOrder(performCheckout);
        response.put("id", agreementToken.getTokenId());
        return response;
    }
    
    // ***********************************************
    // PayPal Client side REST checkout (hosted page)
    // ***********************************************
    @RequestMapping(value = "/hosted/create-payment", method = RequestMethod.POST)
    public String createPaymentHostedJson(HttpServletRequest request, @RequestParam("performCheckout") Boolean performCheckout) throws PaymentException {
        Payment createdPayment = paymentService.createPayPalPaymentForCurrentOrder(performCheckout);
        String redirect = getApprovalLink(createdPayment);
        if (isAjaxRequest(request)) {
            return "ajaxredirect:" + redirect;
        }
        return "redirect:" + redirect;
    }

    @RequestMapping(value = "/hosted/create-billing-agreement-token", method = RequestMethod.POST)
    public String createBillingAgreementTokenHostedJson(HttpServletRequest request, @RequestParam("performCheckout") Boolean performCheckout) throws PaymentException {
        AgreementToken agreementToken = agreementTokenService.createPayPalAgreementTokenForCurrentOrder(performCheckout);
        String redirect = getApprovalLink(agreementToken);
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

    protected String getApprovalLink(AgreementToken agreementToken) {
        for (Links links : agreementToken.getLinks()) {
            if (links.getRel().equals("approval_url")) {
                return links.getHref();
            }
        }
        return null;
    }
}
