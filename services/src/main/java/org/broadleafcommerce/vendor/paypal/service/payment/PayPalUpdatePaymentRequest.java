package org.broadleafcommerce.vendor.paypal.service.payment;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.paypal.api.payments.Patch;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

import java.util.List;

public class PayPalUpdatePaymentRequest extends PayPalRequest {

    protected Payment payment;
    protected List<Patch> patches;

    public PayPalUpdatePaymentRequest(Payment payment, List<Patch> patches, APIContext apiContext) {
        super(apiContext);
        this.payment = payment;
        this.patches = patches;
    }

    @Override
    protected PayPalResponse executeInternal() throws PayPalRESTException {
        payment.update(apiContext, patches);
        return new PayPalUpdatePaymentResponse();
    }

    @Override
    protected boolean isRequestValid() {
        boolean paymentValid = payment != null && StringUtils.isNotBlank(payment.getId());
        if (paymentValid && CollectionUtils.isNotEmpty(patches)) {
            for (Patch patch : patches) {
                if (patch == null || StringUtils.isBlank(patch.getPath())
                        || StringUtils.isBlank(patch.getOp())) {
                    return false;
                }
                if (!"remove".equals(patch.getOp()) && patch.getValue() == null) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
