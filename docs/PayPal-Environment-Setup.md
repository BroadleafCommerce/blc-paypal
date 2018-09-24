# PayPal Checkout Environment Setup

## Prerequisites for Development/Staging Environments
To use the BroadleafCommerce PayPal payment functionality in a test environment, you must establish sandbox accounts with PayPal.

## Property Configurations 
You will need to provide the following system properties to configure your PayPal API account details:

```
#PayPal Client Side REST Checkout
gateway.paypal.checkout.rest.clientId=?
gateway.paypal.checkout.rest.secret=?
# Options are "sandbox" or "live"
gateway.paypal.checkout.rest.mode=?
gateway.paypal.checkout.rest.returnUrl=? (e.g. https://test.blc.localhost:8443/paypal-checkout/return)
gateway.paypal.checkout.rest.cancelUrl=? (e.g. https://test.blc.localhost:8443/paypal-checkout/cancel)
# Options are "sandbox" or "production"
gateway.paypal.smart.button.env=?
gateway.paypal.smart.button.payment.url=? (e.g. /paypal-checkout/create-payment)
gateway.paypal.smart.button.authorize.url=? (e.g. /paypal-checkout/return)
```

> Note: Broadleaf allows you to create unique property files per environment (e.g. common.properties, local.properties, development.properties, integrationdev.properties, integrationqa.properties, staging.properties, and production.properties). Alternatively, you can gather these properties from the `BLC_SYSTEM_PROPERTY` table in your database using the `blSystemPropertiesService`. See the java docs for more details.


## Logging
You may wish to turn on logging to help debug requests being made to PayPal during development. Set the following in your logback file:

```
    <logger name="com.paypal.base.rest.PayPalResource" level="DEBUG"/>
```