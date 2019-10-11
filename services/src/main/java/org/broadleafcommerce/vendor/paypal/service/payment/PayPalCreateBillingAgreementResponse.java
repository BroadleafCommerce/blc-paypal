package org.broadleafcommerce.vendor.paypal.service.payment;


import com.paypal.api.payments.Agreement;

public class PayPalCreateBillingAgreementResponse implements PayPalResponse {

    protected Agreement agreement;

    public PayPalCreateBillingAgreementResponse(Agreement agreement) {
        this.agreement = agreement;
    }

    public Agreement getAgreement() {
        return agreement;
    }
}
