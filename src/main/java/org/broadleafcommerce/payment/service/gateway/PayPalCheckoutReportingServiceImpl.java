/*-
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2022 Broadleaf Commerce
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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.payment.PaymentType;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.AbstractPaymentGatewayReportingService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayReportingService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCheckoutPaymentGatewayType;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalPaymentRetrievalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalPaymentRetrievalResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import com.paypal.api.payments.Payment;
import javax.annotation.Resource;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalCheckoutReportingService")
public class PayPalCheckoutReportingServiceImpl extends AbstractPaymentGatewayReportingService implements PaymentGatewayReportingService {

    private static final Log LOG = LogFactory.getLog(PayPalCheckoutReportingServiceImpl.class);

    @Resource(name = "blExternalCallPayPalCheckoutService")
    protected ExternalCallPayPalCheckoutService payPalCheckoutService;

    @Override
    public PaymentResponseDTO findDetailsByTransaction(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        Assert.isTrue(paymentRequestDTO.getAdditionalFields().containsKey(MessageConstants.HTTP_PAYERID), "The RequestDTO must contain a payerID");
        Assert.isTrue(paymentRequestDTO.getAdditionalFields().containsKey(MessageConstants.HTTP_PAYMENTID), "The RequestDTO must contain a paymentID");

        PayPalPaymentRetrievalResponse response = (PayPalPaymentRetrievalResponse) payPalCheckoutService.call(
                new PayPalPaymentRetrievalRequest((String) paymentRequestDTO.getAdditionalFields().get(MessageConstants.HTTP_PAYMENTID),
                        payPalCheckoutService.constructAPIContext(paymentRequestDTO)));
        Payment payment = response.getPayment();
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT,
                PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);
        payPalCheckoutService.setCommonDetailsResponse(payment, responseDTO);
        responseDTO.responseMap(MessageConstants.PAYERID, (String) paymentRequestDTO.getAdditionalFields().get(MessageConstants.HTTP_PAYERID))
                    .responseMap(MessageConstants.PAYMENTID, (String) paymentRequestDTO.getAdditionalFields().get(MessageConstants.HTTP_PAYMENTID));
        LOG.info("ResponseDTO created: " + ToStringBuilder.reflectionToString(responseDTO, ToStringStyle.MULTI_LINE_STYLE));
        return responseDTO;
    }

}
