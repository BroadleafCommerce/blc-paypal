package org.broadleafcommerce.payment.service.gateway.autoconfigure;

import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutConfiguration;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutExternalCallService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutHostedService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutReportingService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutRollbackService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutTransactionConfirmationService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutTransactionService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalSyncTransactionService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutConfiguration;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutExternalCallService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutHostedService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutReportingService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutRollbackService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutTransactionConfirmationService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutTransactionService;
import org.broadleafcommerce.payment.service.gateway.PayPalSyncTransactionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.broadleafcommerce.paymentgateway.service.transaction.PaymentGatewayTransactionService;

@Configuration
public class PayPalServiceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PayPalCheckoutExternalCallService payPalCheckoutExternalCallService(
            PayPalCheckoutConfiguration configuration) {
        return new DefaultPayPalCheckoutExternalCallService(configuration);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalCheckoutConfiguration payPalCheckoutConfiguration(Environment env) {
        return new DefaultPayPalCheckoutConfiguration(env);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalSyncTransactionService payPalSyncTransactionService(
            PayPalCheckoutExternalCallService externalCallService) {
        return new DefaultPayPalSyncTransactionService(externalCallService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalCheckoutTransactionService payPalCheckoutTransactionService(
            PayPalCheckoutExternalCallService externalCallService) {
        return new DefaultPayPalCheckoutTransactionService(externalCallService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalCheckoutRollbackService payPalCheckoutRollbackService(
            PaymentGatewayTransactionService transactionService) {
        return new DefaultPayPalCheckoutRollbackService(transactionService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalCheckoutTransactionConfirmationService payPalCheckoutTransactionConfirmationService(
            PayPalCheckoutConfiguration configuration,
            PaymentGatewayTransactionService transactionService) {
        return new DefaultPayPalCheckoutTransactionConfirmationService(configuration,
                transactionService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalCheckoutHostedService payPalCheckoutHostedService(
            PayPalCheckoutConfiguration configuration,
            PaymentGatewayTransactionService transactionService) {
        return new DefaultPayPalCheckoutHostedService(configuration, transactionService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalCheckoutReportingService payPalCheckoutReportingService(
            PayPalCheckoutExternalCallService externalCallService) {
        return new DefaultPayPalCheckoutReportingService(externalCallService);
    }

}
