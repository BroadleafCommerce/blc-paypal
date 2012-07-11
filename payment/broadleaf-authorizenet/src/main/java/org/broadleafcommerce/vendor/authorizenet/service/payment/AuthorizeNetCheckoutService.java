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

package org.broadleafcommerce.vendor.authorizenet.service.payment;

import net.authorize.sim.Fingerprint;
import org.broadleafcommerce.core.checkout.service.exception.CheckoutException;
import org.broadleafcommerce.core.checkout.service.workflow.CheckoutResponse;
import org.broadleafcommerce.core.order.domain.Order;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: elbertbautista
 * Date: 6/27/12
 * Time: 11:42 AM
 */
public interface AuthorizeNetCheckoutService {

    public Order findCartForCustomer(Map<String, String[]> responseMap) throws NoSuchAlgorithmException, UnsupportedEncodingException;

    public CheckoutResponse completeAuthorizeAndDebitCheckout(Order order, Map<String, String[]> responseMap) throws CheckoutException;

    public Map<String, String> constructAuthorizeAndDebitFields(Order order) throws NoSuchAlgorithmException, UnsupportedEncodingException;

}
