package org.broadleafcommerce.vendor.paypal.service.autoconfigure;

import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutExternalCallService;
import org.broadleafcommerce.payment.service.gateway.PayPalCheckoutRestConfigurationProperties;
import org.broadleafcommerce.payment.service.gateway.PayPalGatewayConfiguration;
import org.broadleafcommerce.vendor.paypal.service.DefaultPayPalAgreementTokenService;
import org.broadleafcommerce.vendor.paypal.service.DefaultPayPalBillingAgreementService;
import org.broadleafcommerce.vendor.paypal.service.DefaultPayPalPaymentService;
import org.broadleafcommerce.vendor.paypal.service.DefaultPayPalWebExperienceProfileService;
import org.broadleafcommerce.vendor.paypal.service.PayPalAgreementTokenService;
import org.broadleafcommerce.vendor.paypal.service.PayPalBillingAgreementService;
import org.broadleafcommerce.vendor.paypal.service.PayPalPaymentService;
import org.broadleafcommerce.vendor.paypal.service.PayPalWebExperienceProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.paypal.api.payments.WebProfile;

@Configuration
public class PayPalVendorServiceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PayPalAgreementTokenService payPalAgreementTokenService(
            PayPalCheckoutExternalCallService externalCallService) {
        return new DefaultPayPalAgreementTokenService(externalCallService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalBillingAgreementService payPalBillingAgreementService(
            PayPalCheckoutExternalCallService externalCallService) {
        return new DefaultPayPalBillingAgreementService(externalCallService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalPaymentService payPalPaymentService(
            PayPalCheckoutExternalCallService externalCallService,
            PayPalGatewayConfiguration gatewayConfiguration,
            PayPalWebExperienceProfileService webExperienceProfileService,
            PayPalCheckoutRestConfigurationProperties properties) {
        return new DefaultPayPalPaymentService(externalCallService,
                gatewayConfiguration,
                webExperienceProfileService,
                properties.shouldPopulateShippingOnCreatePayment());
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalWebExperienceProfileService payPalWebProfileService(
            PayPalCheckoutExternalCallService externalCallService,
            @Autowired(required = false) WebProfile webProfile) {
        return new DefaultPayPalWebExperienceProfileService(externalCallService, webProfile);
    }

}
