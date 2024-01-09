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
package org.broadleafcommerce.vendor.paypal.service.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.http.HttpResponse;
import com.paypal.http.exceptions.HttpException;
import com.paypal.http.exceptions.SerializeException;
import com.paypal.http.serializer.Json;
import com.paypal.orders.AmountWithBreakdown;
import com.paypal.payments.Money;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author Nathan Moore (nathandmoore)
 */
@Service("blPayPalUtils")
public class PayPalUtils {

    private static final Log LOG = LogFactory.getLog(PayPalUtils.class);

    @Autowired
    private ObjectMapper mapper;

    public String convertResponseToJson(HttpResponse<?> response) {
        try {
            return new Json().serialize(response.result());
        } catch (SerializeException e) {
            LOG.error("Could not convert response into JSON", e);
        }
        return null;
    }

    public PayPalErrorResponse convertFromErrorJson(HttpException ex) {
        try {
            return mapper.readValue(ex.getMessage(), PayPalErrorResponse.class);
        } catch (IOException e) {
            LOG.error("Could not convert error JSON", e);
            PayPalErrorResponse response = new PayPalErrorResponse();
            response.setMessage(HttpStatus.valueOf(ex.statusCode()).getReasonPhrase());
            return response;
        }
    }

    public org.broadleafcommerce.common.money.Money getMoneyForAmountWithBreakdown(AmountWithBreakdown amount) {
        return new org.broadleafcommerce.common.money.Money(amount.value(), amount.currencyCode());
    }

    public org.broadleafcommerce.common.money.Money convertOrdersMoneyToMoney(com.paypal.orders.Money money) {
        return new org.broadleafcommerce.common.money.Money(money.value(), money.currencyCode());
    }

    public org.broadleafcommerce.common.money.Money convertPaymentsMoneyToMoney(com.paypal.payments.Money money) {
        return new org.broadleafcommerce.common.money.Money(money.value(), money.currencyCode());
    }

    public Money convertToPaymentsMoney(String value, String currencyCode) {
        return new Money().value(value).currencyCode(currencyCode);
    }

    public com.paypal.orders.Money convertToOrdersMoney(String value, String currencyCode) {
        return new com.paypal.orders.Money().value(value).currencyCode(currencyCode);
    }
}
