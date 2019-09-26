package org.broadleafcommerce.vendor.paypal.service.payment;

import com.paypal.api.payments.Capture;

public class PayPalCaptureResponse implements PayPalResponse {

    protected Capture capture;

    public PayPalCaptureResponse(Capture capture) {
        this.capture = capture;
    }

    public Capture getCapture() {
        return capture;
    }
}
