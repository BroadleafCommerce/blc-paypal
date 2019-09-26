package org.broadleafcommerce.vendor.paypal.service.payment;

import org.apache.commons.lang3.StringUtils;

import com.paypal.api.payments.Payment;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;


public class PayPalPaymentRetrievalRequest extends PayPalRequest {

    protected String paymentId;

    public PayPalPaymentRetrievalRequest(String paymentId, APIContext apiContext) {
        super(apiContext);
        this.paymentId = paymentId;
    }

    @Override
    protected PayPalResponse executeInternal() throws PayPalRESTException {
        return new PayPalPaymentRetrievalResponse(Payment.get(apiContext, paymentId));
    }

    @Override
    protected boolean isRequestValid() {
        return StringUtils.isNotBlank(paymentId);
    }

}
