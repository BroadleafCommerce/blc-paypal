# PayPal Advance Configuration

Broadleaf allows you to customize many aspects of your PayPal integration.

**You must have completed the [[PayPal Environment Setup]] before continuing**

## Configuring PayPal Payments

You will need to declare the following Spring beans in your application context:

```xml
<bean id="blPayPalPaymentService" class="org.broadleafcommerce.core.payment.service.PaymentServiceImpl">
    <property name="paymentModule" ref="blPayPalModule"/>
</bean>

<bean id="blPayPalModule" class="org.broadleafcommerce.payment.service.module.PayPalPaymentModule">
    <property name="payPalPaymentService" ref="blPayPalVendorOrientedPaymentService"/>
</bean>

<bean id="blPayPalVendorOrientedPaymentService" class="org.broadleafcommerce.vendor.paypal.service.payment.PayPalPaymentServiceImpl">
    <property name="serverUrl" value="${paypal.api.url}"/>
    <property name="failureReportingThreshold" value="1"/>
    <property name="requestGenerator">
        <bean class="org.broadleafcommerce.vendor.paypal.service.payment.PayPalRequestGeneratorImpl">
            <property name="libVersion" value="${paypal.version}"/>
            <property name="password" value="${paypal.password}"/>
            <property name="user" value="${paypal.user}"/>
            <property name="signature" value="${paypal.signature}"/>
            <property name="returnUrl" value="http://localhost:8080/mycompany/paypal/process"/>
            <property name="cancelUrl" value="http://localhost:8080/mycompany/cart"/>
            <property name="shippingDisplayType" value="${paypal.shipping.display}"/>
            <property name="additionalConfig">
                <map>
                    <entry key="HDRIMG" value="http://localhost:8080/mycompany/images/logo.png"/>
                    <entry key="HDRBORDERCOLOR" value="333333"/>
                    <entry key="HDRBACKCOLOR" value="669933"/>
                    <entry key="PAYFLOWCOLOR" value="B58253"/>
                </map>
            </property>
        </bean>
    </property>
    <property name="responseGenerator">
        <bean class="org.broadleafcommerce.vendor.paypal.service.payment.PayPalResponseGeneratorImpl">
            <property name="userRedirectUrl" value="${paypal.user.redirect.url}"/>
        </bean>
    </property>
</bean>
```
> Note: The [[PayPal Quick Start]] solution offers a default application context with these beans already defined and can be used as a reference. Please see `bl-paypal-applicationContext.xml`

* `serverUrl` - the PayPal API URL.
* `failureReportingThreshold` - used by [[QoS | QoS Configuration]] to determine how many times the service should fail before it is considered to be "down".
* `libVersion` - the PayPal API lib version. This is pre-configured per environment in Broadleaf.
* `password` - the PayPal API password.
* `user` - the PayPal API username.
* `signature` - the PayPal API signature.
* `returnUrl` - the destination in your app you want the user to come to after he/she has completed their experience on PayPal's site.
* `cancelUrl` - the destination in your app if he/she cancels the payment on PayPal's site.
* `shippingDisplayType` - this can be the following values:                
    * 0 : PayPal displays the shipping address passed in. This is taken from fulfillmentGroup.getAddress(). This is useful if you want to restrict the countries you ship to in PayPal's Merchant Console.
    * 1 : PayPal does not display the shipping fields at all. (Default)
    * 2 : PayPal will obtain the shipping address from the buyer's profile.
* `additionalConfig` - You have an opportunity to configure a logo image and some CSS values that affect the visual experience for the user on PayPal's site.
* `userRedirectUrl` - the PayPal API user redirect URL. This is pre-configured per environment in Broadleaf.

See [[PayPal Environment Setup]] to learn how to configure the variable properties.

You now need to add the PayPal activity to the `blAuthorizeAndDebitWorkflow`. This is done by configuring the Spring Bean like this:

