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

package org.broadleafcommerce.vendor.braintree.service.payment;

import com.braintreegateway.TransactionRequest;
import org.broadleafcommerce.core.checkout.service.exception.CheckoutException;
import org.broadleafcommerce.core.checkout.service.workflow.CheckoutResponse;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.profile.core.domain.Address;

/**
 * Created with IntelliJ IDEA.
 * User: elbertbautista
 * Date: 6/21/12
 * Time: 3:27 PM
 */
public interface BraintreeCheckoutService {

    public CheckoutResponse completeAuthorizeAndDebitCheckout(String id, String queryString, Order order) throws CheckoutException;

    public TransactionRequest constructAuthorizeAndDebitFields(TransactionRequest trParams, Order order);

    public TransactionRequest constructAuthorizeAndDebitFieldsFromToken(TransactionRequest trParams, Order order, String paymentMethodToken);

    public TransactionRequest constructProtectedFields(TransactionRequest trParams, Order order, boolean submitForSettlement);

    public TransactionRequest constructSaveVaultCustomerFields(TransactionRequest trParams, Order order);

    public TransactionRequest constructShippingFields(TransactionRequest trParams, Address shippingAddress);

    public TransactionRequest constructBillingFields(TransactionRequest trParams, Address billingAddress);

}
