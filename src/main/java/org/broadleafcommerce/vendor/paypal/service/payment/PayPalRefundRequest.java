package org.broadleafcommerce.vendor.paypal.service.payment;

import org.apache.commons.lang3.StringUtils;

import com.paypal.api.payments.Capture;
import com.paypal.api.payments.RefundRequest;
import com.paypal.api.payments.Sale;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;


public class PayPalRefundRequest extends PayPalRequest {

    protected RefundRequest refundRequest;
    protected Capture capture;
    protected Sale sale;

    public PayPalRefundRequest(RefundRequest refundRequest, Capture capture,
            APIContext apiContext) {
        super(apiContext);
        this.refundRequest = refundRequest;
        this.capture = capture;
    }

    public PayPalRefundRequest(RefundRequest refundRequest, Sale sale, APIContext apiContext) {
        super(apiContext);
        this.refundRequest = refundRequest;
        this.sale = sale;
    }

    @Override
    protected PayPalResponse executeInternal() throws PayPalRESTException {
        if (capture != null) {
            return new PayPalRefundResponse(capture.refund(apiContext, refundRequest));
        } else {
            return new PayPalRefundResponse(sale.refund(apiContext, refundRequest));
        }
    }

    @Override
    protected boolean isRequestValid() {
        return refundRequest != null && refundRequest.getAmount() != null
                && ((capture != null && StringUtils.isNoneBlank(capture.getId()))
                        || (sale != null && StringUtils.isNoneBlank(sale.getId())));
    }

}
