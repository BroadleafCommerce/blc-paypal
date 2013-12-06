/*
 * Copyright 2008-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.vendor.paypal.web;

import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.PaymentGatewayConfigurationService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayWebResponseService;
import org.broadleafcommerce.common.web.payment.controller.PaymentGatewayAbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Controller("blPayPalExpressController")
@RequestMapping("/" + BroadleafPayPalExpressController.GATEWAY_CONTEXT_KEY)
public class BroadleafPayPalExpressController extends PaymentGatewayAbstractController {

    protected static final String GATEWAY_CONTEXT_KEY = "paypal-express";

    @Override
    public void handleProcessingException(Exception e) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleUnsuccessfulTransaction(Model model, PaymentResponseDTO responseDTO) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PaymentGatewayWebResponseService getWebResponseService() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PaymentGatewayConfigurationService getConfigurationService() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
