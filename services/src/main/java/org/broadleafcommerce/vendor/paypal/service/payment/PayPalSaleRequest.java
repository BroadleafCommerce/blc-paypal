/*
 * Copyright (C) 2009 - 2020 Broadleaf Commerce
 *
 * Licensed under the Broadleaf End User License Agreement (EULA), Version 1.1 (the
 * "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt).
 *
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the
 * "Custom License") between you and Broadleaf Commerce. You may not use this file except in
 * compliance with the applicable license.
 *
 * NOTICE: All information contained herein is, and remains the property of Broadleaf Commerce, LLC
 * The intellectual and technical concepts contained herein are proprietary to Broadleaf Commerce,
 * LLC and may be covered by U.S. and Foreign Patents, patents in process, and are protected by
 * trade secret or copyright law. Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained from Broadleaf Commerce, LLC.
 */
package org.broadleafcommerce.vendor.paypal.service.payment;

import org.apache.commons.lang3.StringUtils;

import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

public class PayPalSaleRequest extends PayPalRequest {

    protected Payment payment;
    protected PaymentExecution paymentExecution;

    public PayPalSaleRequest(Payment payment, PaymentExecution paymentExecution,
            APIContext apiContext) {
        super(apiContext);
        this.payment = payment;
        this.paymentExecution = paymentExecution;
    }

    @Override
    public PayPalResponse executeInternal() throws PayPalRESTException {
        return new PayPalSaleResponse(payment.execute(apiContext, paymentExecution));
    }

    @Override
    protected boolean isRequestValid() {
        return payment != null && StringUtils.isNoneBlank(payment.getId())
                && paymentExecution != null
                && StringUtils.isNoneBlank(paymentExecution.getPayerId());
    }
}
