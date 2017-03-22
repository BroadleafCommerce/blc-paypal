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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.payment.PaymentTransactionType;
import org.broadleafcommerce.common.payment.PaymentType;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.AbstractPaymentGatewayTransactionService;
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
import javax.annotation.Resource;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalExpressTransactionService")
public class PayPalExpressTransactionServiceImpl extends AbstractPaymentGatewayTransactionService implements PaymentGatewayTransactionService {

    protected static final Log LOG = LogFactory.getLog(PayPalExpressTransactionServiceImpl.class);

    @Resource(name = "blExternalCallPayPalExpressService")
    protected ExternalCallPayPalExpressService payPalExpressService;


    @Override
    public PaymentResponseDTO authorize(PaymentRequestDTO paymentRequestDTO) throws PaymentException  {
        Assert.isTrue(paymentRequestDTO.getAdditionalFields().containsKey(MessageConstants.PAYERID), "The RequestDTO must contain a PAYERID");
        Assert.isTrue(paymentRequestDTO.getAdditionalFields().containsKey(MessageConstants.TOKEN), "The RequestDTO must contain a TOKEN");

        return payPalExpressService.commonAuthorizeOrSale(paymentRequestDTO, PayPalTransactionType.AUTHORIZE,
                (String) paymentRequestDTO.getAdditionalFields().get(MessageConstants.TOKEN),
                (String) paymentRequestDTO.getAdditionalFields().get(MessageConstants.PAYERID));
    }

    @Override
    public PaymentResponseDTO capture(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        Assert.isTrue(paymentRequestDTO.getAdditionalFields().containsKey(MessageConstants.TRANSACTIONID), "The RequestDTO must contain a TRANSACTIONID");

        PayPalPaymentRequest request = payPalExpressService.buildBasicRequest(paymentRequestDTO, PayPalTransactionType.CAPTURE);
        request.setMethodType(PayPalMethodType.CAPTURE);
        request.setTransactionID((String)paymentRequestDTO.getAdditionalFields().get(MessageConstants.TRANSACTIONID));

        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT,
                PayPalExpressPaymentGatewayType.PAYPAL_EXPRESS);
        PayPalPaymentResponse response;

        response = (PayPalPaymentResponse) payPalExpressService.call(request);
        payPalExpressService.setCommonPaymentResponse(response, responseDTO);
        responseDTO.successful(response.isSuccessful());
        responseDTO.paymentTransactionType(PaymentTransactionType.CAPTURE);
        payPalExpressService.setDecisionInformation(response, responseDTO);

        return responseDTO;
    }

    @Override
    public PaymentResponseDTO authorizeAndCapture(PaymentRequestDTO paymentRequestDTO) throws PaymentException  {
        Assert.isTrue(paymentRequestDTO.getAdditionalFields().containsKey(MessageConstants.PAYERID), "The RequestDTO must contain a PAYERID");
        Assert.isTrue(paymentRequestDTO.getAdditionalFields().containsKey(MessageConstants.TOKEN), "The RequestDTO must contain a TOKEN");

        return payPalExpressService.commonAuthorizeOrSale(paymentRequestDTO, PayPalTransactionType.AUTHORIZEANDCAPTURE,
                (String) paymentRequestDTO.getAdditionalFields().get(MessageConstants.TOKEN),
                (String) paymentRequestDTO.getAdditionalFields().get(MessageConstants.PAYERID));
    }

    @Override
    public PaymentResponseDTO reverseAuthorize(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        Assert.isTrue(paymentRequestDTO.getAdditionalFields().containsKey(MessageConstants.TRANSACTIONID), "The RequestDTO must contain a TRANSACTIONID");

        PayPalPaymentRequest request = payPalExpressService.buildBasicRequest(paymentRequestDTO, PayPalTransactionType.REVERSEAUTHORIZE);
        request.setMethodType(PayPalMethodType.VOID);
        request.setTransactionID((String) paymentRequestDTO.getAdditionalFields().get(MessageConstants.TRANSACTIONID));

        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT,
                PayPalExpressPaymentGatewayType.PAYPAL_EXPRESS);
        PayPalPaymentResponse response;

        response = (PayPalPaymentResponse) payPalExpressService.call(request);
        payPalExpressService.setCommonPaymentResponse(response, responseDTO);
        responseDTO.successful(response.isSuccessful());
        responseDTO.paymentTransactionType(PaymentTransactionType.REVERSE_AUTH);
        payPalExpressService.setDecisionInformation(response, responseDTO);

        return responseDTO;
    }

    @Override
    public PaymentResponseDTO refund(PaymentRequestDTO paymentRequestDTO) throws PaymentException  {
        Assert.isTrue(paymentRequestDTO.getAdditionalFields().containsKey(MessageConstants.TRANSACTIONID), "The RequestDTO must contain a TRANSACTIONID");

        PayPalPaymentRequest request = payPalExpressService.buildBasicRequest(paymentRequestDTO, PayPalTransactionType.CREDIT);
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

        response = (PayPalPaymentResponse) payPalExpressService.call(request);
        payPalExpressService.setCommonPaymentResponse(response, responseDTO);
        responseDTO.successful(response.isSuccessful());
        responseDTO.paymentTransactionType(PaymentTransactionType.REFUND);
        payPalExpressService.setDecisionInformation(response, responseDTO);
        payPalExpressService.setRefundInformation(response, responseDTO);

        return responseDTO;
    }

    @Override
    public PaymentResponseDTO voidPayment(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        Assert.isTrue(paymentRequestDTO.getAdditionalFields().containsKey(MessageConstants.TRANSACTIONID), "The RequestDTO must contain a TRANSACTIONID");

        PayPalPaymentRequest request = payPalExpressService.buildBasicRequest(paymentRequestDTO, PayPalTransactionType.VOIDTRANSACTION);
        request.setMethodType(PayPalMethodType.VOID);
        request.setTransactionID((String) paymentRequestDTO.getAdditionalFields().get(MessageConstants.TRANSACTIONID));

        PayPalPaymentResponse response = (PayPalPaymentResponse) payPalExpressService.call(request);
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT,
                PayPalExpressPaymentGatewayType.PAYPAL_EXPRESS);
        payPalExpressService.setCommonPaymentResponse(response, responseDTO);
        responseDTO.successful(response.isSuccessful());
        responseDTO.paymentTransactionType(PaymentTransactionType.VOID);
        payPalExpressService.setDecisionInformation(response, responseDTO);

        return responseDTO;
    }

}
