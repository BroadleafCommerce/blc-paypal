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
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.payment.PaymentTransactionType;
import org.broadleafcommerce.common.payment.PaymentType;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.PaymentGatewayTransactionService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalExpressPaymentGatewayType;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalPaymentRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalPaymentResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.message.payment.PayPalSummaryRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalMethodType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalRefundType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalTransactionType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalExpressTransactionService")
public class PayPalExpressTransactionServiceImpl extends AbstractPayPalExpressService implements PaymentGatewayTransactionService {

    protected static final Log LOG = LogFactory.getLog(PayPalExpressTransactionServiceImpl.class);
    
    @Override
    public String getServiceName() {
        return getClass().getName();
    }

    @Override
    public PaymentResponseDTO authorize(PaymentRequestDTO paymentRequestDTO) throws PaymentException  {
        Assert.isTrue(paymentRequestDTO.getAdditionalFields().containsKey(MessageConstants.PAYERID), "The RequestDTO must contain a PAYERID");
        Assert.isTrue(paymentRequestDTO.getAdditionalFields().containsKey(MessageConstants.TOKEN), "The RequestDTO must contain a TOKEN");

        return commonAuthorizeOrSale(paymentRequestDTO, PayPalTransactionType.AUTHORIZE,
                (String)paymentRequestDTO.getAdditionalFields().get(MessageConstants.TOKEN),
                (String)paymentRequestDTO.getAdditionalFields().get(MessageConstants.PAYERID));
    }

    @Override
    public PaymentResponseDTO capture(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        Assert.isTrue(paymentRequestDTO.getAdditionalFields().containsKey(MessageConstants.TRANSACTIONID), "The RequestDTO must contain a TRANSACTIONID");

        PayPalPaymentRequest request = buildBasicRequest(paymentRequestDTO, PayPalTransactionType.CAPTURE);
        request.setMethodType(PayPalMethodType.CAPTURE);
        request.setTransactionID((String)paymentRequestDTO.getAdditionalFields().get(MessageConstants.TRANSACTIONID));

        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT,
                PayPalExpressPaymentGatewayType.PAYPAL_EXPRESS);
        PayPalPaymentResponse response;

        response = (PayPalPaymentResponse) process(request);
        setCommonPaymentResponse(response, responseDTO);
        responseDTO.successful(response.isSuccessful());
        responseDTO.paymentTransactionType(PaymentTransactionType.CAPTURE);
        setDecisionInformation(response, responseDTO);

        return responseDTO;
    }

    @Override
    public PaymentResponseDTO authorizeAndCapture(PaymentRequestDTO paymentRequestDTO) throws PaymentException  {
        Assert.isTrue(paymentRequestDTO.getAdditionalFields().containsKey(MessageConstants.PAYERID), "The RequestDTO must contain a PAYERID");
        Assert.isTrue(paymentRequestDTO.getAdditionalFields().containsKey(MessageConstants.TOKEN), "The RequestDTO must contain a TOKEN");

        return commonAuthorizeOrSale(paymentRequestDTO, PayPalTransactionType.AUTHORIZEANDCAPTURE,
                (String)paymentRequestDTO.getAdditionalFields().get(MessageConstants.TOKEN),
                (String)paymentRequestDTO.getAdditionalFields().get(MessageConstants.PAYERID));
    }

    @Override
    public PaymentResponseDTO reverseAuthorize(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        Assert.isTrue(paymentRequestDTO.getAdditionalFields().containsKey(MessageConstants.TRANSACTIONID), "The RequestDTO must contain a TRANSACTIONID");

        PayPalPaymentRequest request = buildBasicRequest(paymentRequestDTO, PayPalTransactionType.REVERSEAUTHORIZE);
        request.setMethodType(PayPalMethodType.VOID);
        request.setTransactionID((String) paymentRequestDTO.getAdditionalFields().get(MessageConstants.TRANSACTIONID));

        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT,
                PayPalExpressPaymentGatewayType.PAYPAL_EXPRESS);
        PayPalPaymentResponse response;

        response = (PayPalPaymentResponse) process(request);
        setCommonPaymentResponse(response, responseDTO);
        responseDTO.successful(response.isSuccessful());
        responseDTO.paymentTransactionType(PaymentTransactionType.REVERSE_AUTH);
        setDecisionInformation(response, responseDTO);

        return responseDTO;
    }

    @Override
    public PaymentResponseDTO refund(PaymentRequestDTO paymentRequestDTO) throws PaymentException  {
        Assert.isTrue(paymentRequestDTO.getAdditionalFields().containsKey(MessageConstants.TRANSACTIONID), "The RequestDTO must contain a TRANSACTIONID");

        PayPalPaymentRequest request = buildBasicRequest(paymentRequestDTO, PayPalTransactionType.CREDIT);
        request.setMethodType(PayPalMethodType.REFUND);
        if (paymentRequestDTO.getAdditionalFields().containsKey(MessageConstants.REFUNDTYPE)) {
            request.setRefundType(PayPalRefundType.getInstance((String) paymentRequestDTO.getAdditionalFields().get(MessageConstants.REFUNDTYPE)));
        } else {
            LOG.debug("No refund type passed in, assuming partial refund with requested amount of " + paymentRequestDTO.getTransactionTotal());
            request.setRefundType(PayPalRefundType.PARTIAL);
        }
        
        if (PayPalRefundType.PARTIAL.equals(request.getRefundType())) {
            PayPalSummaryRequest summary = new PayPalSummaryRequest();
            summary.setGrandTotal(new Money(paymentRequestDTO.getTransactionTotal()));
            request.setSummaryRequest(summary);
        }
        
        request.setTransactionID((String)paymentRequestDTO.getAdditionalFields().get(MessageConstants.TRANSACTIONID));

        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT,
                PayPalExpressPaymentGatewayType.PAYPAL_EXPRESS);
        PayPalPaymentResponse response;

        response = (PayPalPaymentResponse) process(request);
        setCommonPaymentResponse(response, responseDTO);
        responseDTO.successful(response.isSuccessful());
        responseDTO.paymentTransactionType(PaymentTransactionType.REFUND);
        setDecisionInformation(response, responseDTO);
        setRefundInformation(response, responseDTO);

        return responseDTO;
    }

    @Override
    public PaymentResponseDTO voidPayment(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        Assert.isTrue(paymentRequestDTO.getAdditionalFields().containsKey(MessageConstants.TRANSACTIONID), "The RequestDTO must contain a TRANSACTIONID");

        PayPalPaymentRequest request = buildBasicRequest(paymentRequestDTO, PayPalTransactionType.VOIDTRANSACTION);
        request.setMethodType(PayPalMethodType.VOID);
        request.setTransactionID((String) paymentRequestDTO.getAdditionalFields().get(MessageConstants.TRANSACTIONID));

        PayPalPaymentResponse response = (PayPalPaymentResponse) process(request);
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT,
                PayPalExpressPaymentGatewayType.PAYPAL_EXPRESS);
        setCommonPaymentResponse(response, responseDTO);
        responseDTO.successful(response.isSuccessful());
        responseDTO.paymentTransactionType(PaymentTransactionType.VOID);
        setDecisionInformation(response, responseDTO);

        return responseDTO;
    }

}
