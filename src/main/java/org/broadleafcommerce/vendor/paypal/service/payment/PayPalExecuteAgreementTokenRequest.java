package org.broadleafcommerce.vendor.paypal.service.payment;

import org.broadleafcommerce.vendor.paypal.api.AgreementToken;

import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

public class PayPalExecuteAgreementTokenRequest extends PayPalRequest {

    protected AgreementToken agreementToken;

    public PayPalExecuteAgreementTokenRequest(AgreementToken agreementToken,
            APIContext apiContext) {
        super(apiContext);
        this.agreementToken = agreementToken;
    }

    @Override
    protected PayPalResponse executeInternal() throws PayPalRESTException {
        return new PayPalExecuteAgreementTokenResponse(
                AgreementToken.execute(agreementToken, apiContext));
    }

    @Override
    protected boolean isRequestValid() {
        return agreementToken != null && agreementToken.getTokenId() != null;
    }

}
