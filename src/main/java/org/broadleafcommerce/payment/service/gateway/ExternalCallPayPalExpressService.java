/*
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.broadleafcommerce.payment.service.gateway;

import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.message.details.PayPalDetailsResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalPaymentRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalPaymentResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalTransactionType;

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

    void setCommonDetailsResponse(PayPalDetailsResponse response, PaymentResponseDTO responseDTO);

    void setDecisionInformation(PayPalPaymentResponse response, PaymentResponseDTO responseDTO);

    void setRefundInformation(PayPalPaymentResponse response, PaymentResponseDTO responseDTO);
}
