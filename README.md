blc-paypal
=============

Broadleaf Commerce currently offers integration with the PayPal Express API.
This module allows users to complete their shopping experience using their PayPal account.

This module utilizes PayPal's NVP API defined here: https://developer.paypal.com/docs/classic/api/NVPAPIOverview/

You can also read up on how to customize the PayPal Express Page here: https://developer.paypal.com/docs/classic/express-checkout/integration-guide/ECCustomizing/


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
    for (FulfillmentGroup fg : order.getFulfillmentGroups()){
        for (FulfillmentGroupFee fee : fg.getFulfillmentGroupFees()){
            additionalFees = additionalFees.add(fee.getAmount());
        }
    }

    if (additionalFees.greaterThan(new Money(0))) {
        requestDTO.lineItem()
                .shortDescription("Additional Fees")
                .amount(additionalFees.getAmount())
                .quantity(1L)
                .done();
        amountItemSubtotal = amountItemSubtotal.add(additionalFees);
    }

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


