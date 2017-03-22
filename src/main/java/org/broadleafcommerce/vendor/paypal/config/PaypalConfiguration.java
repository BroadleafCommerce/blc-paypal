package org.broadleafcommerce.vendor.paypal.config;

import org.broadleafcommerce.common.logging.LifeCycleEvent;
import org.broadleafcommerce.common.logging.ModuleLifecycleLoggingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Nick Crum ncrum
 */
@Configuration
public class PaypalConfiguration {

    @Bean
    public ModuleLifecycleLoggingBean blEnterpriseModuleLifecycle() {
        return new ModuleLifecycleLoggingBean(PaypalModuleRegistration.MODULE_NAME, LifeCycleEvent.LOADING);
    }
}
