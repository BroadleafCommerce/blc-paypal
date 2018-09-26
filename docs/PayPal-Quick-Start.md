# PayPal Checkout Quick Start

Broadleaf Commerce offers an out-of-the-box PayPal solution that requires little configuration and is easily set up.

**You must have completed the [[PayPal Environment Setup]] before continuing**

## Prerequisites
Please familiarize yourself with both the PayPal Checkout 
(https://developer.paypal.com/docs/checkout/) and the PayPal REST API documentation (https://developer.paypal.com/docs/api/overview/) before proceeding.

## Add the Maven Dependency
Once you have established an account with PayPal, begin by including the PayPal Module dependency to your main pom.xml.

```xml
<dependency>
    <groupId>org.broadleafcommerce</groupId>
    <artifactId>broadleaf-paypal</artifactId>
    <version>insert version here</version>
</dependency>
```

Make sure to include the dependency in your `site` AND `admin` pom.xml as well (or just in the shared `core` project):

```xml
<dependency>
    <groupId>org.broadleafcommerce</groupId>
    <artifactId>broadleaf-paypal</artifactId>
</dependency>
```

## Flows
This PayPal Module supports creating transactions via a typical Payments Flow as well as creating Billing Agreements and future payments
 via Reference Transactions. You can learn more about Reference Transactions and Billing Agreements via the REST API here: https://developer.paypal.com/docs/limited-release/reference-transactions/
  
> Note: The Reference Transaction Flow is a limited availability API and you will need to contact your PayPal representative to make sure
  it is enabled for your accounts before you can use it
  
In order to switch between a normal payment flow vs creating a billing agreement at checkout, you will just need to change a few configuration
parameters which will be outlined in the steps below.
  
## Template Updates

1. On your templates that render cart actions like a "Proceed to Checkout" button, you will need to place an empty `div` to render
PayPal's `Smart Checkout` button:

```html
    <div id="paypal-button">
    </div>
```

> Note: if you are using Broadleaf's Heat Clinic store, you can modify `cartOperations.html` and place this under the normal checkout button.

2. Include the following JS to invoke

```html
<script src="https://www.paypalobjects.com/api/checkout.js"></script>
<script type="text/javascript" th:inline="javascript">
    paypal.Button.render({
        env : [[${@environment.getProperty('gateway.paypal.smart.button.env')}]],

        // Specify the style of the button
        style: {
            layout: 'vertical',  // horizontal | vertical
            size:   'medium',    // medium | large | responsive
            shape:  'rect',      // pill | rect
            color:  'gold'       // gold | blue | silver | white | black
        },

        // Specify allowed and disallowed funding sources
        //
        // Options:
        // - paypal.FUNDING.CARD
        // - paypal.FUNDING.CREDIT
        // - paypal.FUNDING.ELV
        funding: {
            allowed: [
                paypal.FUNDING.CARD,
                paypal.FUNDING.CREDIT
            ],
            disallowed: []
        },

        payment : function(data, actions) {
            return BLC.post({
                url : [[${@environment.getProperty('gateway.paypal.smart.button.payment.url')}]],
                data : {
                    performCheckout : false
                }
            }).then(function(res) {
                return res.id;
            });
        },
        onAuthorize : function(data, actions) {
            BLC.get({
                url : [[${@environment.getProperty('gateway.paypal.smart.button.authorize.url')}]],
                data : {
                    paymentId : data.paymentID,
                    payerId : data.payerID
                }
            });
        }
    }, '#paypal-button');
</script>
```

> Note: If you are wishing to switch to the Billing Agreements Flow, you will want to change the `onAuthorize` callback method
  and define the following instead of `paymentId` and `payerId`
  
```
        onAuthorize : function(data, actions) {
            BLC.get({
                url : [[${@environment.getProperty('gateway.paypal.smart.button.authorize.url')}]],
                data : {
                    billingToken: data.billingToken,
                    paymentToken: data.paymentToken
                }
            });
        }
```


If you are using Broadleaf's Heat Clinic demo store, you may also wish to change the following templates to conform to this flow:

in `reviewStage.html` - change to:

```
    <blc:form id="PayPalCheckoutSubmissionForm" class="is-hidden" th:action="@{/paypal-checkout/checkout/complete(guest-checkout=${#request.getParameter('guest-checkout')})}" method="POST" novalidate="novalidate">
```

if you are switching to a billing agreements flow - change to:

```
<blc:form id="PayPalCheckoutSubmissionForm" class="is-hidden" th:action="@{/paypal-checkout/billing-agreement-token/complete(guest-checkout=${#request.getParameter('guest-checkout')})}" method="POST" novalidate="novalidate">
```

### Customizations

One of the more typical customizations you may wish to do revolves around passing the appropriate data to PayPal.
The service that handles translating a Broadleaf cart into the appropriate PayPal request is encapsulated in the following spring components `blPayPalPaymentService` and `blPayPalAgreementTokenService`.
These components handles things like `Payer`, `Transaction`, `Payment`, and `ShippingAddress` construction. You may wish
to extend these services to send the appropriate values based on your business requirements.

## Done!
At this point, all the configuration should be complete and you are now ready to test your integration with PayPal Checkout + Smart Payments.
Add something to your cart and proceed with checkout!
