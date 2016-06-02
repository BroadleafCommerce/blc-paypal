/*
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2014 Broadleaf Commerce
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.AbstractPaymentGatewayHostedService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayHostedService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalTransactionType;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalExpressHostedService")
public class PayPalExpressHostedServiceImpl extends AbstractPaymentGatewayHostedService implements PaymentGatewayHostedService {

    protected static final Log LOG = LogFactory.getLog(PayPalExpressHostedServiceImpl.class);

    @Resource(name = "blExternalCallPayPalExpressService")
    protected ExternalCallPayPalExpressService payPalExpressService;

    @Override
    public PaymentResponseDTO requestHostedEndpoint(PaymentRequestDTO paymentRequestDTO) throws PaymentException {

        PayPalTransactionType transactionType = PayPalTransactionType.AUTHORIZEANDCAPTURE;
        if (!payPalExpressService.getConfiguration().isPerformAuthorizeAndCapture()) {
            transactionType = PayPalTransactionType.AUTHORIZE;
        }

        PaymentResponseDTO responseDTO = payPalExpressService.commonAuthorizeOrSale(paymentRequestDTO, transactionType, null, null);

        if (LOG.isTraceEnabled()) {
            LOG.trace("Request to PayPal Express Checkout Hosted Endpoint with raw response: " +
                responseDTO.getRawResponse());
        }

        return responseDTO;

    }

}
