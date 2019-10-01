package org.broadleafcommerce.vendor.paypal.service.payment;

import com.paypal.api.payments.Authorization;
import com.paypal.api.payments.Payment;

public class PayPalAuthorizeResponse implements PayPalResponse {

    protected Payment authorizedPayment;
    protected Authorization authorization;

    public PayPalAuthorizeResponse(Payment authorizedPayment) {
        this.authorizedPayment = authorizedPayment;
    }

    public Payment getAuthorizedPayment() {
        return authorizedPayment;
    }

    public Authorization getAuthorization() {
        if (authorization == null) {
            this.authorization = authorizedPayment.getTransactions().get(0).getRelatedResources()
                    .get(0).getAuthorization();
        }
        return authorization;
    }
}
