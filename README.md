blc-paypal
=============

Broadleaf Commerce currently offers integration with the PayPal Express API.
This module allows users to complete their shopping experience using their PayPal account.

This module utilizes PayPal's NVP API defined here: https://developer.paypal.com/docs/classic/api/NVPAPIOverview/

You can also read up on how to customize the PayPal Express Page here: https://developer.paypal.com/docs/classic/express-checkout/integration-guide/ECCustomizing/

When integrating Express Checkout, PayPal requires you to conform to certain requirements regarding placement,
such as offering the option as both a Checkout option as well as a Payment Option. Please see documentation here: https://developer.paypal.com/docs/classic/express-checkout/integration-guide/ECUIRequirements/


Notes:
## Showing Line Items on PayPal Express's Checkout Screen:

You must override the OrderToPaymentRequestDTOService implementation to construct
the LineItemDTO's on the PaymentRequestDTO in your application, so that it conforms to the API restrictions of this gateway.
The NVP API validates that the amounts of the line item DTO's that you pass in equal the Subtotal amount
that you passed in.

Your algorithm might look something like this:
```java
    Money amountItemSubtotal = new Money(0);
    List<OrderItem> orderItems = order.getOrderItems();
    //Adds the order items as line items for the PayPal request.
    for (OrderItem orderItem : orderItems) {
        requestDTO.lineItem()
                .shortDescription(orderItem.getName())
                .systemId(orderItem.getId().toString())
                .amount(orderItem.getTotalPrice().toString())
                .quantity(orderItem.getQuantity() + "")
                .done();
        amountItemSubtotal = amountItemSubtotal.add(orderItem.getPrice().multiply(orderItem.getQuantity()));
    }

    Money additionalFees = new Money(0);
    //Collects the sum of the Fulfillment Group fees, if any, to be added to the PayPal request.
    for (FulfillmentGroup fg : order.getFulfillmentGroups()){
        for (FulfillmentGroupFee fee : fg.getFulfillmentGroupFees()){
            additionalFees = additionalFees.add(fee.getAmount());
        }
    }
    
    //If there are fees, this adds it to the PayPal request as a line item.
    if (additionalFees.greaterThan(new Money(0))) {
        requestDTO.lineItem()
                .shortDescription("Additional Fees")
                .amount(additionalFees.getAmount())
                .quantity(1L)
                .done();
        amountItemSubtotal = amountItemSubtotal.add(additionalFees);
    }

    //If there are order level offers, this adds it as a line item with a negative amount
    //for the PayPal request.
    if (order.getTotalAdjustmentsValue() != null &&
      order.getTotalAdjustmentsValue().greaterThan(new Money(0))){
        requestDTO.lineItem()
                .shortDescription("Promotion Code")
                .amount(order.getTotalAdjustmentsValue().getAmount().negate())
                .quantity(1L)
                .done();
        amountItemSubtotal = amountItemSubtotal.subtract(order.getTotalAdjustmentsValue());
    }

    // add additional payment methods like customer credit or gift cards etc...

    requestDTO.subTotal(amountItemSubtotal.toString());

```


### Differences in PayPal Products

Here is some helpful information regarding the different products that PayPal provides:

#### PayPal Express Checkout

Express Checkout gives the customer two options: They can jump over to the PayPal site to login to their account BEFORE completing checkout on your store (which allows them to select their address information there and never have to re­type their address details on YOUR site, thus the "express" part of the transaction) and then choosing shipping choices and discounts/coupons etc before completing the order ....... OR they can go to the PayPal site to login to their account AFTER making shipping/payment/coupon selections on your site (and creating an account on your store and typing their address info on your store), much like they do with Standard.
With Express Checkout, the customer can pay without having a PayPal account (as long as you have "PayPal Account Optional" enabled inside your PayPal merchant account settings: Profile­>Hosted Payment Settings­>Website Payments Preferences­>PayPal Account Optional = ON), except for merchants/customers in China.
Express Checkout has all the same features as Website Payments Standard, but is more reliable because it completes the transaction directly while the customer is actively engaged on your site. It supports all the currencies, payment methods, etc, just the same, but more efficiently. There is no monthly fee for using Express Checkout.
PayPal sees Express Checkout as a payment option that's offered in addition to other payment choices such as a credit card gateway, and that adding Express is a way to allow PayPal members a very quick and easy way to pay using their PayPal account. PayPal also believes Express Checkout improves conversions/sales. Many merchants do use Express Checkout as their sole payment processing option, even without a credit card gateway.

#### Website Payments Pro

appears to the customer only as a couple fields to enter their credit card number directly on your website. They have no idea that in the background you're processing their card via PayPal. They have to make an account on your site, and supply the address details, but once they confirm the order, the payment is collected immediately and the order saved. It doesn't rely on IPN to release the order. However, it does store any transaction updates done on the PayPal end such as refunds etc as long as IPNs can be received by your server. Website Payments Pro is currently only offered in the USA, UK, and Canada. A monthly service charge applies, and there is an account application process and credit check to complete before the feature can be activated on your account. PayPal Express Checkout must be enabled in order for Website Payments Pro to be offered on your site.
PayPal sees Website Payments Pro as a payment gateway for handling credit cards. That's exactly what it is. Coupled with Express Checkout, it gives your customers the maximum amount of choice about how to pay: either by credit card directly on your site, or by using their PayPal account to submit payment.

#### PayPal Payments Standard

takes the customer to PayPal's site AFTER the ENTIRE checkout in order to make payment. The customer can pay without having a PayPal account (depending on what country YOU are in ... PayPal limits that feature for some countries). After the payment is completed, your store is notified of the completed payment, after which time the order is stored in your database. When paying with PayPal Standard, if the customer pays without making a PayPal account and closes their browser when presented with the invitation to make one, your store sits in limbo and never gets the order (due to a bug in PayPal's logic, which they've not fixed yet). If they DO have OR make a PayPal account, you get the notification and the order is stored in your database.
**IF there is any problem in PayPal's ability to communicate to your server, you will never see the order in your store (or receive the confirmation email from your store), because it relies entirely on PayPal's server being able to talk to your server in order to store the order.

#### PayPal Payments Advanced

is a newer service offered starting in 2012, and requires an add-on module in order to use it. Details about this service can be found on the PayPal website. There is a monthly fee to use this service, which is only available in USA.

#### Payflow Pro

is essentially *only* a merchant account. Transactions conducted via Payflow Pro (for US Merchants) do not appear in your PayPal account ... instead, they are forwarded directly to your merchant bank account. Basically, Payflow Pro is just like any other traditional payment gateway (akin to Authorize.net etc). In North America you can connect the Payflow Pro service to your own merchant bank account. In the UK, the Payflow Pro service is actually bundled as a hybrid service with Website Payments Pro, connecting all the transactions to your UK PayPal account, and all monies are deposited to your PayPal account, instead of directly to your bank account. There is a monthly fee for using this service.


