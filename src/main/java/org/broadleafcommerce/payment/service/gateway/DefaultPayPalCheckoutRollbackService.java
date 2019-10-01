package org.broadleafcommerce.payment.service.gateway;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.domain.PaymentResponse;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.broadleafcommerce.paymentgateway.service.rollback.AbstractPaymentGatewayRollbackService;
import com.broadleafcommerce.paymentgateway.service.rollback.PaymentGatewayRollbackService;
import com.broadleafcommerce.paymentgateway.service.transaction.PaymentGatewayTransactionService;

import lombok.RequiredArgsConstructor;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@RequiredArgsConstructor
public class DefaultPayPalCheckoutRollbackService extends AbstractPaymentGatewayRollbackService
        implements PaymentGatewayRollbackService {

    private final PaymentGatewayTransactionService transactionService;

    @Override
    public PaymentResponse rollbackAuthorize(PaymentRequest transactionToBeRolledBack)
            throws PaymentException {
        return transactionService.refund(transactionToBeRolledBack);
    }

    @Override
    public PaymentResponse rollbackCapture(PaymentRequest transactionToBeRolledBack)
            throws PaymentException {
        return transactionService.refund(transactionToBeRolledBack);
    }

    @Override
    public PaymentResponse rollbackAuthorizeAndCapture(PaymentRequest transactionToBeRolledBack)
            throws PaymentException {
        return transactionService.refund(transactionToBeRolledBack);
    }

    @Override
    public PaymentResponse rollbackRefund(PaymentRequest transactionToBeRolledBack)
            throws PaymentException {
        throw new PaymentException("The Rollback Refund method is not supported for this module");
    }

}