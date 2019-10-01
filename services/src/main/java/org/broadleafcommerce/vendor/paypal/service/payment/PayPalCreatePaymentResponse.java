package org.broadleafcommerce.vendor.paypal.service.payment;

import com.paypal.api.payments.Payment;

public class PayPalCreatePaymentResponse implements PayPalResponse {

    protected Payment payment;

    public PayPalCreatePaymentResponse(Payment payment) {
        this.payment = payment;
    }

    public Payment getPayment() {
        return payment;
    }
}
