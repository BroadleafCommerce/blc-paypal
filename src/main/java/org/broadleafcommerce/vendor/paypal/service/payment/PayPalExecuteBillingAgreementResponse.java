package org.broadleafcommerce.vendor.paypal.service.payment;

import com.paypal.api.payments.Agreement;

public class PayPalExecuteBillingAgreementResponse implements PayPalResponse {

    protected Agreement agreement;

    public PayPalExecuteBillingAgreementResponse(Agreement agreement) {
        this.agreement = agreement;
    }

    public Agreement getAgreement() {
        return agreement;
    }

}
