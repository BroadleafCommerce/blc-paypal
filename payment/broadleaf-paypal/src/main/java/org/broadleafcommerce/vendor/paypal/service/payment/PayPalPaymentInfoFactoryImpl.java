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

package org.broadleafcommerce.vendor.paypal.service.payment;

import org.broadleafcommerce.core.order.domain.BundleOrderItem;
import org.broadleafcommerce.core.order.domain.DiscreteOrderItem;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.payment.domain.AmountItem;
import org.broadleafcommerce.core.payment.domain.AmountItemImpl;
import org.broadleafcommerce.core.payment.domain.PaymentInfo;
import org.broadleafcommerce.core.payment.domain.PaymentInfoImpl;
import org.broadleafcommerce.core.payment.service.PaymentInfoFactory;
import org.broadleafcommerce.core.payment.service.type.PaymentInfoType;

/**
 * Created with IntelliJ IDEA.
 * User: elbertbautista
 * Date: 6/13/12
 * Time: 1:54 PM
 */
public class PayPalPaymentInfoFactoryImpl implements PaymentInfoFactory{

    @Override
    public PaymentInfo constructPaymentInfo(Order order) {
        PaymentInfoImpl paymentInfo = new PaymentInfoImpl();
        paymentInfo.setOrder(order);
        paymentInfo.setType(PaymentInfoType.PAYPAL);
        paymentInfo.setReferenceNumber(String.valueOf(order.getId()));
        paymentInfo.setAmount(order.getTotal());
        paymentInfo.getAdditionalFields().put(MessageConstants.SUBTOTAL, order.getSubTotal().toString());
        paymentInfo.getAdditionalFields().put(MessageConstants.TOTALSHIPPING, order.getTotalShipping().toString());
        paymentInfo.getAdditionalFields().put(MessageConstants.TOTALTAX, order.getTotalTax().toString());
        for (OrderItem orderItem : order.getOrderItems()) {
            AmountItem amountItem = new AmountItemImpl();
            if (DiscreteOrderItem.class.isAssignableFrom(orderItem.getClass())) {
                amountItem.setDescription(((DiscreteOrderItem)orderItem).getSku().getDescription());
                amountItem.setSystemId(String.valueOf(((DiscreteOrderItem) orderItem).getSku().getId()));
            } else if (BundleOrderItem.class.isAssignableFrom(orderItem.getClass())) {
                amountItem.setDescription(((BundleOrderItem)orderItem).getSku().getDescription());
                amountItem.setSystemId(String.valueOf(((BundleOrderItem) orderItem).getSku().getId()));
            }
            amountItem.setShortDescription(orderItem.getName());
            amountItem.setPaymentInfo(paymentInfo);
            amountItem.setQuantity((long) orderItem.getQuantity());
            amountItem.setUnitPrice(orderItem.getPrice().getAmount());
            paymentInfo.getAmountItems().add(amountItem);
        }

        return paymentInfo;
    }

}
