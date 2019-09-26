package org.broadleafcommerce.vendor.paypal.service.payment;

import org.broadleafcommerce.vendor.paypal.api.AgreementToken;

import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

public class PayPalCreateAgreementTokenRequest extends PayPalRequest {

    protected AgreementToken agreementToken;

    public PayPalCreateAgreementTokenRequest(AgreementToken agreementToken, APIContext apiContext) {
        super(apiContext);
        this.agreementToken = agreementToken;
    }

    @Override
    protected PayPalResponse executeInternal() throws PayPalRESTException {
        return new PayPalCreateAgreementTokenResponse(agreementToken.create(apiContext));
    }

    @Override
    protected boolean isRequestValid() {
        return (agreementToken != null && agreementToken.getDescription() != null &&
                agreementToken.getPayer() != null && agreementToken.getPlan() != null);
    }
}
