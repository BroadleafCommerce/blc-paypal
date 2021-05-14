/*-
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2021 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.vendor.paypal.service.payment;

import com.paypal.http.HttpResponse;
import com.paypal.payments.CapturesRefundRequest;
import com.paypal.payments.Money;
import com.paypal.payments.Refund;
import com.paypal.payments.RefundRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.vendor.paypal.service.PayPalClientProvider;

import java.io.IOException;

/**
 * Represents a refund request. To partially refund an order, specify
 * {@link RefundRequest#amount(Money)}, otherwise the refund request may be empty (but not null).
 */
public class PayPalRefundRequest
        extends AbstractPayPalRequest<Refund, CapturesRefundRequest> {

    private final String captureId;

    private final RefundRequest refundRequest;

    public PayPalRefundRequest(PayPalClientProvider clientProvider,
                               PaymentRequestDTO paymentRequest, RefundRequest refundRequest,
                               String captureId) {
        super(clientProvider, paymentRequest);
        this.refundRequest = refundRequest;
        this.captureId = captureId;
    }

    @Override
    protected CapturesRefundRequest buildRequest() {
        CapturesRefundRequest request = new CapturesRefundRequest(getCaptureId());
        request.requestBody(getRefundRequest());
        return request;
    }

    @Override
    protected AbstractPayPalResponse<Refund> executeInternal() throws IOException {
        HttpResponse<Refund> response = getClient().execute(getRequest());
        return new PayPalRefundResponse(response);
    }

    @Override
    protected boolean isValidInternal() {
        RefundRequest request = getRefundRequest();
        return StringUtils.isNotBlank(captureId) && request != null && isAmountValid();
    }

    private boolean isAmountValid() {
        RefundRequest request = getRefundRequest();
        Money amount = request.amount();

        return (amount == null || (StringUtils.isNoneBlank(amount.currencyCode(), amount.value())
                && NumberUtils.isCreatable(amount.value())));
    }

    protected String getCaptureId() {
        return this.captureId;
    }

    protected RefundRequest getRefundRequest() {
        return this.refundRequest;
    }
}