```xml
<bean id="blAuthorizeAndDebitWorkflow" class="org.broadleafcommerce.core.workflow.SequenceProcessor">
    <property name="processContextFactory">
        <bean class="org.broadleafcommerce.core.payment.service.workflow.PaymentProcessContextFactory">
            <property name="paymentActionType" value="AUTHORIZEANDDEBIT"/>
        </bean>
    </property>
    <property name="activities">
        <list>
            <bean class="org.broadleafcommerce.core.payment.service.workflow.PaymentActivity">
                <property name="paymentService" ref="blPayPalPaymentService"/>
                <property name="userName" value="web"/>
            </bean>
            <bean class="org.broadleafcommerce.core.payment.service.workflow.PaymentActivity">
                <property name="paymentService" ref="blCreditCardService"/>
                <property name="userName" value="web"/>
            </bean>
        </list>
    </property>
    <property name="defaultErrorHandler" ref="blDefaultErrorHandler"/>
</bean>
```

## Customizing the PayPalCheckoutService

Broadleaf provides the `PayPalCheckoutService`, an abstraction layer on top of the payment workflow that aids in creating
the objects necessary for completing a successful checkout. The `blPayPalCheckoutService` can be overridden using a custom implementation.
This API is called from the `BroadleafPayPalController` used in the [[PayPal Quick Start]] solution.

If you have set `${paypal.shipping.display}` to "2", PayPal will capture or use the buyer's address on their profile. 
This information can then be obtained using `getExpressCheckoutDetails`. Once obtained, you can save this information to the profile or Fulfillment Group as you wish. 
See the code examples below for more details.


## Manually Configuring the Presentation Layer

