# PayPal Express Checkout Quick Start

Broadleaf Commerce offers an out-of-the-box PayPal solution that requires little configuration and is easily set up.

**You must have completed the [[PayPal Environment Setup]] before continuing**

## Adding PayPal Express Checkout Support

1. Add the PayPal Express Buttons to your site:

If you are using the Heat Clinic Demo Site:

- replace the following code in cart.html

```html
<form blc:null_payment_hosted_action="${paymentRequestDTO}" complete_checkout="${false}" method="POST">
...
</form>
```

with

```html
<a th:href="@{/paypal-express/redirect}">
    <img src="https://www.paypal.com/en_US/i/btn/btn_xpressCheckout.gif" align="left" style="margin-right:7px;"/>
</a>
```

- replace the following code in paymentMethodForm.html

```html
<img th:src="@{/img/paypal.gif}" alt="Pay with Paypal" width="100" />
```

with

```html
<a th:href="@{/paypal-express/redirect?complete=true}">
    <img src="https://www.paypal.com/en_US/i/btn/btn_xpressCheckout.gif" align="left" style="margin-right:7px;"/>
</a>
```

## Done!
At this point, all the configuration should be complete and you are now ready to test your integration with PayPal Express Checkout.
Add something to your cart and proceed with checkout.
