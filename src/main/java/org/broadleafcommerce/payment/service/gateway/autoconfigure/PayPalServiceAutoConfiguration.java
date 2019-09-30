package org.broadleafcommerce.payment.service.gateway.autoconfigure;

import org.broadleafcommerce.payment.service.gateway.DefaultExternalCallPayPalCheckoutService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutConfiguration;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutConfigurationService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutHostedService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutReportingService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutRollbackService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutTransactionConfirmationService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutTransactionService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalSyncTransactionService;
import org.broadleafcommerce.payment.service.gateway.ExternalCallPayPalCheckoutService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutConfiguration;
import org.broadleafcommerce.payment.service.gateway.PayPalSyncTransactionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.broadleafcommerce.paymentgateway.service.configuration.PaymentGatewayConfigurationService;
import com.broadleafcommerce.paymentgateway.service.hosted.PaymentGatewayHostedService;
import com.broadleafcommerce.paymentgateway.service.reporting.PaymentGatewayReportingService;
import com.broadleafcommerce.paymentgateway.service.rollback.PaymentGatewayRollbackService;
import com.broadleafcommerce.paymentgateway.service.transaction.PaymentGatewayTransactionConfirmationService;
import com.broadleafcommerce.paymentgateway.service.transaction.PaymentGatewayTransactionService;

@Configuration
public class PayPalServiceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ExternalCallPayPalCheckoutService externalCallPayPalCheckoutService(
            PayPalCheckoutConfiguration configuration) {
        return new DefaultExternalCallPayPalCheckoutService(configuration);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalCheckoutConfiguration payPalCheckoutConfiguration(Environment env) {
        return new DefaultPayPalCheckoutConfiguration(env);
    }

    @Bean
    @ConditionalOnMissingBean
    public PaymentGatewayConfigurationService paymentGatewayConfigurationService(
            PayPalCheckoutConfiguration configuration,
            PaymentGatewayTransactionService transactionService,
            PaymentGatewayTransactionConfirmationService transactionConfirmationService,
            PaymentGatewayReportingService reportingService,
            PaymentGatewayRollbackService rollbackService,
            PaymentGatewayHostedService hostedService) {
        return new DefaultPayPalCheckoutConfigurationService(configuration,
                transactionService,
                transactionConfirmationService,
                reportingService,
                rollbackService,
                null,
                hostedService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PaymentGatewayHostedService paymentGatewayHostedService(
            PayPalCheckoutConfiguration configuration,
            PaymentGatewayTransactionService transactionService) {
        return new DefaultPayPalCheckoutHostedService(configuration, transactionService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PaymentGatewayReportingService paymentGatewayReportingService(
            ExternalCallPayPalCheckoutService payPalCheckoutService) {
        return new DefaultPayPalCheckoutReportingService(payPalCheckoutService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PaymentGatewayRollbackService paymentGatewayRollbackService(
            PaymentGatewayTransactionService transactionService) {
        return new DefaultPayPalCheckoutRollbackService(transactionService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PaymentGatewayTransactionConfirmationService paymentGatewayTransactionConfirmationService(
            PayPalCheckoutConfiguration configuration,
            PaymentGatewayTransactionService transactionService) {
        return new DefaultPayPalCheckoutTransactionConfirmationService(configuration,
                transactionService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PaymentGatewayTransactionService paymentGatewayTransactionService(
            ExternalCallPayPalCheckoutService payPalCheckoutService) {
        return new DefaultPayPalCheckoutTransactionService(payPalCheckoutService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalSyncTransactionService payPalSyncTransactionService(
            ExternalCallPayPalCheckoutService payPalCheckoutService) {
        return new DefaultPayPalSyncTransactionService(payPalCheckoutService);
    }

}
