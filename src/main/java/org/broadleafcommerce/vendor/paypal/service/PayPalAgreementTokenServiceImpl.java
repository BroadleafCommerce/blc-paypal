package org.broadleafcommerce.vendor.paypal.service;

import org.broadleafcommerce.payment.service.gateway.ExternalCallPayPalCheckoutService;
import org.broadleafcommerce.vendor.paypal.api.AgreementToken;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreateAgreementTokenRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreateAgreementTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.service.CurrentOrderPaymentRequestService;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.paypal.api.payments.MerchantPreferences;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Plan;

import javax.annotation.Resource;

@Service("blPayPalAgreementTokenService")
public class PayPalAgreementTokenServiceImpl implements PayPalAgreementTokenService {

    @Resource(name = "blExternalCallPayPalCheckoutService")
    protected ExternalCallPayPalCheckoutService externalCallService;

    @Resource(name = "blPayPalWebProfileService")
    protected PayPalWebProfileService webProfileService;

    @Autowired(required = false)
    protected CurrentOrderPaymentRequestService currentOrderPaymentRequestService;

    /**
     * To support PayPal Reference Transactions and Billing Agreement Tokens
     * {@see https://developer.paypal.com/docs/limited-release/reference-transactions}
     *
     * @param performCheckoutOnReturn
     * @return
     * @throws PaymentException
     */
    @Override
    public AgreementToken createPayPalAgreementTokenForCurrentOrder(boolean performCheckoutOnReturn)
            throws PaymentException {
        PaymentRequest paymentRequest = getPaymentRequestForCurrentOrder();

        // Create Agreement Token
        String agreementDescription = constructAgreementDescription(paymentRequest);
        Payer payer = constructPayer(paymentRequest);
        Plan plan = constructPlan(paymentRequest, performCheckoutOnReturn);
        AgreementToken agreementToken = new AgreementToken(agreementDescription, payer, plan);
        return createAgreementToken(agreementToken, paymentRequest);
    }

    protected Plan constructPlan(PaymentRequest paymentRequest, boolean performCheckoutOnReturn) {
        Plan plan = new Plan();
        plan.setType(MessageConstants.PLAN_TYPE_MERCHANTINITIATEDBILLING);

        // Set up merchant preferences
        MerchantPreferences merchantPreferences = new MerchantPreferences();
        merchantPreferences.setCancelUrl(externalCallService.getConfiguration().getCancelUrl());
        String returnUrl = externalCallService.getConfiguration().getReturnUrl();
        if (performCheckoutOnReturn) {
            returnUrl += "?" + MessageConstants.CHECKOUT_COMPLETE + "=true";
        }
        merchantPreferences.setReturnUrl(returnUrl);
        merchantPreferences
                .setAcceptedPaymentType(MessageConstants.MERCHANTPREF_ACCEPTEDPAYMENTTYPE_INSTANT);
        plan.setMerchantPreferences(merchantPreferences);
        return plan;
    }

    protected Payer constructPayer(PaymentRequest paymentRequest) {
        Payer payer = new Payer();
        payer.setPaymentMethod(MessageConstants.PAYER_PAYMENTMETHOD_PAYPAL);
        return payer;
    }

    protected AgreementToken createAgreementToken(AgreementToken agreementToken,
            PaymentRequest paymentRequest) throws PaymentException {
        PayPalCreateAgreementTokenResponse response =
                (PayPalCreateAgreementTokenResponse) externalCallService.call(
                        new PayPalCreateAgreementTokenRequest(agreementToken,
                                externalCallService.constructAPIContext(paymentRequest)));
        return response.getAgreementToken();
    }

    protected String constructAgreementDescription(PaymentRequest paymentRequest) {
        return externalCallService.getConfiguration().getPaymentDescription();
    }

    @Override
    public PaymentRequest getPaymentRequestForCurrentOrder() throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            return currentOrderPaymentRequestService.getPaymentRequestFromCurrentOrder();
        } else {
            throw new PaymentException("Unable to get PaymentRequestDTO for current order");
        }
    }

    @Override
    public String getPayPalBillingAgreementIdFromCurrentOrder() throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            return currentOrderPaymentRequestService
                    .retrieveOrderAttributeFromCurrentOrder(MessageConstants.BILLINGAGREEMENTID);
        } else {
            throw new PaymentException(
                    "Unable to retrieve PayPal Billing Agreement ID for current order");
        }
    }

    @Override
    public void setPayPalBillingAgreementIdOnCurrentOrder(String billingAgreementId)
            throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            currentOrderPaymentRequestService.addOrderAttributeToCurrentOrder(
                    MessageConstants.BILLINGAGREEMENTID, billingAgreementId);
        } else {
            throw new PaymentException(
                    "Unable to set PayPal Billing Agreement ID on current order");
        }
    }

    @Override
    public String getPayPalAgreementTokenFromCurrentOrder() throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            return currentOrderPaymentRequestService
                    .retrieveOrderAttributeFromCurrentOrder(MessageConstants.AGREEMENTTOKENID);
        } else {
            throw new PaymentException(
                    "Unable to retrieve PayPal Agreement Token for current order");
        }
    }

    @Override
    public void setPayPalAgreementTokenOnCurrentOrder(String agreementToken)
            throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            currentOrderPaymentRequestService.addOrderAttributeToCurrentOrder(
                    MessageConstants.AGREEMENTTOKENID, agreementToken);
        } else {
            throw new PaymentException("Unable to set PayPal Agreement token on current order");
        }
    }

}
