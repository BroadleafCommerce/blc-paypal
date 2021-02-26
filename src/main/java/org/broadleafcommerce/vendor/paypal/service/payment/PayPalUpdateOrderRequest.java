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
import com.paypal.orders.OrdersPatchRequest;
import com.paypal.orders.Patch;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.vendor.paypal.service.PayPalClientProvider;

import java.io.IOException;
import java.util.List;

public class PayPalUpdateOrderRequest extends AbstractPayPalRequest<Void, OrdersPatchRequest> {

    private static final Log LOG = LogFactory.getLog(PayPalUpdateOrderRequest.class);

    private final String orderId;

    private final List<Patch> patches;

    public PayPalUpdateOrderRequest(PayPalClientProvider clientProvider,
                                    PaymentRequestDTO paymentRequest, String orderId,
                                    List<Patch> patches) {
        super(clientProvider, paymentRequest);
        this.orderId = orderId;
        this.patches = patches;
    }

    @Override
    protected OrdersPatchRequest buildRequest() {
        OrdersPatchRequest request = new OrdersPatchRequest(getOrderId());
        request.requestBody(getPatches());
        return request;
    }

    @Override
    protected AbstractPayPalResponse<Void> executeInternal() throws IOException {
        HttpResponse<Void> response = getClient().execute(getRequest());
        return new PayPalUpdateOrderResponse(response);
    }

    @Override
    protected boolean isValidInternal() {
        List<Patch> requestContent = getPatches();
        boolean paymentValid = StringUtils.isNotBlank(orderId);

        if (!paymentValid || !CollectionUtils.isNotEmpty(requestContent)) {
            return false;
        }

        for (int i = 0, requestContentSize = requestContent.size(); i < requestContentSize; i++) {
            Patch patch = requestContent.get(i);
            boolean changesNothing = patch == null || StringUtils.isBlank(patch.path())
                    || StringUtils.isBlank(patch.op());
            if (changesNothing) {
                LOG.debug(String.format("Patch %d didn't contain any change info", i));
                return false;
            }

            boolean missingChangeValue = !"remove".equals(patch.op()) && patch.value() == null;

            if (missingChangeValue) {
                LOG.debug(String.format("Patch %d ({ op: %s, path: %s, from: %s }) doesn't contain a value", i,
                        patch.op(), patch.path(), patch.from()));
                return false;
            }
        }

        return true;
    }

    protected String getOrderId() {
        return this.orderId;
    }

    protected List<Patch> getPatches() {
        return this.patches;
    }
}
