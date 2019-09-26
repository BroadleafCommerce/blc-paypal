package org.broadleafcommerce.vendor.paypal.service.payment;

import org.apache.commons.lang3.StringUtils;

import com.paypal.api.payments.Sale;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

public class PayPalSaleRetrievalRequest extends PayPalRequest {

    protected String saleId;

    public PayPalSaleRetrievalRequest(String saleId, APIContext apiContext) {
        super(apiContext);
        this.saleId = saleId;
    }

    @Override
    protected PayPalResponse executeInternal() throws PayPalRESTException {
        return new PayPalSaleRetrievalResponse(Sale.get(apiContext, saleId));
    }

    @Override
    protected boolean isRequestValid() {
        return StringUtils.isNoneBlank(saleId);
    }

}
