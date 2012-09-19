/*
 * Copyright 2008-2012 the original author or authors.
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

import org.apache.commons.lang.StringUtils;
import org.broadleafcommerce.core.checkout.service.CheckoutService;
import org.broadleafcommerce.core.checkout.service.exception.CheckoutException;
import org.broadleafcommerce.core.checkout.service.workflow.CheckoutResponse;
import org.broadleafcommerce.core.order.domain.Order;
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

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * User: elbertbautista
 * Date: 6/13/12
 *
 * This class provides an abstraction layer on top of the CompositePaymentService and the PayPalPaymentModule, allowing developers
 * to easily call the most frequently-used methods.
 *
 * For custom implementation, this class should be extended or custom calls to the CompositePaymentService should be utilized.
 *
 */
public class PayPalCheckoutServiceImpl implements PayPalCheckoutService {

    @Resource(name="blCheckoutService")
    protected CheckoutService checkoutService;

    @Resource(name="blCustomerService")
    protected CustomerService customerService;

    @Resource(name="blCompositePaymentService")
    protected CompositePaymentService compositePaymentService;

    @Resource(name="blPayPalModule")
    protected PayPalPaymentModule payPalPaymentModule;

    /**
     * Initiates an Express Checkout transaction by invoking the SetExpressCheckout API.
     * This will verify the PayPal PaymentInfo on the order when sent to the gateway.
     * see: https://www.x.com/developers/paypal/documentation-tools/api/setexpresscheckout-api-operation-nvp
     *
     * @param order - The order
     * @return CompositePaymentResponse - the response from PayPal.
     *         In most cases you will need to check if the transaction was a success,
     *         and redirect the user to: responseItem.getAdditionalFields().get(MessageConstants.REDIRECTURL)
     */
    @Override
    public CompositePaymentResponse initiateExpressCheckout(Order order) throws PaymentException {
        return compositePaymentService.executePaymentForGateway(order, new PayPalPaymentInfoFactoryImpl());
    }

    /**
     * Completes an Express Checkout transaction by invoking the DoExpressCheckout API.
     * If you set up a billing agreement in your SetExpressCheckout API call,
     * the billing agreement is created when you call the DoExpressCheckout API operation.
     *
     * This call executes a complete checkout by calling checkoutService.performCheckout().
     * Make sure the payments and fulfillment groups are fully set up before invoking.
     * see: https://www.x.com/developers/paypal/documentation-tools/api/doexpresscheckoutpayment-api-operation-nvp
     *
     * Note: this method must be passed in an order already initialized for checkout
     * i.e. the order status set to SUBMITTED, the order number set, and the date submitted set.
     *
     * @param order   - The order
     * @param token   - A PayPal parameter sent back after invoking SetExpressCheckout
     * @param payerId - A PayPal parameter sent back after invoking SetExpressCheckout
     * @return CheckoutResponse - the response from PayPal.
     */
    @Override
    public CheckoutResponse completeExpressCheckout(String token, String payerId, Order order) throws CheckoutException {
        PaymentInfo payPalPaymentInfo = null;

        PayPalDetailsRequest detailsRequest = new PayPalDetailsRequest();
        detailsRequest.setMethodType(PayPalMethodType.DETAILS);
        detailsRequest.setToken(token);

        try {
            PayPalDetailsResponse response = payPalPaymentModule.getExpressCheckoutDetails(detailsRequest);
            if (response != null && response.getPaymentDetails() != null) {
                if (!order.getTotal().getAmount().equals(response.getPaymentDetails().getAmount().getAmount())) {
                    throw new CheckoutException("The Order Total does not match the total from PayPal", null);
                }
            }
        } catch (PaymentException e) {
            throw new CheckoutException("Unable to retrieve Express Checkout Details", e, null);
        }


        Map<PaymentInfo, Referenced> payments = new HashMap<PaymentInfo, Referenced>();
        for (PaymentInfo paymentInfo : order.getPaymentInfos()) {
            if (PaymentInfoType.PAYPAL.equals(paymentInfo.getType())) {
                //There should only be one payment info of type paypal in the order
                paymentInfo.getAdditionalFields().put(MessageConstants.PAYERID, payerId);
                paymentInfo.getAdditionalFields().put(MessageConstants.TOKEN, token);
                payments.put(paymentInfo, paymentInfo.createEmptyReferenced());
                payPalPaymentInfo = paymentInfo;
                break;
            }
        }

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

    /**
     * Obtains information about an Express Checkout transaction by invoking the GetExpressCheckoutDetails API.
     * This call is useful when wanting to obtain information from the PayPal transaction, such as the Shipping Address.
     * see: https://www.x.com/developers/paypal/documentation-tools/api/getexpresscheckoutdetails-api-operation-nvp
     *
     * @param token   - A PayPal parameter sent back after invoking SetExpressCheckout
     * @return PayPalDetailsResponse - the response from PayPal.
     */
    @Override
    public PayPalDetailsResponse getExpressCheckoutDetails(String token) throws PaymentException {
        PayPalDetailsRequest detailsRequest = new PayPalDetailsRequest();
        detailsRequest.setToken(token);
        detailsRequest.setMethodType(PayPalMethodType.DETAILS);
        return payPalPaymentModule.getExpressCheckoutDetails(detailsRequest);
    }

    /**
     * Refunds the PayPal account holder associated with a transaction by invoking the RefundTransaction API.
     * see: https://www.x.com/developers/paypal/documentation-tools/api/refundtransaction-api-operation-nvp
     *
     * @param order - the Order
     * @param transactionId   - A PayPal parameter sent back after invoking SetExpressCheckout
     * @return CompositePaymentResponse - the response from PayPal.
     */

    @Override
    public CompositePaymentResponse refundTransaction(String transactionId, Order order) throws PaymentException {
        Map<PaymentInfo, Referenced> payments = new HashMap<PaymentInfo, Referenced>();
        for (PaymentInfo paymentInfo : order.getPaymentInfos()) {
            if (PaymentInfoType.PAYPAL.equals(paymentInfo.getType())) {
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
