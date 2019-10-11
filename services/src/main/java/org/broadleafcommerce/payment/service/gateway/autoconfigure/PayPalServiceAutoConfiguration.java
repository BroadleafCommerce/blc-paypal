package org.broadleafcommerce.payment.service.gateway.autoconfigure;

import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutExternalCallService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutHostedService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutReportingService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutRollbackService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutTransactionConfirmationService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalCheckoutTransactionService;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalGatewayConfiguration;
import org.broadleafcommerce.payment.service.gateway.DefaultPayPalSyncTransactionService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutExternalCallService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutHostedService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutReportingService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutRestConfigurationProperties;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutRollbackService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutTransactionConfirmationService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutTransactionService;
import org.broadleafcommerce.payment.service.gateway.PayPalGatewayConfiguration;
import org.broadleafcommerce.payment.service.gateway.PayPalSyncTransactionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.broadleafcommerce.paymentgateway.service.transaction.PaymentGatewayTransactionService;

@Configuration
@EnableConfigurationProperties({PayPalCheckoutRestConfigurationProperties.class})
public class PayPalServiceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PayPalCheckoutExternalCallService payPalCheckoutExternalCallService(
            PayPalCheckoutRestConfigurationProperties configProperties,
            PayPalGatewayConfiguration gatewayConfiguration) {
        return new DefaultPayPalCheckoutExternalCallService(configProperties,
                gatewayConfiguration);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalGatewayConfiguration payPalGatewayConfiguration() {
        return new DefaultPayPalGatewayConfiguration();
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
            PayPalGatewayConfiguration gatewayConfiguration,
            PaymentGatewayTransactionService transactionService) {
        return new DefaultPayPalCheckoutTransactionConfirmationService(gatewayConfiguration,
                transactionService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalCheckoutHostedService payPalCheckoutHostedService(
            PayPalGatewayConfiguration gatewayConfiguration,
            PaymentGatewayTransactionService transactionService) {
        return new DefaultPayPalCheckoutHostedService(gatewayConfiguration, transactionService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalCheckoutReportingService payPalCheckoutReportingService(
            PayPalCheckoutExternalCallService externalCallService) {
        return new DefaultPayPalCheckoutReportingService(externalCallService);
    }

}
