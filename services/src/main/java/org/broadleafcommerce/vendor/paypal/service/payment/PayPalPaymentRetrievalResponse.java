package org.broadleafcommerce.vendor.paypal.service.payment;

import com.paypal.api.payments.Payment;

public class PayPalPaymentRetrievalResponse implements PayPalResponse {

    protected Payment payment;

    public PayPalPaymentRetrievalResponse(Payment payment) {
        this.payment = payment;
    }

    public Payment getPayment() {
        return payment;
    }
}
