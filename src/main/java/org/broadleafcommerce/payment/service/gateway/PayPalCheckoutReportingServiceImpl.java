package org.broadleafcommerce.payment.service.gateway;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCheckoutPaymentGatewayType;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalPaymentRetrievalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalPaymentRetrievalResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.domain.PaymentResponse;
import com.broadleafcommerce.paymentgateway.domain.enums.PaymentType;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.broadleafcommerce.paymentgateway.service.reporting.AbstractPaymentGatewayReportingService;
import com.broadleafcommerce.paymentgateway.service.reporting.PaymentGatewayReportingService;
import com.paypal.api.payments.Payment;

import javax.annotation.Resource;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalCheckoutReportingService")
public class PayPalCheckoutReportingServiceImpl extends AbstractPaymentGatewayReportingService
        implements PaymentGatewayReportingService {

    private static final Log LOG = LogFactory.getLog(PayPalCheckoutReportingServiceImpl.class);

    @Resource(name = "blExternalCallPayPalCheckoutService")
    protected ExternalCallPayPalCheckoutService payPalCheckoutService;

    @Override
    public PaymentResponse findDetailsByTransaction(PaymentRequest paymentRequest)
            throws PaymentException {
        Assert.isTrue(
                paymentRequest.getAdditionalFields().containsKey(MessageConstants.HTTP_PAYERID),
                "The RequestDTO must contain a payerID");
        Assert.isTrue(
                paymentRequest.getAdditionalFields().containsKey(MessageConstants.HTTP_PAYMENTID),
                "The RequestDTO must contain a paymentID");

        PayPalPaymentRetrievalResponse response =
                (PayPalPaymentRetrievalResponse) payPalCheckoutService.call(
                        new PayPalPaymentRetrievalRequest(
                                (String) paymentRequest.getAdditionalFields()
                                        .get(MessageConstants.HTTP_PAYMENTID),
                                payPalCheckoutService.constructAPIContext(paymentRequest)));
        Payment payment = response.getPayment();
        PaymentResponse responseDTO = new PaymentResponse(PaymentType.THIRD_PARTY_ACCOUNT,
                PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT);
        payPalCheckoutService.setCommonDetailsResponse(payment, responseDTO);
        responseDTO
                .responseMap(MessageConstants.PAYERID,
                        (String) paymentRequest.getAdditionalFields()
                                .get(MessageConstants.HTTP_PAYERID))
                .responseMap(MessageConstants.PAYMENTID, (String) paymentRequest
                        .getAdditionalFields().get(MessageConstants.HTTP_PAYMENTID));
        LOG.info("ResponseDTO created: "
                + ToStringBuilder.reflectionToString(responseDTO, ToStringStyle.MULTI_LINE_STYLE));
        return responseDTO;
    }

}
