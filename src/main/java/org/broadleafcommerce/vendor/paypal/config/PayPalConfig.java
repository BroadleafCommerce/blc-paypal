/*
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2018 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.vendor.paypal.config;

import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.paypal.base.rest.APIContext;

@Configuration
public class PayPalConfig {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public APIContext blPayPalApiContext(@Value("${gateway.paypal.checkout.rest.clientId}") String clientId,
                                         @Value("${gateway.paypal.checkout.rest.secret}") String secret,
                                         @Value("${gateway.paypal.checkout.rest.mode}") String mode) {
        APIContext context = new APIContext(clientId, secret, mode);
        context.addHTTPHeader(MessageConstants.BN, MessageConstants.BNCODE);
        return context;
    }
}
