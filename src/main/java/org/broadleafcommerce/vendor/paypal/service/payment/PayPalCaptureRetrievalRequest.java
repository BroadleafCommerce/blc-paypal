package org.broadleafcommerce.vendor.paypal.service.payment;

import org.apache.commons.lang3.StringUtils;

import com.paypal.api.payments.Capture;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;


public class PayPalCaptureRetrievalRequest extends PayPalRequest {

    protected String captureId;

    public PayPalCaptureRetrievalRequest(String captureId, APIContext apiContext) {
        super(apiContext);
        this.captureId = captureId;
    }

    @Override
    protected PayPalResponse executeInternal() throws PayPalRESTException {
        return new PayPalCaptureRetrievalResponse(Capture.get(apiContext, captureId));
    }

    @Override
    protected boolean isRequestValid() {
        return StringUtils.isNoneBlank(captureId);
    }

}
