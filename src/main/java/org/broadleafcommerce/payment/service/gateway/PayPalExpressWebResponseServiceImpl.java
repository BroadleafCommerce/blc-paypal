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

import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.PaymentGatewayWebResponsePrintService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayWebResponseService;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalExpressWebResponseService")
public class PayPalExpressWebResponseServiceImpl implements PaymentGatewayWebResponseService {

    @Resource(name = "blPaymentGatewayWebResponsePrintService")
    protected PaymentGatewayWebResponsePrintService webResponsePrintService;

    @Override
    public PaymentResponseDTO translateWebResponse(HttpServletRequest request) {
        PaymentResponseDTO responseDTO = new PaymentResponseDTO();
        responseDTO.setRawResponse(webResponsePrintService.printRequest(request));
        responseDTO.getResponseMap().put(MessageConstants.HTTP_PAYERID, request.getParameter(MessageConstants.HTTP_PAYERID));
        responseDTO.getResponseMap().put(MessageConstants.HTTP_TOKEN, request.getParameter(MessageConstants.HTTP_TOKEN));
        return responseDTO;
    }

}