It is up to you to choose the presentation layer approach that best fits your needs, but regardless of the approach, 
you will be required at some point to compile the [[PaymentInfo | https://github.com/BroadleafCommerce/BroadleafCommerce/blob/master/core/broadleaf-framework/src/main/java/org/broadleafcommerce/core/payment/domain/PaymentInfo.java]] information 
to the order before calling performCheckout on the CheckoutService. 
Most Broadleaf Commerce users will choose Spring MVC and will likely implement their own CheckoutController. 

In this example, we will show you how a Spring MVC Controller might be structured to handle calling the PayPal Module. Begin by adding the PayPal Checkout button to your shopping cart page following the guidelines outlined here: [[Express Checkout User Interface Requirements | https://cms.paypal.com/us/cgi-bin/?cmd=_render-content&content_ID=developer/e_howto_api_ECUIRequirements]] (In the example below, the PayPal button links to "/paypal/checkout").
If your implementation does not require that much customization, consider extending the `BroadleafPayPalController`.
> Note: This example does not use the PayPalCheckoutService in order to demonstrate another way of executing the workflow. 

```java

    //this service is backed by the entire payment workflow configured in application context
    //it is the entry point for engaging the payment workflow
    @Resource(name="blCompositePaymentService")
    protected CompositePaymentService compositePaymentService;
    
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/paypal/checkout", method = {RequestMethod.GET})
    public String paypalCheckout(@ModelAttribute CheckoutForm checkoutForm,
                           BindingResult errors,
                           ModelMap model,
                           HttpServletRequest request,
                           HttpServletResponse response) throws IOException {
        Customer currentCustomer = customerState.getCustomer(request);                   
        final Order order = cartService.findCartForCustomer(currentCustomer);
        Map<PaymentInfo, Referenced> payments = new HashMap<PaymentInfo, Referenced>();

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
            } else if (BundleOrderItem.class.isAssignableFrom(orderItem.getClass()) {
                amountItem.setDescription(((BundleOrderItem)orderItem).getSku().getDescription());
                amountItem.setSystemId(String.valueOf(((BundleOrderItem) orderItem).getSku().getId()));
            }
            amountItem.setShortDescription(orderItem.getName());
            amountItem.setPaymentInfo(paymentInfo);
            amountItem.setQuantity((long) orderItem.getQuantity());
            amountItem.setUnitPrice(orderItem.getPrice().getAmount());
            paymentInfo.getAmountItems().add(amountItem);
        }
        payments.put(paymentInfo, paymentInfo.createEmptyReferenced());
        List<PaymentInfo> paymentInfos = new ArrayList<PaymentInfo>();
        paymentInfos.add(paymentInfo);
        order.setPaymentInfos(paymentInfos);

        try {
            CompositePaymentResponse compositePaymentResponse = compositePaymentService.executePayment(order, payments);
            PaymentResponseItem responseItem = compositePaymentResponse.getPaymentResponse().getResponseItems().get(paymentInfo);
            if (responseItem.getTransactionSuccess()) {
                return "redirect:" + responseItem.getAdditionalFields().get(MessageConstants.REDIRECTURL);
            }
        } catch (PaymentException e) {
            LOG.error("Cannot perform checkout", e);
        }

        return null;
    }
```

Now let's add a method to our controller to handle the callback from PayPal back to Broadleaf. Notice that the callback from PayPal will have a request parameters called `PayerID` and `token`

```java
    @RequestMapping(value="/paypal/process", method = {RequestMethod.GET})
    public String paypalProcess(ModelMap model,
                                @RequestParam String token,
                                @RequestParam("PayerID") String payerID,
                                CheckoutForm checkoutForm,
                                HttpServletRequest request) {
        Order order = retrieveCartOrder(request, model);
        PaymentInfo payPalPaymentInfo = null;
        Map<PaymentInfo, Referenced> payments = new HashMap<PaymentInfo, Referenced>();
        for (PaymentInfo paymentInfo : order.getPaymentInfos()) {
            if (paymentInfo.getType() == PaymentInfoType.PAYPAL) {
                //There should only be one payment info of type paypal in the order
                paymentInfo.getAdditionalFields().put(MessageConstants.PAYERID, payerID);
                paymentInfo.getAdditionalFields().put(MessageConstants.TOKEN, token);
                payments.put(paymentInfo, paymentInfo.createEmptyReferenced());
                payPalPaymentInfo = paymentInfo;
                break;
            }
        }
        
        order.setStatus(OrderStatus.SUBMITTED);
        order.setSubmitDate(Calendar.getInstance().getTime());

        CheckoutResponse checkoutResponse;
        try {
            /*
                Grab some details about the transaction - useful if you want to
                retrieve address information for the user
             */
            //PayPalDetailsRequest detailsRequest = new PayPalDetailsRequest();
            //detailsRequest.setToken(token);
            //detailsRequest.setMethodType(PayPalMethodType.DETAILS);
            //PayPalDetailsResponse detailsResponse = payPalPaymentModule.getExpressCheckoutDetails(detailsRequest);

            checkoutResponse = checkoutService.performCheckout(order, payments);

            PaymentResponseItem responseItem = checkoutResponse.getPaymentResponse().getResponseItems().get(payPalPaymentInfo);
            if (responseItem.getTransactionSuccess()) {
                //Fill out a few customer values for anonymous customers
                Customer customer = order.getCustomer();
                if (StringUtils.isEmpty(customer.getFirstName())) {
                    customer.setFirstName(checkoutForm.getBillingAddress().getFirstName());
                }
                if (StringUtils.isEmpty(customer.getLastName())) {
                    customer.setLastName(checkoutForm.getBillingAddress().getLastName());
                }
                if (StringUtils.isEmpty(customer.getEmailAddress())) {
                    customer.setEmailAddress(order.getEmailAddress());
                }
                customerService.saveCustomer(customer, false);

                return receiptView != null ? "redirect:" + receiptView : "redirect:/orders/viewOrderConfirmation.htm?orderNumber=" + order.getOrderNumber();
            }
        } catch (Exception e) {
            LOG.error("Cannot perform checkout", e);
        }

        return null;
    }
```

