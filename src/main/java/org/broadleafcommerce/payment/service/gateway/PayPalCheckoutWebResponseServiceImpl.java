/*-
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2022 Broadleaf Commerce
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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.payment.PaymentTransactionType;
import org.broadleafcommerce.common.payment.PaymentType;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.AbstractPaymentGatewayWebResponseService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayReportingService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayWebResponsePrintService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayWebResponseService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.vendor.paypal.api.AgreementToken;
import org.broadleafcommerce.vendor.paypal.service.PayPalAgreementTokenService;
import org.broadleafcommerce.vendor.paypal.service.PayPalPaymentService;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCheckoutPaymentGatewayType;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalExecuteAgreementTokenRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalExecuteAgreementTokenResponse;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalCheckoutWebResponseService")
public class PayPalCheckoutWebResponseServiceImpl extends AbstractPaymentGatewayWebResponseService implements PaymentGatewayWebResponseService {

    private static final Log LOG = LogFactory.getLog(PayPalCheckoutWebResponseServiceImpl.class);

    @Resource(name = "blExternalCallPayPalCheckoutService")
    protected ExternalCallPayPalCheckoutService externalCallService;

    @Resource(name = "blPaymentGatewayWebResponsePrintService")
    protected PaymentGatewayWebResponsePrintService webResponsePrintService;

    @Resource(name = "blPayPalCheckoutReportingService")
    protected PaymentGatewayReportingService reportingService;

    @Resource(name = "blPayPalPaymentService")
    protected PayPalPaymentService paymentService;

    @Resource(name = "blPayPalAgreementTokenService")
    protected PayPalAgreementTokenService agreementTokenService;

    @Override
    public PaymentResponseDTO translateWebResponse(HttpServletRequest request) throws PaymentException {
        boolean completeCheckout = false;
        if (request.getParameter(MessageConstants.CHECKOUT_COMPLETE) != null) {
            completeCheckout = Boolean.valueOf(request.getParameter(MessageConstants.CHECKOUT_COMPLETE));
        }

        String paymentId = request.getParameter(MessageConstants.HTTP_PAYMENTID);
        String payerId = request.getParameter(MessageConstants.HTTP_PAYERID);
        String token = request.getParameter(MessageConstants.HTTP_TOKEN);
        String billingToken = request.getParameter(MessageConstants.HTTP_BILLINGTOKEN);

        // 1. Handle Reference Transactions - Request that contain a "Billing Agreement Token" in the callback
        if (StringUtils.isNotBlank(billingToken)) {
            String billingAgreementId = agreementTokenService.getPayPalBillingAgreementIdFromCurrentOrder();
            PaymentRequestDTO requestDTO = agreementTokenService.getPaymentRequestForCurrentOrder();

            if (StringUtils.isBlank(billingAgreementId)) {
                AgreementToken agreementToken = new AgreementToken(billingToken);
                agreementToken = executeAgreementToken(agreementToken, requestDTO);

                PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT,
                        PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT)
                        .paymentTransactionType(PaymentTransactionType.UNCONFIRMED);
                externalCallService.setCommonDetailsResponse(agreementToken, responseDTO, new Money(requestDTO.getTransactionTotal()),
                        requestDTO.getOrderId(), completeCheckout);
                responseDTO.responseMap(MessageConstants.BILLINGAGREEMENTID, agreementToken.getId());

                LOG.info("ResponseDTO created: " + ToStringBuilder.reflectionToString(responseDTO, ToStringStyle.MULTI_LINE_STYLE));

                agreementTokenService.setPayPalAgreementTokenOnCurrentOrder(billingToken);
                agreementTokenService.setPayPalBillingAgreementIdOnCurrentOrder(agreementToken.getId());

                return responseDTO;
            } else {
                PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT,
                        PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT)
                        .paymentTransactionType(PaymentTransactionType.UNCONFIRMED);
                externalCallService.setCommonDetailsResponse(null, responseDTO, new Money(requestDTO.getTransactionTotal()),
                        requestDTO.getOrderId(), completeCheckout);
                responseDTO.responseMap(MessageConstants.BILLINGAGREEMENTID, billingAgreementId);

                LOG.info("ResponseDTO created: " + ToStringBuilder.reflectionToString(responseDTO, ToStringStyle.MULTI_LINE_STYLE));

                return responseDTO;
            }

        }

        // 2. Handle Billing Agreement Approvals - Request that contain an EC "token" in the callback
        if (StringUtils.isNotBlank(token)) {
            throw new UnsupportedOperationException("Billing Agreements and Recurring Subscriptions " +
                    "created via the Payments API is not yet supported.");
        }

        // 3. Finally (if not a billing agreement flow), handle payments
        PaymentRequestDTO requestDTO = new PaymentRequestDTO()
                .additionalField(MessageConstants.HTTP_PAYMENTID, paymentId)
                .additionalField(MessageConstants.HTTP_PAYERID, payerId);
        PaymentResponseDTO responseDTO = reportingService.findDetailsByTransaction(requestDTO);

        responseDTO.responseMap(MessageConstants.HTTP_REQUEST, webResponsePrintService.printRequest(request))
                .paymentTransactionType(PaymentTransactionType.UNCONFIRMED);

        paymentService.setPayPalPaymentIdOnCurrentOrder(paymentId);
        paymentService.setPayPalPayerIdOnCurrentOrder(payerId);
        return responseDTO;
    }

    protected AgreementToken executeAgreementToken(AgreementToken agreementToken, PaymentRequestDTO requestDTO) throws PaymentException {
        PayPalExecuteAgreementTokenResponse response = (PayPalExecuteAgreementTokenResponse) externalCallService.call(
                new PayPalExecuteAgreementTokenRequest(agreementToken, externalCallService.constructAPIContext(requestDTO)));
        return response.getAgreementToken();
    }

}
