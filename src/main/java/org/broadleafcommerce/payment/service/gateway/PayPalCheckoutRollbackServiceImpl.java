package org.broadleafcommerce.payment.service.gateway;

import org.springframework.stereotype.Service;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.domain.PaymentResponse;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
import com.broadleafcommerce.paymentgateway.service.rollback.AbstractPaymentGatewayRollbackService;
import com.broadleafcommerce.paymentgateway.service.rollback.PaymentGatewayRollbackService;
import com.broadleafcommerce.paymentgateway.service.transaction.PaymentGatewayTransactionService;

import javax.annotation.Resource;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalCheckoutRollbackService")
public class PayPalCheckoutRollbackServiceImpl extends AbstractPaymentGatewayRollbackService
        implements PaymentGatewayRollbackService {

    @Resource(name = "blPayPalCheckoutTransactionService")
    protected PaymentGatewayTransactionService transactionService;

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
