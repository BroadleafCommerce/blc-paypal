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
This PayPal Module supports creating transactions via a typical Payments Flow.
  
> Note: The Billing Agreements and future payments via Reference Transactions have not yet been implemented.
  
## Template Updates

1. On your templates that render cart actions like a "Proceed to Checkout" button, you will need to place an empty `div` to render
PayPal's `Smart Checkout` button:

```html
    <div id="paypal-button-container">
    </div>
```

> Note: if you are using Broadleaf's Heat Clinic store, you can modify `cartOperations.html` and place this under the normal checkout button.

2. Include the following JS to invoke

```html
<script
        src="https://www.paypal.com/sdk/js?client-id=<YOUR CLIENT ID">
</script>
<script>
        paypal.Buttons({
            createOrder : function(data, actions) {
                return BLC.post({
                    url : '<YOUR CREATE ORDER URL (e.g. /paypal-checkout/create-order)>',
                    data : {
                        performCheckout : false
                    }
                }).then(function(res) {
                    return res.id;
                });
            },
            onApprove : function(data, actions) {
                console.log('onApprove');
                BLC.get({
                    url : '<YOUR RETURN URL> (e.g. /paypal-checkout/return)',
                    data : {
                        orderId : data.orderID,
                        payerId : data.payerID
                    }
                });
            }
        }).render('#paypal-button-container');
</script>
```

### Customizations

One of the more typical customizations you may wish to do revolves around passing the appropriate data to PayPal.
The service that handles translating a Broadleaf cart into the appropriate PayPal request is encapsulated in the `blPayPalPaymentService` spring component.
These components handles things like `Payer`, `Payee`, `Order`, and `ShippingDetail` construction. You may wish
to extend these services to send the appropriate values based on your business requirements.

## Done!
At this point, all the configuration should be complete and you are now ready to test your integration with PayPal Checkout.
Add something to your cart and proceed with checkout!
