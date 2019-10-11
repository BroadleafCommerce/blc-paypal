package org.broadleafcommerce.vendor.paypal.service.payment;

import com.paypal.api.payments.Sale;

public class PayPalSaleRetrievalResponse implements PayPalResponse {

    protected Sale sale;

    public PayPalSaleRetrievalResponse(Sale sale) {
        this.sale = sale;
    }

    public Sale getSale() {
        return sale;
    }
}
