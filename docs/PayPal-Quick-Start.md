# PayPal Express Checkout Quick Start

Broadleaf Commerce offers an out-of-the-box PayPal solution that requires little configuration and is easily set up.

**You must have completed the [[PayPal Environment Setup]] before continuing**

## Adding PayPal Express Checkout Support

1. Add the following component scan in your `applicationContext-servlet.xml` in your `site` module

```xml
    <context:component-scan base-package="org.broadleafcommerce.vendor.paypal"/>
```

2. Add the PayPal Express component scan in your `applicationContext.xml` of your `core` module

3. Declare the bean `org.broadleafcommerce.vendor.paypal.service.payment.PayPalExpressPaymentGatewayType` so that Spring can initalize the Broadleaf Enumeration for both Site and Admin

4. Inject the PayPalExpressCheckoutLinkProcessor into the Broadleaf Thymeleaf Dialect Processors `blDialectProcessors` in your `applicationContext.xml` of your `core` module

5. Inject the `blPayPalExpressConfigurationService` bean into the `blPaymentGatewayConfigurationServices` list in your `applicationContext.xml` of your `core` module

Example:

```xml
    <!-- PayPal Express Checkout -->
    <context:component-scan base-package="org.broadleafcommerce.payment.service.gateway"/>
    <context:component-scan base-package="org.broadleafcommerce.vendor.paypal"/>
    <bean class="org.broadleafcommerce.vendor.paypal.service.payment.PayPalExpressPaymentGatewayType"/>

    <bean id="blPayPalExpressCheckoutLinkProcessor" class="org.broadleafcommerce.vendor.paypal.web.processor.PayPalExpressCheckoutLinkProcessor"/>
    <bean id="myCompanyPaymentProcessors" class="org.springframework.beans.factory.config.SetFactoryBean">
        <property name="sourceSet">
            <set>
                <ref bean="blPayPalExpressCheckoutLinkProcessor"/>
            </set>
        </property>
    </bean>
    <bean class="org.broadleafcommerce.common.extensibility.context.merge.LateStageMergeBeanPostProcessor">
        <property name="collectionRef" value="myCompanyPaymentProcessors"/>
        <property name="targetRef" value="blDialectProcessors"/>
    </bean>

    <bean id="mySampleConfigurationServices" class="org.springframework.beans.factory.config.ListFactoryBean">
        <property name="sourceList">
            <list>
                <ref bean="blPayPalExpressConfigurationService"/>
            </list>
        </property>
    </bean>
    <bean class="org.broadleafcommerce.common.extensibility.context.merge.LateStageMergeBeanPostProcessor">
        <property name="collectionRef" value="mySampleConfigurationServices"/>
        <property name="targetRef" value="blPaymentGatewayConfigurationServices"/>
    </bean>
```

5. Add the PayPal Express Buttons to your site:

If you are using the Heat Clinic Demo Site:

- replace the following code in cart.html

```html
<form blc:null_payment_hosted_action="${paymentRequestDTO}" complete_checkout="${false}" method="POST">
...
</form>
```

with

```html
<a blc:paypal_express_link="${paymentRequestDTO}" complete_checkout="${false}">
    <img src="https://www.paypal.com/en_US/i/btn/btn_xpressCheckout.gif" align="left" style="margin-right:7px;"/>
</a>
```

- replace the following code in paymentMethodForm.html

```html
<img th:src="@{/img/paypal.gif}" alt="Pay with Paypal" width="100" />
```

with

```html
<a blc:paypal_express_link="${paymentRequestDTO}" complete_checkout="${true}">
    <img src="https://www.paypal.com/en_US/i/btn/btn_xpressCheckout.gif" align="left" style="margin-right:7px;"/>
</a>
```
