package org.broadleafcommerce.vendor.paypal.service;

import org.broadleafcommerce.vendor.paypal.api.AgreementToken;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;

public interface PayPalAgreementTokenService {
    AgreementToken createPayPalAgreementTokenForCurrentOrder(boolean performCheckoutOnReturn)
            throws PaymentException;

    PaymentRequest getPaymentRequestForCurrentOrder() throws PaymentException;

    String getPayPalBillingAgreementIdFromCurrentOrder() throws PaymentException;

    void setPayPalBillingAgreementIdOnCurrentOrder(String billingAgreementId)
            throws PaymentException;

    String getPayPalAgreementTokenFromCurrentOrder() throws PaymentException;

    void setPayPalAgreementTokenOnCurrentOrder(String agreementToken) throws PaymentException;
}
