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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.CurrentOrderPaymentRequestService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayConfiguration;
import org.broadleafcommerce.common.payment.service.PaymentGatewayHostedService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayWebResponseService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.common.web.payment.controller.PaymentGatewayAbstractController;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Controller("blPayPalExpressController")
@RequestMapping("/" + BroadleafPayPalExpressController.GATEWAY_CONTEXT_KEY)
public class BroadleafPayPalExpressController extends PaymentGatewayAbstractController {

    protected static final Log LOG = LogFactory.getLog(BroadleafPayPalExpressController.class);
    protected static final String GATEWAY_CONTEXT_KEY = "paypal-express";

    @Resource(name = "blPayPalExpressWebResponseService")
    protected PaymentGatewayWebResponseService paymentGatewayWebResponseService;

    @Resource(name = "blPayPalExpressHostedService")
    private PaymentGatewayHostedService paymentGatewayHostedService;

    @Resource(name = "blPayPalExpressConfiguration")
    protected PaymentGatewayConfiguration paymentGatewayConfiguration;

    @Autowired(required=false)
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
                                 @PathVariable Map<String, String> pathVars)
            throws PaymentException {
        return super.process(model, request, redirectAttributes);
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
    // PayPal Express Redirect Endpoint
    // which generates the links on page load
    // ***********************************************
    @RequestMapping(value = "/redirect", method = RequestMethod.GET)
    public String redirectEndpoint(Model model, HttpServletRequest request) throws PaymentException {

        PaymentRequestDTO requestDTO = getCurrentOrder();

        if (requestDTO != null) {
            try {
                if ( request.getParameterMap().containsKey("complete") ) {
                    Boolean completeCheckout = Boolean.parseBoolean(request.getParameter("complete"));
                    requestDTO.completeCheckoutOnCallback(completeCheckout);
                }

                PaymentResponseDTO responseDTO = paymentGatewayHostedService.requestHostedEndpoint(requestDTO);
                String url = responseDTO.getResponseMap().get(MessageConstants.REDIRECTURL).toString();

                //https://developer.paypal.com/docs/classic/express-checkout/integration-guide/ECCustomizing/
                if (requestDTO.isCompleteCheckoutOnCallback()) {
                    url = url + "&useraction=commit";
                }

                url = "redirect:" + url;
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Redirecting to PayPal Express with URL: " + url);
                }

                return url;
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Unable to Create the PayPal Express Redirect Link.", e);
                }
            }
        }

        throw new PaymentException("Unable to Create the PayPal Express Redirect Link. The Request DTO is null");

    }

    protected PaymentRequestDTO getCurrentOrder() {
        if (LOG.isErrorEnabled()) {
            if (currentOrderPaymentRequestService == null) {
                LOG.trace("getCurrentOrder: CurrentOrderPaymentRequestService is null. Please check your configuration.");
            }
        }

        if (currentOrderPaymentRequestService != null) {
            return currentOrderPaymentRequestService.getPaymentRequestFromCurrentOrder();
        }
        return null;
    }

}
