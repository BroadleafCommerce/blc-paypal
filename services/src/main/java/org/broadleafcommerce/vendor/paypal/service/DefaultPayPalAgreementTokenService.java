package org.broadleafcommerce.vendor.paypal.service;

import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutExternalCallService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutRestConfigurationProperties;
import org.broadleafcommerce.vendor.paypal.api.AgreementToken;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreateAgreementTokenRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreateAgreementTokenResponse;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.paypal.api.payments.MerchantPreferences;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Plan;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultPayPalAgreementTokenService implements PayPalAgreementTokenService {

    private final PayPalCheckoutExternalCallService externalCallService;

    /**
     * To support PayPal Reference Transactions and Billing Agreement Tokens
     * {@see https://developer.paypal.com/docs/limited-release/reference-transactions}
     *
     * @param performCheckoutOnReturn
     * @return
     * @throws PaymentException
     */
    @Override
    public AgreementToken createPayPalAgreementToken(PaymentRequest paymentRequest,
            boolean performCheckoutOnReturn)
            throws PaymentException {
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

        PayPalCheckoutRestConfigurationProperties configProperties =
                externalCallService.getConfigProperties();

        // Set up merchant preferences
        MerchantPreferences merchantPreferences = new MerchantPreferences();
        merchantPreferences.setCancelUrl(configProperties.getCancelUrl(paymentRequest));
        String returnUrl = configProperties.getReturnUrl(paymentRequest);
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
        return externalCallService.getConfigProperties().getPaymentDescription();
    }

}
