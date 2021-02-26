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
import com.paypal.orders.Order;
import com.paypal.orders.OrderRequest;
import com.paypal.orders.OrdersCaptureRequest;
import org.apache.commons.lang3.StringUtils;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.vendor.paypal.service.PayPalClientProvider;

import java.io.IOException;

/**
 * Request object used to capture an order immediately. To auth the order immediately but capture it
 * later, use {@link PayPalCaptureAuthRequest}.
 *
 * @author Nathan Moore (nathandmoore)
 */
public class PayPalCaptureOrderRequest extends AbstractPayPalRequest<Order, OrdersCaptureRequest> {

    private final String orderId;

    public PayPalCaptureOrderRequest(PayPalClientProvider clientProvider,
                                     PaymentRequestDTO paymentRequest, String orderId) {
        super(clientProvider, paymentRequest);
        this.orderId = orderId;
    }

    @Override
    protected OrdersCaptureRequest buildRequest() {
        OrdersCaptureRequest request = new OrdersCaptureRequest(getOrderId());
        request.requestBody(new OrderRequest());
        return request;
    }

    @Override
    protected AbstractPayPalResponse<Order> executeInternal() throws IOException {
        HttpResponse<Order> response = getClient().execute(getRequest());
        return new PayPalCaptureOrderResponse(response);
    }

    @Override
    protected boolean isValidInternal() {
        return StringUtils.isNotBlank(getOrderId());
    }

    protected String getOrderId() {
        return this.orderId;
    }
}
