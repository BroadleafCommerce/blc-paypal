# PayPal Express Checkout Quick Start

Broadleaf Commerce offers an out-of-the-box PayPal solution that requires little configuration and is easily set up.

**You must have completed the [[PayPal Environment Setup]] before continuing**

## Prerequisites
Please familiarize yourself with the []PayPal Express Checkout documentation](https://developer.paypal.com/docs/integration/direct/express-checkout/integration-jsv4/) before proceeding.


## Add the Maven Dependency
Once you have established an account with PayPal, begin by including the PayPal Module dependency to your main pom.xml.

```xml
<dependency>
    <groupId>org.broadleafcommerce</groupId>
    <artifactId>broadleaf-paypal</artifactId>
    <version>2.7.1-SNAPSHOT</version>
</dependency>
```

Make sure to include the dependency in your `site` AND `admin` pom.xml as well (or just in the shared `core` project):

```xml
<dependency>
    <groupId>org.broadleafcommerce</groupId>
    <artifactId>broadleaf-paypal</artifactId>
</dependency>
```

## Template Updates for the Heat Clinic Demo Site

1. In `cartOperations.html`, replace the `paypal-payment-method-container`'s contents with

```html
<a th:href="@{/paypal-express/redirect?complete=false}">
    <img src="https://www.paypal.com/en_US/i/btn/btn_xpressCheckout.gif" align="center"/>
</a>
```

2. In `payPalPaymentMethodForm.html`, replace the `read-only` fragment's content with

```html
<div class="row">
    <div class="col-sm-3">
        <img src="https://www.paypal.com/en_US/i/btn/btn_xpressCheckout.gif" class="text-center-mobile"/>
    </div>
    <div class="col-sm-9" th:utext="#{checkout.paymentMethod.payPal.readOnly.message}"></div>
</div>
```

and the `form` fragment's content with

```html
<th:block th:if="${#paymentMethod.cartContainsThirdPartyPayment()}">
    <div class="row">
        <div class="col-sm-3">
            <img src="https://www.paypal.com/en_US/i/btn/btn_xpressCheckout.gif" class="text-center-mobile"/>
        </div>
        <div class="col-sm-9" th:utext="#{checkout.paymentMethod.payPal.edit.message}"></div>
    </div>
</th:block>

<th:block th:unless="${#paymentMethod.cartContainsThirdPartyPayment()}">
    <a th:href="@{/paypal-express/redirect?complete=false}" class="js-payPalPaymentMethodAction is-hidden"></a>
    <div class="row">
        <div class="col-sm-3">
            <img src="https://www.paypal.com/en_US/i/btn/btn_xpressCheckout.gif" class="text-center-mobile"/>
        </div>
        <div class="col-sm-9" th:utext="#{checkout.paymentMethod.payPal.message}"></div>
    </div>
</th:block>
```

## Configuration Properties
To configure your connection to the PayPal API, please complete the items outlined in the [[PayPal Configuration Properties]] document.

## Production Configurations
For information on preparing this integration for production, please reference the [[PayPal Production Configurations]] document.

## Done!
At this point, all the configuration should be complete and you are now ready to test your integration with PayPal Express Checkout.
Add something to your cart and proceed with checkout!
