package org.broadleafcommerce.vendor.paypal.config;

import org.broadleafcommerce.common.module.BroadleafModuleRegistration;

/**
 * @author Nick Crum ncrum
 */
public class PaypalModuleRegistration implements BroadleafModuleRegistration {

    public static final String MODULE_NAME = "Paypal";

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }
}
