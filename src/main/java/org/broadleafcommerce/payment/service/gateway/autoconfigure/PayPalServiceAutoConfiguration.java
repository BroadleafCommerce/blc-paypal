package org.broadleafcommerce.payment.service.gateway.autoconfigure;

import org.broadleafcommerce.payment.service.gateway.DefaultPayPalExternalCallService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutConfiguration;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalConfigurationService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalHostedService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalReportingService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalRollbackService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalTransactionConfirmationService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalTransactionService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalSyncTransactionService;
import org.broadleafcommerce.payment.service.gateway.PayPalExternalCallService;
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
    public PayPalExternalCallService externalCallPayPalCheckoutService(
            PayPalCheckoutConfiguration configuration) {
        return new DefaultPayPalExternalCallService(configuration);
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
        return new DefaultPayPalConfigurationService(configuration,
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
        return new DefaultPayPalHostedService(configuration, transactionService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PaymentGatewayReportingService paymentGatewayReportingService(
            PayPalExternalCallService payPalCheckoutService) {
        return new DefaultPayPalReportingService(payPalCheckoutService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PaymentGatewayRollbackService paymentGatewayRollbackService(
            PaymentGatewayTransactionService transactionService) {
        return new DefaultPayPalRollbackService(transactionService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PaymentGatewayTransactionConfirmationService paymentGatewayTransactionConfirmationService(
            PayPalCheckoutConfiguration configuration,
            PaymentGatewayTransactionService transactionService) {
        return new DefaultPayPalTransactionConfirmationService(configuration,
                transactionService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PaymentGatewayTransactionService paymentGatewayTransactionService(
            PayPalExternalCallService payPalCheckoutService) {
        return new DefaultPayPalTransactionService(payPalCheckoutService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalSyncTransactionService payPalSyncTransactionService(
            PayPalExternalCallService payPalCheckoutService) {
        return new DefaultPayPalSyncTransactionService(payPalCheckoutService);
    }

}
