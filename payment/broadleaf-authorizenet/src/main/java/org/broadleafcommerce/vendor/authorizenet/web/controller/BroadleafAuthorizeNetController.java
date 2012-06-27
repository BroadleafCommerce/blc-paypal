/*
 * Copyright 2008-2009 the original author or authors.
 *
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
 */

package org.broadleafcommerce.vendor.authorizenet.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.web.controller.BroadleafAbstractController;
import org.broadleafcommerce.core.checkout.service.exception.CheckoutException;
import org.broadleafcommerce.core.payment.service.exception.PaymentException;
import org.broadleafcommerce.core.pricing.service.exception.PricingException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: elbertbautista
 * Date: 6/27/12
 * Time: 11:54 AM
 */
public class BroadleafAuthorizeNetController extends BroadleafAbstractController {

    private static final Log LOG = LogFactory.getLog(BroadleafAuthorizeNetController.class);

    public String constructAuthorizeAndDebitAuthorizeNetForm(Model model, HttpServletRequest request) throws PaymentException {
        return null;
    }


    public String processAuthorizeNetAuthorizeAndDebit(Model model, HttpServletRequest request) throws CheckoutException, PricingException {
        return null;
    }

}
