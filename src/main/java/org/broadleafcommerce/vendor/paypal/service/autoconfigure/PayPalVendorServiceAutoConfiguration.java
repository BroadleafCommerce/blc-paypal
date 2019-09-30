package org.broadleafcommerce.vendor.paypal.service.autoconfigure;

import org.broadleafcommerce.payment.service.gateway.ExternalCallPayPalCheckoutService;
import org.broadleafcommerce.vendor.paypal.service.DefaultPayPalAgreementTokenService;
import org.broadleafcommerce.vendor.paypal.service.DefaultPayPalBillingAgreementService;
import org.broadleafcommerce.vendor.paypal.service.DefaultPayPalPaymentService;
import org.broadleafcommerce.vendor.paypal.service.DefaultPayPalWebProfileService;
import org.broadleafcommerce.vendor.paypal.service.PayPalAgreementTokenService;
import org.broadleafcommerce.vendor.paypal.service.PayPalBillingAgreementService;
import org.broadleafcommerce.vendor.paypal.service.PayPalPaymentService;
import org.broadleafcommerce.vendor.paypal.service.PayPalWebProfileService;
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
            ExternalCallPayPalCheckoutService externalCallService) {
        return new DefaultPayPalAgreementTokenService(externalCallService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalBillingAgreementService payPalBillingAgreementService(
            ExternalCallPayPalCheckoutService externalCallService) {
        return new DefaultPayPalBillingAgreementService(externalCallService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalPaymentService payPalPaymentService(
            ExternalCallPayPalCheckoutService externalCallService,
            PayPalWebProfileService webProfileService) {
        return new DefaultPayPalPaymentService(externalCallService, webProfileService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PayPalWebProfileService payPalWebProfileService(
            ExternalCallPayPalCheckoutService externalCallService,
            @Autowired(required = false) WebProfile webProfile) {
        return new DefaultPayPalWebProfileService(externalCallService, webProfile);
    }

}
