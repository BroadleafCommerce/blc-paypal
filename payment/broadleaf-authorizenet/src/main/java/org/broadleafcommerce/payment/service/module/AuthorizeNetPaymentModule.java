/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.payment.service.module;

import org.broadleafcommerce.core.payment.domain.PaymentResponseItem;
import org.broadleafcommerce.core.payment.service.PaymentContext;
import org.broadleafcommerce.core.payment.service.exception.PaymentException;
import org.broadleafcommerce.core.payment.service.module.PaymentModule;
import org.broadleafcommerce.core.payment.service.type.PaymentInfoType;
import org.broadleafcommerce.vendor.authorizenet.service.payment.AuthorizeNetPaymentService;

/**
 * Created with IntelliJ IDEA.
 * User: elbertbautista
 * Date: 6/27/12
 * Time: 10:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class AuthorizeNetPaymentModule implements PaymentModule {

    protected AuthorizeNetPaymentService authorizeNetPaymentService;

    @Override
    public PaymentResponseItem authorize(PaymentContext paymentContext) throws PaymentException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PaymentResponseItem reverseAuthorize(PaymentContext paymentContext) throws PaymentException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PaymentResponseItem debit(PaymentContext paymentContext) throws PaymentException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PaymentResponseItem authorizeAndDebit(PaymentContext paymentContext) throws PaymentException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PaymentResponseItem credit(PaymentContext paymentContext) throws PaymentException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PaymentResponseItem voidPayment(PaymentContext paymentContext) throws PaymentException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PaymentResponseItem balance(PaymentContext paymentContext) throws PaymentException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Boolean isValidCandidate(PaymentInfoType paymentType) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public AuthorizeNetPaymentService getAuthorizeNetPaymentService() {
        return authorizeNetPaymentService;
    }

    public void setAuthorizeNetPaymentService(AuthorizeNetPaymentService authorizeNetPaymentService) {
        this.authorizeNetPaymentService = authorizeNetPaymentService;
    }
}
