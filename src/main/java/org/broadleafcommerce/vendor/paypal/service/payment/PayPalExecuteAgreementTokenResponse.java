package org.broadleafcommerce.vendor.paypal.service.payment;

import org.broadleafcommerce.vendor.paypal.api.AgreementToken;

public class PayPalExecuteAgreementTokenResponse implements PayPalResponse {

    protected AgreementToken agreementToken;

    public PayPalExecuteAgreementTokenResponse(AgreementToken agreementToken) {
        this.agreementToken = agreementToken;
    }

    public AgreementToken getAgreementToken() {
        return agreementToken;
    }

}
