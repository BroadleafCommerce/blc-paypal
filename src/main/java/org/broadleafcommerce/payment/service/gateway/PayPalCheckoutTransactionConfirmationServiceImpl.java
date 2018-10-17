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

import org.broadleafcommerce.common.payment.PaymentTransactionType;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.AbstractPaymentGatewayTransactionConfirmationService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayTransactionConfirmationService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayTransactionService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalCheckoutTransactionConfirmationService")
public class PayPalCheckoutTransactionConfirmationServiceImpl extends AbstractPaymentGatewayTransactionConfirmationService implements PaymentGatewayTransactionConfirmationService {

    @Resource(name = "blPayPalCheckoutConfiguration")
    protected PayPalCheckoutConfiguration configuration;

    @Resource(name = "blPayPalCheckoutTransactionService")
    protected PaymentGatewayTransactionService transactionService;

    @Override
    public PaymentResponseDTO confirmTransaction(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        PaymentResponseDTO responseDTO = null;
        if (configuration.isPerformAuthorizeAndCapture()){
            responseDTO = transactionService.authorizeAndCapture(paymentRequestDTO);
            responseDTO.paymentTransactionType(PaymentTransactionType.AUTHORIZE_AND_CAPTURE);
        } else {
            responseDTO =  transactionService.authorize(paymentRequestDTO);
            responseDTO.paymentTransactionType(PaymentTransactionType.AUTHORIZE);
        }

        return responseDTO;

    }

}
