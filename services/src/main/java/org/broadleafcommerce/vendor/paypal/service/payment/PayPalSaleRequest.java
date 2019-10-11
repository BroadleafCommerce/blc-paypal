package org.broadleafcommerce.vendor.paypal.service.payment;

import org.apache.commons.lang3.StringUtils;

import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

public class PayPalSaleRequest extends PayPalRequest {

    protected Payment payment;
    protected PaymentExecution paymentExecution;

    public PayPalSaleRequest(Payment payment, PaymentExecution paymentExecution,
            APIContext apiContext) {
        super(apiContext);
        this.payment = payment;
        this.paymentExecution = paymentExecution;
    }

    @Override
    public PayPalResponse executeInternal() throws PayPalRESTException {
        return new PayPalSaleResponse(payment.execute(apiContext, paymentExecution));
    }

    @Override
    protected boolean isRequestValid() {
        return payment != null && StringUtils.isNoneBlank(payment.getId())
                && paymentExecution != null
                && StringUtils.isNoneBlank(paymentExecution.getPayerId());
    }
}
