package org.broadleafcommerce.vendor.paypal.service.payment;

import org.broadleafcommerce.vendor.paypal.api.AgreementToken;

public class PayPalCreateAgreementTokenResponse implements PayPalResponse {

    protected AgreementToken agreementToken;

    public PayPalCreateAgreementTokenResponse(AgreementToken agreementToken) {
        this.agreementToken = agreementToken;
    }

    public AgreementToken getAgreementToken() {
        return agreementToken;
    }
}
