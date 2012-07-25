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

import net.authorize.ResponseField;
import net.authorize.sim.Result;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.time.SystemTime;
import org.broadleafcommerce.core.payment.domain.PaymentInfo;
import org.broadleafcommerce.core.payment.domain.PaymentResponseItem;
import org.broadleafcommerce.core.payment.domain.PaymentResponseItemImpl;
import org.broadleafcommerce.core.payment.service.PaymentContext;
import org.broadleafcommerce.core.payment.service.exception.PaymentException;
import org.broadleafcommerce.core.payment.service.module.PaymentModule;
import org.broadleafcommerce.core.payment.service.type.PaymentInfoType;
import org.broadleafcommerce.vendor.authorizenet.service.payment.AuthorizeNetPaymentService;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: elbertbautista
 * Date: 6/27/12
 * Time: 10:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class AuthorizeNetPaymentModule implements PaymentModule {

    private static final Log LOG = LogFactory.getLog(AuthorizeNetPaymentModule.class);

    //TODO inject this for Authorize.net AIM (Advance Integration Method)
    protected AuthorizeNetPaymentService authorizeNetPaymentService;

    @Override
    public PaymentResponseItem authorize(PaymentContext paymentContext) throws PaymentException {
        return authorizeAndDebit(paymentContext);
    }

    @Override
    public PaymentResponseItem reverseAuthorize(PaymentContext paymentContext) throws PaymentException {
        throw new PaymentException("The reverseAuthorize method is not supported by this org.broadleafcommerce.payment.service.module.AuthorizeNetPaymentModule");
    }

    @Override
    public PaymentResponseItem debit(PaymentContext paymentContext) throws PaymentException {
        throw new PaymentException("The debit method is not supported by this org.broadleafcommerce.payment.service.module.AuthorizeNetPaymentModule");
    }

    @Override
    public PaymentResponseItem authorizeAndDebit(PaymentContext paymentContext) throws PaymentException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating Payment Response for authorize and debit.");
        }

        Assert.isTrue(paymentContext.getPaymentInfo().getAdditionalFields().containsKey(ResponseField.RESPONSE_CODE.getFieldName()), "Must pass a RESPONSE_CODE value on the additionalFields of the PaymentInfo instance.");
        Assert.isTrue(paymentContext.getPaymentInfo().getAmount() != null, "Payment Info Amount must not be null");

        if (LOG.isDebugEnabled()) {
            LOG.debug(paymentContext.getPaymentInfo().getAdditionalFields().get(ResponseField.RESPONSE_CODE.getFieldName()));
        }

        PaymentResponseItem responseItem = buildBasicDPMResponse(paymentContext);
        responseItem.setPaymentInfoId(paymentContext.getPaymentInfo().getId());

        return responseItem;
    }

    @Override
    public PaymentResponseItem credit(PaymentContext paymentContext) throws PaymentException {
        throw new PaymentException("The credit method is not supported by this org.broadleafcommerce.payment.service.module.AuthorizeNetPaymentModule");
    }

    @Override
    public PaymentResponseItem voidPayment(PaymentContext paymentContext) throws PaymentException {
        throw new PaymentException("The voidPayment method is not supported by this org.broadleafcommerce.payment.service.module.AuthorizeNetPaymentModule");
    }

    @Override
    public PaymentResponseItem balance(PaymentContext paymentContext) throws PaymentException {
        throw new PaymentException("The balance method is not supported by this org.broadleafcommerce.payment.service.module.AuthorizeNetPaymentModule");
    }

    private PaymentResponseItem buildBasicDPMResponse(PaymentContext paymentContext) {
        PaymentResponseItem responseItem = new PaymentResponseItemImpl();
        responseItem.setTransactionSuccess("1".equals(paymentContext.getPaymentInfo().getAdditionalFields().get(ResponseField.RESPONSE_CODE.getFieldName())));
        responseItem.setTransactionTimestamp(SystemTime.asDate());
        responseItem.setAmountPaid(paymentContext.getPaymentInfo().getAmount());
        return responseItem;
    }

    @Override
    public Boolean isValidCandidate(PaymentInfoType paymentType) {
        return PaymentInfoType.CREDIT_CARD.equals(paymentType);
    }

    public AuthorizeNetPaymentService getAuthorizeNetPaymentService() {
        return authorizeNetPaymentService;
    }

    public void setAuthorizeNetPaymentService(AuthorizeNetPaymentService authorizeNetPaymentService) {
        this.authorizeNetPaymentService = authorizeNetPaymentService;
    }
}
