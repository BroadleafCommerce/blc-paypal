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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.payment.PaymentTransactionType;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.AbstractPaymentGatewayWebResponseService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayReportingService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayWebResponsePrintService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayWebResponseService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.vendor.paypal.service.PayPalPaymentService;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalCheckoutWebResponseService")
public class PayPalCheckoutWebResponseServiceImpl extends AbstractPaymentGatewayWebResponseService implements PaymentGatewayWebResponseService {

    private static final Log LOG = LogFactory.getLog(PayPalCheckoutWebResponseServiceImpl.class);

    @Resource(name = "blExternalCallPayPalCheckoutService")
    protected ExternalCallPayPalCheckoutService externalCallService;

    @Resource(name = "blPaymentGatewayWebResponsePrintService")
    protected PaymentGatewayWebResponsePrintService webResponsePrintService;

    @Resource(name = "blPayPalCheckoutReportingService")
    protected PaymentGatewayReportingService reportingService;

    @Resource(name = "blPayPalPaymentService")
    protected PayPalPaymentService paymentService;

    @Override
    public PaymentResponseDTO translateWebResponse(HttpServletRequest request) throws PaymentException {
        String orderId = request.getParameter(MessageConstants.HTTP_ORDER_ID);
        String payerId = request.getParameter(MessageConstants.HTTP_PAYERID);

        PaymentRequestDTO requestDTO = paymentService.getPaymentRequestForCurrentOrder();
        requestDTO.additionalField(MessageConstants.HTTP_ORDER_ID, orderId)
                .additionalField(MessageConstants.HTTP_PAYERID, payerId);
        PaymentResponseDTO responseDTO = reportingService.findDetailsByTransaction(requestDTO);

        responseDTO.responseMap(MessageConstants.HTTP_REQUEST, webResponsePrintService.printRequest(request))
                .paymentTransactionType(PaymentTransactionType.UNCONFIRMED);

        paymentService.setPayPalOrderIdOnCurrentOrder(orderId);
        paymentService.setPayPalPayerIdOnCurrentOrder(payerId);
        return responseDTO;
    }
}
