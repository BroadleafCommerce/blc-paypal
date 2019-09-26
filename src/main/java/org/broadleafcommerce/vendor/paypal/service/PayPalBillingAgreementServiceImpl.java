package org.broadleafcommerce.vendor.paypal.service;

import org.broadleafcommerce.payment.service.gateway.ExternalCallPayPalCheckoutService;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreateBillingAgreementRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreateBillingAgreementResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreatePlanRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreatePlanResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalUpdatePlanRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.service.CurrentOrderPaymentRequestService;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
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
 * BETA: Placeholder service to facilitate creating billing agreements and recurring subscriptions
 * via the Payments API.
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
    public Agreement createPayPalBillingAgreementForCurrentOrder(boolean performCheckoutOnReturn)
            throws PaymentException {
        PaymentRequest paymentRequest = getPaymentRequestForCurrentOrder();

        // 1. Create Plan
        Plan plan = constructPlan(paymentRequest);

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

        updatePlan(plan, patchRequestList, paymentRequest);

        // 3. Create Agreement Details
        Agreement agreement = constructAgreement(paymentRequest, plan);

        return createAgreement(agreement, paymentRequest);
    }

    protected Agreement constructAgreement(PaymentRequest paymentRequest, Plan plan) {
        Payer payer = constructPayer(paymentRequest);

        // Add agreement details
        Agreement agreement = new Agreement();
        agreement.setName("Billing Agreement");
        agreement.setDescription("Billing Agreement");
        agreement.setPayer(payer);
        Plan agreementPlan = new Plan();
        agreementPlan.setId(plan.getId());
        agreement.setPlan(agreementPlan);
        agreement.setStartDate("2018-12-22T09:13:49Z");

        if (paymentRequest.shipToPopulated()) {
            ShippingAddress address = externalCallService.getPayPalShippingAddress(paymentRequest);
            agreement.setShippingAddress(address);
        }
        return agreement;
    }


    protected Payer constructPayer(PaymentRequest paymentRequest) {
        Payer payer = new Payer();
        payer.setPaymentMethod(MessageConstants.PAYER_PAYMENTMETHOD_PAYPAL);
        return payer;
    }

    protected Plan constructPlan(PaymentRequest paymentRequest) throws PaymentException {
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
        Amount amt = externalCallService.getPayPalAmountFromOrder(paymentRequest);
        paymentDefinition.setAmount(new Currency(amt.getCurrency(), amt.getTotal()));
        paymentDefinitions.add(paymentDefinition);
        plan.setPaymentDefinitions(paymentDefinitions);

        return createPlan(plan, paymentRequest);
    }

    protected Plan createPlan(Plan plan, PaymentRequest paymentRequest) throws PaymentException {
        PayPalCreatePlanResponse response = (PayPalCreatePlanResponse) externalCallService.call(
                new PayPalCreatePlanRequest(plan,
                        externalCallService.constructAPIContext(paymentRequest)));
        return response.getPlan();
    }

    protected void updatePlan(Plan plan, List<Patch> patches, PaymentRequest paymentRequest)
            throws PaymentException {
        externalCallService.call(new PayPalUpdatePlanRequest(plan,
                patches,
                externalCallService.constructAPIContext(paymentRequest)));
    }

    protected Agreement createAgreement(Agreement agreement, PaymentRequest paymentRequest)
            throws PaymentException {
        PayPalCreateBillingAgreementResponse response =
                (PayPalCreateBillingAgreementResponse) externalCallService.call(
                        new PayPalCreateBillingAgreementRequest(agreement,
                                externalCallService.constructAPIContext(paymentRequest)));
        return response.getAgreement();
    }

    protected PaymentRequest getPaymentRequestForCurrentOrder() throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            return currentOrderPaymentRequestService.getPaymentRequestFromCurrentOrder();
        } else {
            throw new PaymentException("Unable to get PaymentRequestDTO for current order");
        }
    }

    @Override
    public String getPayPalBillingTokenFromCurrentOrder() throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            return currentOrderPaymentRequestService
                    .retrieveOrderAttributeFromCurrentOrder(MessageConstants.BILLINGECTOKEN);
        } else {
            throw new PaymentException(
                    "Unable to retrieve PayPal Billing EC token for current order");
        }
    }

    @Override
    public void setPayPalBillingTokenOnCurrentOrder(String billingToken) throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            currentOrderPaymentRequestService
                    .addOrderAttributeToCurrentOrder(MessageConstants.BILLINGECTOKEN, billingToken);
        } else {
            throw new PaymentException("Unable to set PayPal Billing EC token on current order");
        }
    }



}
