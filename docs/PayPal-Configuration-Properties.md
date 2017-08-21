# PayPal Express Checkout Environment Setup

## Prerequisites for Development/Staging Environments
To use the BroadleafCommerce PayPal payment functionality in a test environment, you must establish sandbox accounts with PayPal. This can be done here: https://developer.paypal.com/docs/classic/lifecycle/sb_create-accounts/.

The API credentials (api username, api password, signature) need to perform operations on the PayPal Express API are provided automatically when you create a sandbox business account. They can be gathered by viewing the details of the sandbox account.

## Property Confirgurations 
You will need to provide the following system properties to configure your PayPal API account details:

- gateway.paypal.expressCheckout.libVersion=78.0
- gateway.paypal.expressCheckout.serverUrl=https://api-3t.sandbox.paypal.com/nvp
- gateway.paypal.expressCheckout.userRedirectUrl=https://www.sandbox.paypal.com/cgi-bin/webscr
- gateway.paypal.expressCheckout.user=
- gateway.paypal.expressCheckout.password=
- gateway.paypal.expressCheckout.signature=
- gateway.paypal.expressCheckout.useRelativeUrls=false
- gateway.paypal.expressCheckout.shippingDisplayType=1
- gateway.paypal.expressCheckout.returnUrl=http://localhost:8080/paypal-express/return
- gateway.paypal.expressCheckout.cancelUrl=http://localhost:8080/paypal-express/cancel

> Note: Broadleaf allows you to create unique property files per environment (e.g. common.properties, local.properties, development.properties, integrationdev.properties, integrationqa.properties, staging.properties, and production.properties). Alternatively, you can gather these properties from the `BLC_SYSTEM_PROPERTY` table in your database using the `blSystemPropertiesService`. See the java docs for more details.

> Note: This module comes pre-configured with a Spring MVC controller with endpoints to handle the following return and cancel urls:
- `/paypal-express/return`
- `/paypal-express/cancel`

### Options for the shippingDisplayType
- 0: PayPal displays the shipping address that was passed in.
- 1: PayPal does not display the shipping fields at all.
- 2: PayPal will obtain the shipping address from the buyer's profile.

## Production Configurations
Please see the [[PayPal Production Configurations]] guide for details on configuring this integration in your production environment.
