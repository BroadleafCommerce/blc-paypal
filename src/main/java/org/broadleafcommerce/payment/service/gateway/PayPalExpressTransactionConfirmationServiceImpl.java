/*
 * Copyright 2008-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.payment.service.gateway;

import org.broadleafcommerce.common.payment.PaymentTransactionType;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.PaymentGatewayTransactionConfirmationService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayTransactionService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalExpressTransactionConfirmationService")
public class PayPalExpressTransactionConfirmationServiceImpl implements PaymentGatewayTransactionConfirmationService {

    @Resource(name = "blPayPalExpressConfiguration")
    protected PayPalExpressConfiguration configuration;

    @Resource(name = "blPayPalExpressTransactionService")
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
