package org.broadleafcommerce.vendor.paypal.service;

import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.paypal.api.payments.Agreement;

public interface PayPalBillingAgreementService {

    Agreement createPayPalBillingAgreementForCurrentOrder(boolean performCheckoutOnReturn)
            throws PaymentException;

    String getPayPalBillingTokenFromCurrentOrder() throws PaymentException;

    void setPayPalBillingTokenOnCurrentOrder(String billingToken) throws PaymentException;
}
