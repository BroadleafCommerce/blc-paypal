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

package org.broadleafcommerce.vendor.paypal.service.payment;

import org.broadleafcommerce.common.payment.PaymentGatewayType;

/**
 * @author Elbert Bautista (elbertbautista)
 */
public class PayPalExpressPaymentGatewayType extends PaymentGatewayType {

    public static final PaymentGatewayType PAYPAL_EXPRESS  = new PaymentGatewayType("PayPal_Express", "PayPal Express Checkout");

}
