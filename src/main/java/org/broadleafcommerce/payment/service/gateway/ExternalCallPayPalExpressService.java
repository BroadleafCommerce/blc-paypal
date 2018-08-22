/*
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
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

import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalPaymentRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalPaymentResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalTransactionType;

import com.paypal.api.payments.Payment;

/**
 * @author Elbert Bautista (elbertbautista)
 */
public interface ExternalCallPayPalExpressService {

    PayPalExpressConfiguration getConfiguration();

    PayPalResponse call(PayPalRequest paymentRequest) throws PaymentException;

    PayPalPaymentRequest buildBasicRequest(PaymentRequestDTO requestDTO, PayPalTransactionType transactionType);

    PaymentResponseDTO commonAuthorizeOrSale(PaymentRequestDTO requestDTO, PayPalTransactionType transactionType,
                                             String token, String payerId) throws PaymentException;

    void setCommonPaymentResponse(PayPalPaymentResponse response, PaymentResponseDTO responseDTO);

    void setCommonDetailsResponse(Payment response, PaymentResponseDTO responseDTO);

    void setDecisionInformation(PayPalPaymentResponse response, PaymentResponseDTO responseDTO);

    void setRefundInformation(PayPalPaymentResponse response, PaymentResponseDTO responseDTO);
}
