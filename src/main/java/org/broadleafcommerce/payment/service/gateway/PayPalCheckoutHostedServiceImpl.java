package org.broadleafcommerce.payment.service.gateway;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.domain.PaymentResponse;
import com.broadleafcommerce.paymentgateway.domain.enums.DefaultTransactionTypes;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.broadleafcommerce.paymentgateway.service.hosted.AbstractPaymentGatewayHostedService;
import com.broadleafcommerce.paymentgateway.service.hosted.PaymentGatewayHostedService;
import com.broadleafcommerce.paymentgateway.service.transaction.PaymentGatewayTransactionService;

import javax.annotation.Resource;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalCheckoutHostedService")
public class PayPalCheckoutHostedServiceImpl extends AbstractPaymentGatewayHostedService
        implements PaymentGatewayHostedService {

    protected static final Log LOG = LogFactory.getLog(PayPalCheckoutHostedServiceImpl.class);

    @Resource(name = "blExternalCallPayPalCheckoutService")
    protected ExternalCallPayPalCheckoutService payPalCheckoutService;

    @Resource(name = "blPayPalCheckoutTransactionService")
    protected PaymentGatewayTransactionService transactionService;

    @Override
    public PaymentResponse requestHostedEndpoint(PaymentRequest paymentRequest)
            throws PaymentException {

        PaymentResponse responseDTO;
        if (payPalCheckoutService.getConfiguration().isPerformAuthorizeAndCapture()) {
            responseDTO = transactionService.authorizeAndCapture(paymentRequest);
            responseDTO.transactionType(DefaultTransactionTypes.AUTHORIZE_AND_CAPTURE);
        } else {
            responseDTO = transactionService.authorize(paymentRequest);
            responseDTO.transactionType(DefaultTransactionTypes.AUTHORIZE);
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("Request to PayPal Checkout Hosted Endpoint with raw response: " +
                    responseDTO.getRawResponse());
        }

        return responseDTO;

    }

}
