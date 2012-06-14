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

import org.apache.commons.lang.StringUtils;
import org.broadleafcommerce.core.checkout.service.CheckoutService;
import org.broadleafcommerce.core.checkout.service.exception.CheckoutException;
import org.broadleafcommerce.core.checkout.service.workflow.CheckoutResponse;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.type.OrderStatus;
import org.broadleafcommerce.core.payment.domain.PaymentInfo;
import org.broadleafcommerce.core.payment.domain.PaymentResponseItem;
import org.broadleafcommerce.core.payment.domain.Referenced;
import org.broadleafcommerce.core.payment.service.CompositePaymentService;
import org.broadleafcommerce.core.payment.service.exception.PaymentException;
import org.broadleafcommerce.core.payment.service.type.PaymentInfoType;
import org.broadleafcommerce.core.payment.service.workflow.CompositePaymentResponse;
import org.broadleafcommerce.payment.service.module.PayPalPaymentModule;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.broadleafcommerce.vendor.paypal.service.payment.message.details.PayPalDetailsRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.details.PayPalDetailsResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalMethodType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalRefundType;

//import javax.annotation.Resource;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: elbertbautista
 * Date: 6/13/12
 * Time: 3:12 PM
 */
public class PayPalCheckoutServiceImpl implements PayPalCheckoutService {

    //@Resource(name="blCheckoutService")
    protected CheckoutService checkoutService;

    //@Resource(name="blCartService")
    protected CustomerService customerService;

    //@Resource(name="blCompositePaymentService")
    protected CompositePaymentService compositePaymentService;

    //@Resource(name="blPayPalModule")
    protected PayPalPaymentModule payPalPaymentModule;

    //The SetExpressCheckout API operation initiates an Express Checkout transaction.
    //see: https://www.x.com/developers/paypal/documentation-tools/api/setexpresscheckout-api-operation-nvp
    @Override
    public CompositePaymentResponse initiateExpressCheckout(Order order) throws PaymentException {
        return compositePaymentService.executePaymentForGateway(order, new PayPalPaymentInfoFactoryImpl());
    }

    //The DoExpressCheckoutPayment API operation completes an Express Checkout transaction. If you set up a billing agreement in your SetExpressCheckout API call, the billing agreement is created when you call the DoExpressCheckoutPayment API operation.
    //see: https://www.x.com/developers/paypal/documentation-tools/api/doexpresscheckoutpayment-api-operation-nvp
    @Override
    public CheckoutResponse completeExpressCheckout(String token, String payerId, Order order) throws CheckoutException {
        PaymentInfo payPalPaymentInfo = null;
        Map<PaymentInfo, Referenced> payments = new HashMap<PaymentInfo, Referenced>();
        for (PaymentInfo paymentInfo : order.getPaymentInfos()) {
            if (paymentInfo.getType() == PaymentInfoType.PAYPAL) {
                //There should only be one payment info of type paypal in the order
                paymentInfo.getAdditionalFields().put(MessageConstants.PAYERID, payerId);
                paymentInfo.getAdditionalFields().put(MessageConstants.TOKEN, token);
                payments.put(paymentInfo, paymentInfo.createEmptyReferenced());
                payPalPaymentInfo = paymentInfo;
                break;
            }
        }

        order.setStatus(OrderStatus.SUBMITTED);
        order.setSubmitDate(Calendar.getInstance().getTime());

        CheckoutResponse checkoutResponse = checkoutService.performCheckout(order, payments);

        PaymentResponseItem responseItem = checkoutResponse.getPaymentResponse().getResponseItems().get(payPalPaymentInfo);
        if (responseItem.getTransactionSuccess()) {
            //Fill out a few customer values for anonymous customers
            Customer customer = order.getCustomer();
            if (StringUtils.isEmpty(customer.getFirstName())) {
                customer.setFirstName(responseItem.getCustomer().getFirstName());
            }
            if (StringUtils.isEmpty(customer.getLastName())) {
                customer.setLastName(responseItem.getCustomer().getLastName());
            }
            if (StringUtils.isEmpty(customer.getEmailAddress())) {
                customer.setEmailAddress(responseItem.getCustomer().getEmailAddress());
            }
            customerService.saveCustomer(customer, false);
        }


        return checkoutResponse;

    }

    //The GetExpressCheckoutDetails API operation obtains information about an Express Checkout transaction.
    //see: https://www.x.com/developers/paypal/documentation-tools/api/getexpresscheckoutdetails-api-operation-nvp
    @Override
    public PayPalDetailsResponse getExpressCheckoutDetails(String token) throws PaymentException {
        PayPalDetailsRequest detailsRequest = new PayPalDetailsRequest();
        detailsRequest.setToken(token);
        detailsRequest.setMethodType(PayPalMethodType.DETAILS);
        return payPalPaymentModule.getExpressCheckoutDetails(detailsRequest);
    }

    //The RefundTransaction API operation issues a refund to the PayPal account holder associated with a transaction.
    //see: https://www.x.com/developers/paypal/documentation-tools/api/refundtransaction-api-operation-nvp
    @Override
    public CompositePaymentResponse refundTransaction(String transactionId, Order order) throws PaymentException {
        Map<PaymentInfo, Referenced> payments = new HashMap<PaymentInfo, Referenced>();
        for (PaymentInfo paymentInfo : order.getPaymentInfos()) {
            if (paymentInfo.getType() == PaymentInfoType.PAYPAL) {
                //There should only be one payment info of type paypal in the order
                paymentInfo.getAdditionalFields().put(MessageConstants.TRANSACTIONID, transactionId);
                paymentInfo.getAdditionalFields().put(MessageConstants.REFUNDTYPE, PayPalRefundType.FULL.getType());
                payments.put(paymentInfo, paymentInfo.createEmptyReferenced());
                break;
            }
        }

        return compositePaymentService.executePayment(order, payments);
    }

}
