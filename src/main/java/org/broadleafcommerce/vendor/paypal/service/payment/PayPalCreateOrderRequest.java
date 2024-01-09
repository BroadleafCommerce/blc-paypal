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
import com.paypal.orders.AmountBreakdown;
import com.paypal.orders.AmountWithBreakdown;
import com.paypal.orders.Order;
import com.paypal.orders.OrderRequest;
import com.paypal.orders.OrdersCreateRequest;
import com.paypal.orders.PurchaseUnitRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.vendor.paypal.service.PayPalClientProvider;

import java.io.IOException;

public class PayPalCreateOrderRequest extends AbstractPayPalRequest<Order, OrdersCreateRequest> {

    private final OrderRequest orderRequest;

    public PayPalCreateOrderRequest(PayPalClientProvider clientProvider,
                                    PaymentRequestDTO paymentRequest,
                                    OrderRequest orderRequest) {
        super(clientProvider, paymentRequest);
        this.orderRequest = orderRequest;
    }

    @Override
    protected OrdersCreateRequest buildRequest() {
        OrdersCreateRequest request = new OrdersCreateRequest();
        request.prefer("return=representation");
        request.requestBody(getOrderRequest());
        return request;
    }

    @Override
    protected AbstractPayPalResponse<Order> executeInternal() throws IOException {
        HttpResponse<Order> response = getClient().execute(getRequest());
        return new PayPalCreateOrderResponse(response);
    }

    @Override
    protected boolean isValidInternal() {
        OrderRequest request = getOrderRequest();

        if (request == null || request.payer() == null
                || CollectionUtils.isEmpty(request.purchaseUnits())
                || StringUtils.isBlank(request.checkoutPaymentIntent())) {
            return false;
        }

        PurchaseUnitRequest purchaseUnitRequest = request.purchaseUnits().get(0);

        if (purchaseUnitRequest == null || purchaseUnitRequest.amountWithBreakdown() == null) {
            return false;
        }

        AmountWithBreakdown totalWithBreakdown = purchaseUnitRequest.amountWithBreakdown();
        String totalCurrency = totalWithBreakdown.currencyCode();
        String totalAmount = totalWithBreakdown.value();

        if (totalCurrency != null && StringUtils.isBlank(totalAmount)) {
            return false;
        }

        AmountBreakdown subtotals = totalWithBreakdown.amountBreakdown();

        return subtotals != null
                && (subtotals.itemTotal() != null
                && StringUtils.isNotBlank(subtotals.itemTotal().value()))
                && (subtotals.shipping() == null
                || StringUtils.isNotBlank(subtotals.shipping().value()))
                && (subtotals.taxTotal() == null
                || StringUtils.isNotBlank(subtotals.taxTotal().value()));
    }

    protected OrderRequest getOrderRequest() {
        return this.orderRequest;
    }
}
