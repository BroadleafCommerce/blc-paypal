package org.broadleafcommerce.payment.service.gateway;

import org.springframework.stereotype.Service;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.domain.PaymentResponse;
import com.broadleafcommerce.paymentgateway.domain.enums.DefaultTransactionTypes;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.broadleafcommerce.paymentgateway.service.transaction.AbstractPaymentGatewayTransactionConfirmationService;
import com.broadleafcommerce.paymentgateway.service.transaction.PaymentGatewayTransactionConfirmationService;
import com.broadleafcommerce.paymentgateway.service.transaction.PaymentGatewayTransactionService;

import javax.annotation.Resource;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalCheckoutTransactionConfirmationService")
public class PayPalCheckoutTransactionConfirmationServiceImpl
        extends AbstractPaymentGatewayTransactionConfirmationService
        implements PaymentGatewayTransactionConfirmationService {

    @Resource(name = "blPayPalCheckoutConfiguration")
    protected PayPalCheckoutConfiguration configuration;

    @Resource(name = "blPayPalCheckoutTransactionService")
    protected PaymentGatewayTransactionService transactionService;

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
