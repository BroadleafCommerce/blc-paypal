package org.broadleafcommerce.vendor.paypal.service.payment;

import com.paypal.api.payments.Capture;

public class PayPalCaptureRetrievalResponse implements PayPalResponse {

    protected Capture capture;

    public PayPalCaptureRetrievalResponse(Capture capture) {
        this.capture = capture;
    }

    public Capture getCapture() {
        return capture;
    }
}
