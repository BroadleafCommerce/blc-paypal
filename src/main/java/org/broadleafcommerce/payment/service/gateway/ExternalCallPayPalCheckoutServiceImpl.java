/*
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2014 Broadleaf Commerce
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
package org.broadleafcommerce.payment.service.gateway;

import org.broadleafcommerce.common.payment.service.AbstractExternalPaymentGatewayCall;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blExternalCallPayPalCheckoutService")
public class ExternalCallPayPalCheckoutServiceImpl extends AbstractExternalPaymentGatewayCall<PayPalRequest, PayPalResponse> implements ExternalCallPayPalCheckoutService {

    @Resource(name = "blPayPalCheckoutConfiguration")
    protected PayPalCheckoutConfiguration configuration;

    @Override
    public PayPalCheckoutConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public String getServiceName() {
        return getClass().getName();
    }

    @Override
    public <T extends PayPalResponse> T call(PayPalRequest paymentRequest, Class<T> responseType) throws PaymentException {
        return responseType.cast(super.process(paymentRequest));
    }

    @Override
    public PayPalResponse communicateWithVendor(PayPalRequest paymentRequest) throws Exception {
        return paymentRequest.execute();
    }

    @Override
    public Integer getFailureReportingThreshold() {
        return configuration.getFailureReportingThreshold();
    }
}