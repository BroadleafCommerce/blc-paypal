/*-
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2023 Broadleaf Commerce
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
import com.paypal.payments.AuthorizationsCaptureRequest;
import com.paypal.payments.Capture;
import com.paypal.payments.CaptureRequest;
import com.paypal.payments.Money;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.vendor.paypal.service.PayPalClientProvider;

import java.io.IOException;

/**
 * Request object used to capture an authorization when doing auth immediately but delaying capture
 * until later (such as until after fulfillment). To capture the order immediately, use
 * {@link PayPalCaptureOrderRequest}.
 *
 * @author Nathan Moore
 */
public class PayPalCaptureAuthRequest
        extends AbstractPayPalRequest<Capture, AuthorizationsCaptureRequest> {

    private final String authId;

    private final CaptureRequest captureRequest;

    public PayPalCaptureAuthRequest(PayPalClientProvider clientProvider,
                                    PaymentRequestDTO paymentRequest,
                                    CaptureRequest captureRequest,
                                    String authId) {
        super(clientProvider, paymentRequest);
        this.authId = authId;
        this.captureRequest = captureRequest;
    }

    @Override
    protected AuthorizationsCaptureRequest buildRequest() {
        AuthorizationsCaptureRequest request =
                new AuthorizationsCaptureRequest(getAuthId());
        request.requestBody(getCaptureRequest());
        return request;
    }

    @Override
    public AbstractPayPalResponse<Capture> executeInternal() throws IOException {
        HttpResponse<Capture> response = getClient().execute(getRequest());
        return new PayPalCaptureAuthResponse(response);
    }

    @Override
    protected boolean isValidInternal() {
        CaptureRequest capture = getCaptureRequest();

        if (capture == null) {
            return false;
        }

        Money amount = capture.amount();
        return amount != null
                && StringUtils.isNoneBlank(authId, amount.currencyCode(), amount.value())
                && NumberUtils.isCreatable(amount.value());
    }

    protected String getAuthId() {
        return this.authId;
    }

    protected CaptureRequest getCaptureRequest() {
        return this.captureRequest;
    }
}
