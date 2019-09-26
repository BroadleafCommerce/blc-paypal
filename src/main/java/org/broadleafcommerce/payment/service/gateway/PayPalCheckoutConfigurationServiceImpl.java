package org.broadleafcommerce.payment.service.gateway;

import org.springframework.stereotype.Service;

import com.broadleafcommerce.paymentgateway.service.configuration.AbstractPaymentGatewayConfigurationService;
import com.broadleafcommerce.paymentgateway.service.configuration.PaymentGatewayConfiguration;
import com.broadleafcommerce.paymentgateway.service.configuration.PaymentGatewayConfigurationService;
import com.broadleafcommerce.paymentgateway.service.hosted.PaymentGatewayHostedService;
import com.broadleafcommerce.paymentgateway.service.reporting.PaymentGatewayReportingService;
import com.broadleafcommerce.paymentgateway.service.rollback.PaymentGatewayRollbackService;
import com.broadleafcommerce.paymentgateway.service.transaction.PaymentGatewayTransactionConfirmationService;
import com.broadleafcommerce.paymentgateway.service.transaction.PaymentGatewayTransactionService;
import com.broadleafcommerce.paymentgateway.service.webresponse.PaymentGatewayWebResponseService;

import javax.annotation.Resource;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalCheckoutConfigurationService")
public class PayPalCheckoutConfigurationServiceImpl extends
        AbstractPaymentGatewayConfigurationService implements PaymentGatewayConfigurationService {

    @Resource(name = "blPayPalCheckoutConfiguration")
    protected PayPalCheckoutConfiguration configuration;

    @Resource(name = "blPayPalCheckoutTransactionService")
    protected PaymentGatewayTransactionService transactionService;

    @Resource(name = "blPayPalCheckoutTransactionConfirmationService")
    protected PaymentGatewayTransactionConfirmationService transactionConfirmationService;

    @Resource(name = "blPayPalCheckoutReportingService")
    protected PaymentGatewayReportingService reportingService;

    @Resource(name = "blPayPalCheckoutRollbackService")
    protected PaymentGatewayRollbackService rollbackService;

    @Resource(name = "blPayPalCheckoutWebResponseService")
    protected PaymentGatewayWebResponseService webResponseService;

    @Resource(name = "blPayPalCheckoutHostedService")
    protected PaymentGatewayHostedService hostedService;

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
