package org.broadleafcommerce.vendor.paypal.service.payment;

import com.paypal.api.payments.DetailedRefund;

public class PayPalRefundResponse implements PayPalResponse {

    protected DetailedRefund detailedRefund;

    public PayPalRefundResponse(DetailedRefund detailedRefund) {
        this.detailedRefund = detailedRefund;
    }

    public DetailedRefund getDetailedRefund() {
        return detailedRefund;
    }
}
