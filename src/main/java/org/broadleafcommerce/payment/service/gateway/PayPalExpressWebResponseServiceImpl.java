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

import org.broadleafcommerce.common.payment.PaymentTransactionType;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.AbstractPaymentGatewayWebResponseService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayReportingService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayWebResponsePrintService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayWebResponseService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalExpressWebResponseService")
public class PayPalExpressWebResponseServiceImpl extends AbstractPaymentGatewayWebResponseService implements PaymentGatewayWebResponseService {

    @Resource(name = "blPaymentGatewayWebResponsePrintService")
    protected PaymentGatewayWebResponsePrintService webResponsePrintService;

    @Resource(name = "blPayPalExpressReportingService")
    protected PaymentGatewayReportingService reportingService;

    @Override
    public PaymentResponseDTO translateWebResponse(HttpServletRequest request) throws PaymentException {
        PaymentRequestDTO requestDTO = new PaymentRequestDTO()
                .additionalField(MessageConstants.TOKEN, request.getParameter(MessageConstants.HTTP_TOKEN));
        PaymentResponseDTO responseDTO = reportingService.findDetailsByTransaction(requestDTO);

        responseDTO.responseMap(MessageConstants.HTTP_REQUEST, webResponsePrintService.printRequest(request))
                .paymentTransactionType(PaymentTransactionType.UNCONFIRMED);

        return responseDTO;
    }

}
