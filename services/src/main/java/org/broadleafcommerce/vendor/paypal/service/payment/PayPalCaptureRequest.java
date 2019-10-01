package org.broadleafcommerce.vendor.paypal.service.payment;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.paypal.api.payments.Authorization;
import com.paypal.api.payments.Capture;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;


public class PayPalCaptureRequest extends PayPalRequest {

    protected Authorization authorization;
    protected Capture capture;

    public PayPalCaptureRequest(Authorization authorization, Capture capture,
            APIContext apiContext) {
        super(apiContext);
        this.authorization = authorization;
        this.capture = capture;
    }

    @Override
    public PayPalResponse executeInternal() throws PayPalRESTException {
        return new PayPalCaptureResponse(authorization.capture(apiContext, capture));
    }

    @Override
    protected boolean isRequestValid() {
        return authorization != null && StringUtils.isNoneBlank(authorization.getId())
                && capture != null && capture.getAmount() != null
                && capture.getAmount().getDetails() == null
                && StringUtils.isNoneBlank(capture.getAmount().getCurrency())
                && StringUtils.isNoneBlank(capture.getAmount().getTotal())
                && NumberUtils.isCreatable(capture.getAmount().getTotal());
    }


}
