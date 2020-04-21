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
package org.broadleafcommerce.payment.service.gateway;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalPaymentRetrievalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalPaymentRetrievalResponse;
import org.springframework.util.Assert;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.domain.PaymentResponse;
import com.broadleafcommerce.paymentgateway.domain.enums.DefaultPaymentTypes;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.paypal.api.payments.Payment;

import lombok.RequiredArgsConstructor;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@RequiredArgsConstructor
public class DefaultPayPalCheckoutReportingService implements PayPalCheckoutReportingService {

    private static final Log LOG = LogFactory.getLog(DefaultPayPalCheckoutReportingService.class);

    private final PayPalCheckoutExternalCallService paypalCheckoutService;

    @Override
    public PaymentResponse findDetailsByTransaction(PaymentRequest paymentRequest)
            throws PaymentException {
        Assert.isTrue(
                paymentRequest.getAdditionalFields().containsKey(MessageConstants.HTTP_PAYERID),
                "The RequestDTO must contain a payerID");
        Assert.isTrue(
                paymentRequest.getAdditionalFields().containsKey(MessageConstants.HTTP_PAYMENTID),
                "The RequestDTO must contain a paymentID");

        PayPalPaymentRetrievalResponse response =
                (PayPalPaymentRetrievalResponse) paypalCheckoutService.call(
                        new PayPalPaymentRetrievalRequest(
                                (String) paymentRequest.getAdditionalFields()
                                        .get(MessageConstants.HTTP_PAYMENTID),
                                paypalCheckoutService.constructAPIContext(paymentRequest)));
        Payment payment = response.getPayment();
        PaymentResponse responseDTO = new PaymentResponse(DefaultPaymentTypes.THIRD_PARTY_ACCOUNT,
                PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);
        paypalCheckoutService.setCommonDetailsResponse(payment, responseDTO);
        responseDTO
                .responseMap(MessageConstants.PAYERID,
                        (String) paymentRequest.getAdditionalFields()
                                .get(MessageConstants.HTTP_PAYERID))
                .responseMap(MessageConstants.PAYMENTID, (String) paymentRequest
                        .getAdditionalFields().get(MessageConstants.HTTP_PAYMENTID));
        LOG.info("ResponseDTO created: "
                + ToStringBuilder.reflectionToString(responseDTO, ToStringStyle.MULTI_LINE_STYLE));
        return responseDTO;
    }

    @Override
    public String getGatewayType() {
        return PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT.name();
    }

}
