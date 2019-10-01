package org.broadleafcommerce.vendor.paypal.service.payment;

import com.paypal.api.payments.Payment;
import com.paypal.api.payments.Sale;

public class PayPalSaleResponse implements PayPalResponse {

    protected Payment salePayment;
    protected Sale sale;

    public PayPalSaleResponse(Payment salePayment) {
        this.salePayment = salePayment;
    }

    public Payment getSalePayment() {
        return salePayment;
    }

    public Sale getSale() {
        if (sale == null) {
            this.sale = salePayment.getTransactions().get(0).getRelatedResources().get(0).getSale();
        }
        return sale;
    }

}
