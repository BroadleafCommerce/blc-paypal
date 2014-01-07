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

import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.PaymentGatewayHostedService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalTransactionType;
import org.springframework.stereotype.Service;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalExpressHostedService")
public class PayPalExpressHostedServiceImpl extends AbstractPayPalExpressService implements PaymentGatewayHostedService {

    @Override
    public String getServiceName() {
        return getClass().getName();
    }

    @Override
    public PaymentResponseDTO requestHostedEndpoint(PaymentRequestDTO paymentRequestDTO) throws PaymentException {

        PayPalTransactionType transactionType = PayPalTransactionType.AUTHORIZEANDCAPTURE;
        if (!configuration.isPerformAuthorizeAndCapture()) {
            transactionType = PayPalTransactionType.AUTHORIZE;
        }

        return commonAuthorizeOrSale(paymentRequestDTO, transactionType, null, null);

    }

}
