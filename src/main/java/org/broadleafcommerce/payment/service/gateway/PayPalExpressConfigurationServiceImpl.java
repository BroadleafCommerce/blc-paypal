/*
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2014 Broadleaf Commerce
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.broadleafcommerce.payment.service.gateway;

import org.broadleafcommerce.common.payment.service.AbstractPaymentGatewayConfigurationService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayConfiguration;
import org.broadleafcommerce.common.payment.service.PaymentGatewayConfigurationService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayCreditCardService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayCustomerService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayFraudService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayHostedService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayReportingService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayRollbackService;
import org.broadleafcommerce.common.payment.service.PaymentGatewaySubscriptionService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayTransactionConfirmationService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayTransactionService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayTransparentRedirectService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayWebResponseService;
import org.broadleafcommerce.common.web.payment.expression.PaymentGatewayFieldExtensionHandler;
import org.broadleafcommerce.common.web.payment.processor.CreditCardTypesExtensionHandler;
import org.broadleafcommerce.common.web.payment.processor.TRCreditCardExtensionHandler;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalExpressConfigurationService")
public class PayPalExpressConfigurationServiceImpl extends AbstractPaymentGatewayConfigurationService implements PaymentGatewayConfigurationService {

    @Resource(name = "blPayPalExpressConfiguration")
    protected PayPalExpressConfiguration configuration;

    @Resource(name = "blPayPalExpressTransactionService")
    protected PaymentGatewayTransactionService transactionService;

    @Resource(name = "blPayPalExpressTransactionConfirmationService")
    protected PaymentGatewayTransactionConfirmationService transactionConfirmationService;

    @Resource(name = "blPayPalExpressReportingService")
    protected PaymentGatewayReportingService reportingService;

    @Resource(name = "blPayPalExpressRollbackService")
    protected PaymentGatewayRollbackService rollbackService;

    @Resource(name = "blPayPalExpressWebResponseService")
    protected PaymentGatewayWebResponseService webResponseService;

    public PaymentGatewayConfiguration getConfiguration() {
        return configuration;
    }

    public PaymentGatewayTransactionService getTransactionService() {
        return transactionService;
    }

    public PaymentGatewayTransactionConfirmationService getTransactionConfirmationService() {
        return transactionConfirmationService;
    }

    public PaymentGatewayReportingService getReportingService() {
        return reportingService;
    }

    public PaymentGatewayRollbackService getRollbackService() {
        return rollbackService;
    }

    public PaymentGatewayWebResponseService getWebResponseService() {
        return webResponseService;
    }

}
