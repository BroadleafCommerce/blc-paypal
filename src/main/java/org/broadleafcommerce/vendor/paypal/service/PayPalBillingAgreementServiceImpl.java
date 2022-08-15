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
package org.broadleafcommerce.vendor.paypal.service;

import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.service.CurrentOrderPaymentRequestService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.payment.service.gateway.ExternalCallPayPalCheckoutService;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreateBillingAgreementRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreateBillingAgreementResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreatePlanRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreatePlanResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalUpdatePlanRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.paypal.api.payments.Agreement;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Currency;
import com.paypal.api.payments.MerchantPreferences;
import com.paypal.api.payments.Patch;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.PaymentDefinition;
import com.paypal.api.payments.Plan;
import com.paypal.api.payments.ShippingAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;

/**
 * BETA: Placeholder service to facilitate creating billing agreements and recurring subscriptions via the Payments API.
 *
 * Billing Agreement Tokens and Merchant Initiated Billing Agreements are supported via the
 * {@link PayPalAgreementTokenService}
 */
@Service("blPayPalBillingAgreementService")
public class PayPalBillingAgreementServiceImpl implements PayPalBillingAgreementService {

    @Resource(name = "blExternalCallPayPalCheckoutService")
    protected ExternalCallPayPalCheckoutService externalCallService;

    @Resource(name = "blPayPalWebProfileService")
    protected PayPalWebProfileService webProfileService;

    @Autowired(required = false)
    protected CurrentOrderPaymentRequestService currentOrderPaymentRequestService;

    @Override
    public Agreement createPayPalBillingAgreementForCurrentOrder(boolean performCheckoutOnReturn) throws PaymentException {
        PaymentRequestDTO paymentRequestDTO = getPaymentRequestForCurrentOrder();

        // 1. Create Plan
        Plan plan = constructPlan(paymentRequestDTO);

        // 2. Activate Plan
        List<Patch> patchRequestList = new ArrayList<>();
        Map<String, String> value = new HashMap<>();
        value.put("state", "ACTIVE");

        // Create update object to activate plan
        Patch patch = new Patch();
        patch.setPath("/");
        patch.setValue(value);
        patch.setOp("replace");
        patchRequestList.add(patch);

        updatePlan(plan, patchRequestList, paymentRequestDTO);

        // 3. Create Agreement Details
        Agreement agreement = constructAgreement(paymentRequestDTO, plan);

        return createAgreement(agreement, paymentRequestDTO);
    }

    protected Agreement constructAgreement(PaymentRequestDTO paymentRequestDTO, Plan plan) {
        Payer payer = constructPayer(paymentRequestDTO);

        // Add agreement details
        Agreement agreement = new Agreement();
        agreement.setName("Billing Agreement");
        agreement.setDescription("Billing Agreement");
        agreement.setPayer(payer);
        Plan agreementPlan = new Plan();
        agreementPlan.setId(plan.getId());
        agreement.setPlan(agreementPlan);
        agreement.setStartDate("2018-12-22T09:13:49Z");

        if (paymentRequestDTO.shipToPopulated()) {
            ShippingAddress address = externalCallService.getPayPalShippingAddress(paymentRequestDTO);
            agreement.setShippingAddress(address);
        }
        return agreement;
    }


    protected Payer constructPayer(PaymentRequestDTO paymentRequestDTO) {
        Payer payer = new Payer();
        payer.setPaymentMethod(MessageConstants.PAYER_PAYMENTMETHOD_PAYPAL);
        return payer;
    }

    protected Plan constructPlan(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        // Set up merchant preferences
        MerchantPreferences merchantPreferences = new MerchantPreferences();
        merchantPreferences.setCancelUrl(externalCallService.getConfiguration().getCancelUrl());
        merchantPreferences.setReturnUrl(externalCallService.getConfiguration().getReturnUrl());

        // 1. Set up a plan
        Plan plan = new Plan();
        plan.setName("Test Plan");
        plan.setDescription("Test Plan Description");
        plan.setType("INFINITE");
        plan.setMerchantPreferences(merchantPreferences);
        List<PaymentDefinition> paymentDefinitions = new ArrayList<>();
        PaymentDefinition paymentDefinition = new PaymentDefinition();
        paymentDefinition.setName("Test Regular Payment Definition");
        paymentDefinition.setType("REGULAR");
        paymentDefinition.setFrequencyInterval("1");
        paymentDefinition.setFrequency("YEAR");
        paymentDefinition.setCycles("0");
        Amount amt = externalCallService.getPayPalAmountFromOrder(paymentRequestDTO);
        paymentDefinition.setAmount(new Currency(amt.getCurrency(), amt.getTotal()));
        paymentDefinitions.add(paymentDefinition);
        plan.setPaymentDefinitions(paymentDefinitions);

        return createPlan(plan, paymentRequestDTO);
    }

    protected Plan createPlan(Plan plan, PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        PayPalCreatePlanResponse response = (PayPalCreatePlanResponse) externalCallService.call(
                new PayPalCreatePlanRequest(plan, externalCallService.constructAPIContext(paymentRequestDTO)));
        return response.getPlan();
    }

    protected void updatePlan(Plan plan, List<Patch> patches, PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        externalCallService.call(new PayPalUpdatePlanRequest(plan, patches, externalCallService.constructAPIContext(paymentRequestDTO)));
    }

    protected Agreement createAgreement(Agreement agreement, PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        PayPalCreateBillingAgreementResponse response = (PayPalCreateBillingAgreementResponse) externalCallService.call(
                new PayPalCreateBillingAgreementRequest(agreement, externalCallService.constructAPIContext(paymentRequestDTO)));
        return response.getAgreement();
    }

    protected PaymentRequestDTO getPaymentRequestForCurrentOrder() throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            return currentOrderPaymentRequestService.getPaymentRequestFromCurrentOrder();
        } else {
            throw new PaymentException("Unable to get PaymentRequestDTO for current order");
        }
    }

    @Override
    public String getPayPalBillingTokenFromCurrentOrder() throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            return currentOrderPaymentRequestService.retrieveOrderAttributeFromCurrentOrder(MessageConstants.BILLINGECTOKEN);
        } else {
            throw new PaymentException("Unable to retrieve PayPal Billing EC token for current order");
        }
    }

    @Override
    public void setPayPalBillingTokenOnCurrentOrder(String billingToken) throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            currentOrderPaymentRequestService.addOrderAttributeToCurrentOrder(MessageConstants.BILLINGECTOKEN, billingToken);
        } else {
            throw new PaymentException("Unable to set PayPal Billing EC token on current order");
        }
    }



}
