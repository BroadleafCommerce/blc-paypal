package org.broadleafcommerce.payment.service.gateway;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.vendor.paypal.api.AgreementToken;
import org.broadleafcommerce.vendor.paypal.service.PayPalAgreementTokenService;
import org.broadleafcommerce.vendor.paypal.service.PayPalPaymentService;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCheckoutPaymentGatewayType;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalExecuteAgreementTokenRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalExecuteAgreementTokenResponse;
import org.springframework.stereotype.Service;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.domain.PaymentResponse;
import com.broadleafcommerce.paymentgateway.domain.enums.DefaultTransactionTypes;
import com.broadleafcommerce.paymentgateway.domain.enums.PaymentType;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.broadleafcommerce.paymentgateway.service.reporting.PaymentGatewayReportingService;
import com.broadleafcommerce.paymentgateway.service.webresponse.AbstractPaymentGatewayWebResponseService;
import com.broadleafcommerce.paymentgateway.service.webresponse.PaymentGatewayWebResponsePrintService;
import com.broadleafcommerce.paymentgateway.service.webresponse.PaymentGatewayWebResponseService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalCheckoutWebResponseService")
public class PayPalCheckoutWebResponseServiceImpl extends AbstractPaymentGatewayWebResponseService
        implements PaymentGatewayWebResponseService {

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
    public PaymentResponse translateWebResponse(HttpServletRequest request)
            throws PaymentException {
        boolean completeCheckout = false;
        if (request.getParameter(MessageConstants.CHECKOUT_COMPLETE) != null) {
            completeCheckout =
                    Boolean.valueOf(request.getParameter(MessageConstants.CHECKOUT_COMPLETE));
        }

        String paymentId = request.getParameter(MessageConstants.HTTP_PAYMENTID);
        String payerId = request.getParameter(MessageConstants.HTTP_PAYERID);
        String token = request.getParameter(MessageConstants.HTTP_TOKEN);
        String billingToken = request.getParameter(MessageConstants.HTTP_BILLINGTOKEN);

        // 1. Handle Reference Transactions - Request that contain a "Billing Agreement Token" in
        // the callback
        if (StringUtils.isNotBlank(billingToken)) {
            String billingAgreementId =
                    agreementTokenService.getPayPalBillingAgreementIdFromCurrentOrder();
            PaymentRequest paymentRequest =
                    agreementTokenService.getPaymentRequestForCurrentOrder();

            if (StringUtils.isBlank(billingAgreementId)) {
                AgreementToken agreementToken = new AgreementToken(billingToken);
                agreementToken = executeAgreementToken(agreementToken, paymentRequest);

                PaymentResponse paymentResponse =
                        new PaymentResponse(PaymentType.THIRD_PARTY_ACCOUNT,
                                PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT)
                                        .transactionType(DefaultTransactionTypes.UNCONFIRMED);
                externalCallService.setCommonDetailsResponse(agreementToken, paymentResponse,
                        paymentRequest, completeCheckout);
                paymentResponse.responseMap(MessageConstants.BILLINGAGREEMENTID,
                        agreementToken.getId());

                LOG.info("ResponseDTO created: " + ToStringBuilder
                        .reflectionToString(paymentResponse, ToStringStyle.MULTI_LINE_STYLE));

                agreementTokenService.setPayPalAgreementTokenOnCurrentOrder(billingToken);
                agreementTokenService
                        .setPayPalBillingAgreementIdOnCurrentOrder(agreementToken.getId());

                return paymentResponse;
            } else {
                PaymentResponse responseDTO = new PaymentResponse(PaymentType.THIRD_PARTY_ACCOUNT,
                        PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT)
                                .transactionType(DefaultTransactionTypes.UNCONFIRMED);
                externalCallService.setCommonDetailsResponse(null, responseDTO, paymentRequest,
                        completeCheckout);
                responseDTO.responseMap(MessageConstants.BILLINGAGREEMENTID, billingAgreementId);

                LOG.info("ResponseDTO created: " + ToStringBuilder.reflectionToString(responseDTO,
                        ToStringStyle.MULTI_LINE_STYLE));

                return responseDTO;
            }

        }

        // 2. Handle Billing Agreement Approvals - Request that contain an EC "token" in the
        // callback
        if (StringUtils.isNotBlank(token)) {
            throw new UnsupportedOperationException(
                    "Billing Agreements and Recurring Subscriptions " +
                            "created via the Payments API is not yet supported.");
        }

        // 3. Finally (if not a billing agreement flow), handle payments
        PaymentRequest requestDTO = new PaymentRequest()
                .additionalField(MessageConstants.HTTP_PAYMENTID, paymentId)
                .additionalField(MessageConstants.HTTP_PAYERID, payerId);
        PaymentResponse responseDTO = reportingService.findDetailsByTransaction(requestDTO);

        responseDTO
                .responseMap(MessageConstants.HTTP_REQUEST,
                        webResponsePrintService.printRequest(request))
                .transactionType(DefaultTransactionTypes.UNCONFIRMED);

        paymentService.setPayPalPaymentIdOnCurrentOrder(paymentId);
        paymentService.setPayPalPayerIdOnCurrentOrder(payerId);
        return responseDTO;
    }

    protected AgreementToken executeAgreementToken(AgreementToken agreementToken,
            PaymentRequest requestDTO) throws PaymentException {
        PayPalExecuteAgreementTokenResponse response =
                (PayPalExecuteAgreementTokenResponse) externalCallService.call(
                        new PayPalExecuteAgreementTokenRequest(agreementToken,
                                externalCallService.constructAPIContext(requestDTO)));
        return response.getAgreementToken();
    }

}
