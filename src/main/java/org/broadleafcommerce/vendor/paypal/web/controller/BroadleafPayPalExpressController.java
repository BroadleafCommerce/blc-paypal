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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Details;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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

    @Resource(name = "blPayPalApiContext")
    protected APIContext apiContext;

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
    @RequestMapping(value = "/return", method = RequestMethod.POST)
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
    // PayPal Client side REST checkout
    // ***********************************************
    @RequestMapping(value = "/create-payment", method = RequestMethod.POST)
    public @ResponseBody Map<String, String> createPayment() throws PayPalRESTException {
        Map<String, String> response = new HashMap<>();
        Payment payment = createPayPalPayment();
        LOG.info("Payment before creation : " + ToStringBuilder.reflectionToString(payment, ToStringStyle.MULTI_LINE_STYLE));
        Payment createdPayment = payment.create(apiContext);
        LOG.info("Payment after creation : " + ToStringBuilder.reflectionToString(createdPayment, ToStringStyle.MULTI_LINE_STYLE));
        response.put("id", createdPayment.getId());
        return response;
    }

    @RequestMapping(value = "/execute-payment", method = RequestMethod.POST)
    public @ResponseBody Map<String, Object> executePayment(@RequestParam("paymentID") String paymentId,
                                                            @RequestParam("payerID") String payerId) throws PayPalRESTException {
        Map<String, Object> response = new HashMap<>();
        Payment payment = new Payment();
        payment.setId(paymentId);
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);
        try {
            Payment createdPayment = payment.execute(apiContext, paymentExecution);
            response.put("success", true);
            response.put("message", createdPayment.getState());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
    
    @RequestMapping(value = "/update-payment", method = RequestMethod.POST)
    public @ResponseBody Boolean updatePayment(@RequestBody PayPalUpdatePaymentDTO updateDto) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(updateDto.getPaymentId());
        payment.update(apiContext, updateDto.getPatches());
        return true;
    }

    public Payment createPayPalPayment() {

        Order order = CartState.getCart();

        // Set payer details
        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        // Set redirect URLs
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl("http://localhost:3000/cancel");
        redirectUrls.setReturnUrl("http://localhost:3000/process");

        // Set payment details
        Details details = new Details();

        details.setShipping(order.getTotalShipping().toString());
        details.setSubtotal(order.getSubTotal().toString());
        details.setTax(order.getTotalTax().toString());

        // Payment amount
        Amount amount = new Amount();
        amount.setCurrency(order.getCurrency().getCurrencyCode());
        // Total must be equal to sum of shipping, tax and subtotal.
        amount.setTotal(order.getTotal().toString());
        amount.setDetails(details);

        // Transaction information
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription("This is the payment transaction description.");
        transaction.setCustom(order.getId().toString());

        // Add transaction to a list
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        // Add payment details
        Payment payment = new Payment();
        payment.setIntent("authorize");
        payment.setPayer(payer);
        payment.setRedirectUrls(redirectUrls);
        payment.setTransactions(transactions);

        return payment;
    }
}
