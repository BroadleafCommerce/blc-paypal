package org.broadleafcommerce.payment.service.gateway;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.domain.PaymentResponse;
import com.broadleafcommerce.paymentgateway.domain.enums.DefaultTransactionTypes;
import com.broadleafcommerce.paymentgateway.service.PaymentGatewayTransactionService;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;

import lombok.RequiredArgsConstructor;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@RequiredArgsConstructor
public class DefaultPayPalCheckoutHostedService implements PayPalCheckoutHostedService {

    protected static final Log LOG = LogFactory.getLog(DefaultPayPalCheckoutHostedService.class);

    private final PayPalGatewayConfiguration configuration;
    private final PaymentGatewayTransactionService transactionService;

    @Override
    public PaymentResponse requestHostedEndpoint(PaymentRequest paymentRequest)
            throws PaymentException {

        PaymentResponse responseDTO;
        if (configuration.isPerformAuthorizeAndCapture()) {
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

    @Override
    public String getGatewayType() {
        return PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT.name();
    }

}
