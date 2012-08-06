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

package org.broadleafcommerce.vendor.paypal.service.payment;

import org.broadleafcommerce.core.checkout.service.exception.CheckoutException;
import org.broadleafcommerce.core.checkout.service.workflow.CheckoutResponse;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.payment.service.exception.PaymentException;
import org.broadleafcommerce.core.payment.service.workflow.CompositePaymentResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.message.details.PayPalDetailsResponse;

/**
 * Created with IntelliJ IDEA.
 * User: elbertbautista
 * Date: 6/13/12
 * Time: 2:25 PM
 */
public interface PayPalCheckoutService {

    public CompositePaymentResponse initiateExpressCheckout(Order order) throws PaymentException;

    public CheckoutResponse completeExpressCheckout(String token, String payerId, Order order) throws CheckoutException;

    public PayPalDetailsResponse getExpressCheckoutDetails(String token) throws PaymentException;

    public CompositePaymentResponse refundTransaction(String transactionId, Order order)throws PaymentException;

}
