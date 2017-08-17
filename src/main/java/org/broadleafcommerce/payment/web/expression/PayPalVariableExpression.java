/*
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2017 Broadleaf Commerce
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
package org.broadleafcommerce.payment.web.expression;

import org.apache.commons.lang3.StringUtils;
import org.broadleafcommerce.common.web.expression.BroadleafVariableExpression;
import org.broadleafcommerce.payment.service.gateway.PayPalExpressConfiguration;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Chris Kittrell (ckittrell)
 */
@Component("blPayPalVariableExpression")
public class PayPalVariableExpression implements BroadleafVariableExpression {

    @Resource(name = "blPayPalExpressConfiguration")
    protected PayPalExpressConfiguration configuration;

    @Override
    public String getName() {
        return "paypal";
    }

    public boolean hasConfigProperties() {
        return StringUtils.isNotBlank(configuration.getUser())
                && StringUtils.isNotBlank(configuration.getPassword())
                && StringUtils.isNotBlank(configuration.getSignature())
                && StringUtils.isNotBlank(configuration.getSignature())
                && StringUtils.isNotBlank(configuration.getLibVersion())
                && StringUtils.isNotBlank(configuration.getServerUrl())
                && StringUtils.isNotBlank(configuration.getUserRedirectUrl())
                && StringUtils.isNotBlank(configuration.getReturnUrl())
                && StringUtils.isNotBlank(configuration.getCancelUrl())
                && configuration.getShippingDisplayType() != null;
    }

}
