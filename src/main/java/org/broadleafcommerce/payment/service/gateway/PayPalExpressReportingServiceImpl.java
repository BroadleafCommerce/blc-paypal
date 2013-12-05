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
import org.broadleafcommerce.common.payment.service.PaymentGatewayReportingService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.message.details.PayPalDetailsRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.details.PayPalDetailsResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalMethodType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalExpressReportingService")
public class PayPalExpressReportingServiceImpl extends AbstractPayPalExpressService implements PaymentGatewayReportingService {

    @Override
    public PaymentResponseDTO findDetailsByTransaction(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        Assert.isTrue(paymentRequestDTO.getAdditionalFields().containsKey(MessageConstants.TOKEN), "The RequestDTO must contain a TOKEN");

        PayPalDetailsRequest detailsRequest = new PayPalDetailsRequest();
        detailsRequest.setMethodType(PayPalMethodType.DETAILS);
        detailsRequest.setToken((String)paymentRequestDTO.getAdditionalFields().get(MessageConstants.TOKEN));

        PaymentResponseDTO responseDTO = new PaymentResponseDTO();
        PayPalDetailsResponse response;

        response = (PayPalDetailsResponse) process(detailsRequest);
        setRawResponse(response, responseDTO);
        return responseDTO;
    }

    @Override
    public String getServiceName() {
        return getClass().getName();
    }
}
