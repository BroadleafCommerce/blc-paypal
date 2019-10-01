package org.broadleafcommerce.vendor.paypal.service;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.paypal.api.payments.Agreement;

public interface PayPalBillingAgreementService {

    Agreement createPayPalBillingAgreement(PaymentRequest paymentRequest,
            boolean performCheckoutOnReturn)
            throws PaymentException;

}
