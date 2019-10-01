package org.broadleafcommerce.payment.service.gateway;

import com.broadleafcommerce.paymentgateway.service.configuration.AbstractPaymentGatewayConfigurationService;
import com.broadleafcommerce.paymentgateway.service.configuration.PaymentGatewayConfiguration;
import com.broadleafcommerce.paymentgateway.service.configuration.PaymentGatewayConfigurationService;
import com.broadleafcommerce.paymentgateway.service.hosted.PaymentGatewayHostedService;
import com.broadleafcommerce.paymentgateway.service.reporting.PaymentGatewayReportingService;
import com.broadleafcommerce.paymentgateway.service.rollback.PaymentGatewayRollbackService;
import com.broadleafcommerce.paymentgateway.service.transaction.PaymentGatewayTransactionConfirmationService;
import com.broadleafcommerce.paymentgateway.service.transaction.PaymentGatewayTransactionService;
import com.broadleafcommerce.paymentgateway.service.webresponse.PaymentGatewayWebResponseService;

import lombok.RequiredArgsConstructor;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@RequiredArgsConstructor
public class DefaultPayPalCheckoutConfigurationService extends
        AbstractPaymentGatewayConfigurationService implements PaymentGatewayConfigurationService {

    private final PayPalCheckoutConfiguration configuration;
    private final PaymentGatewayTransactionService transactionService;
    private final PaymentGatewayTransactionConfirmationService transactionConfirmationService;
    private final PaymentGatewayReportingService reportingService;
    private final PaymentGatewayRollbackService rollbackService;
    private final PaymentGatewayWebResponseService webResponseService;
    private final PaymentGatewayHostedService hostedService;

    @Override
    public PaymentGatewayConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public PaymentGatewayTransactionService getTransactionService() {
        return transactionService;
    }

    @Override
    public PaymentGatewayTransactionConfirmationService getTransactionConfirmationService() {
        return transactionConfirmationService;
    }

    @Override
    public PaymentGatewayReportingService getReportingService() {
        return reportingService;
    }

    @Override
    public PaymentGatewayRollbackService getRollbackService() {
        return rollbackService;
    }

    @Override
    public PaymentGatewayWebResponseService getWebResponseService() {
        return webResponseService;
    }

    @Override
    public PaymentGatewayHostedService getHostedService() {
        return hostedService;
    }
}
