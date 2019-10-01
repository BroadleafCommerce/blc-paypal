package org.broadleafcommerce.vendor.paypal.service;

import org.broadleafcommerce.vendor.paypal.api.AgreementToken;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;

public interface PayPalAgreementTokenService {

    AgreementToken createPayPalAgreementToken(PaymentRequest paymentRequest,
            boolean performCheckoutOnReturn)
            throws PaymentException;

}
