package org.broadleafcommerce.payment.service.gateway;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.domain.PaymentResponse;
import com.broadleafcommerce.paymentgateway.domain.enums.DefaultTransactionTypes;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.broadleafcommerce.paymentgateway.service.transaction.AbstractPaymentGatewayTransactionConfirmationService;
import com.broadleafcommerce.paymentgateway.service.transaction.PaymentGatewayTransactionConfirmationService;
import com.broadleafcommerce.paymentgateway.service.transaction.PaymentGatewayTransactionService;

import lombok.RequiredArgsConstructor;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@RequiredArgsConstructor
public class DefaultPayPalCheckoutTransactionConfirmationService
        extends AbstractPaymentGatewayTransactionConfirmationService
        implements PaymentGatewayTransactionConfirmationService {

    private final PayPalCheckoutConfiguration configuration;
    private final PaymentGatewayTransactionService transactionService;

    @Override
    public PaymentResponse confirmTransaction(PaymentRequest paymentRequest)
            throws PaymentException {
        PaymentResponse responseDTO = null;
        if (configuration.isPerformAuthorizeAndCapture()) {
            responseDTO = transactionService.authorizeAndCapture(paymentRequest);
            responseDTO.transactionType(DefaultTransactionTypes.AUTHORIZE_AND_CAPTURE);
        } else {
            responseDTO = transactionService.authorize(paymentRequest);
            responseDTO.transactionType(DefaultTransactionTypes.AUTHORIZE);
        }

        return responseDTO;

    }

}
