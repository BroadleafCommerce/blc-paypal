# Paypal Module

Broadleaf Commerce currently offers integration with the PayPal Express API. See [Getting Started With Express Checkout](https://cms.paypal.com/us/cgi-bin/?cmd=_render-content&content_ID=developer/e_howto_api_ECGettingStarted) for more information. This module allows users to complete their shopping experience using their PayPal account.

![PayPal Diagram](payment-paypal-diagram.png)

## How It Works
1. When a customer clicks the "Pay with PayPal" button on your checkout page, a request is sent to Broadleaf to initiate the Express Checkout flow.
2. Broadleaf will generate a request and call PayPal using the NVP API to verify that the order being sent to them is valid.
3. If the checkout request is valid, PayPal will send over a redirect URL.
4. Broadleaf will then relay that URL to the customer's browser.
5. The customer will then be redirected to PayPal's site to authenticate and review the transaction. Once completed, PayPal would redirect the customer back to a URL in Broadleaf with an authorization token to complete checkout.
6. Broadleaf would then complete the order and settlement with PayPal and finally redirect the customer's browser to an order confirmation page.

> Note: PayPal requires you to conform to certain UI requirements in order to integrate with them. Please see [[Express Checkout User Interface Requirements | https://cms.paypal.com/us/cgi-bin/?cmd=_render-content&content_ID=developer/e_howto_api_ECUIRequirements]] for more information.

There are two ways to get started integrating the PayPal module into your web application. 
Broadleaf offers a [[PayPal Quick Start]] solution that allows developers to easily add PayPal functionality with little configuration.
If you have a complex payment workflow, please take a look at our [[PayPal Advance Configuration]]

Here's a feature list to help you decide which approach to choose:
(This list does not contain all the capabilities of the module)

### Feature List
| Feature                                                                    | Quick Start | Advanced |
| :------------------------------------------------------------------------- | :---------: | :------: |
| Customizable Logo and Color Scheme on PayPal                               | ✓           | ✓        |
| Single payments using PayPal or another credit card payment gateway. </td> | ✓           | ✓        |
| Partial payments using custom defined payment activities                   |             | ✓        |
| Capture shipping information in Broadleaf                                  | ✓           | ✓        |
| Capture shipping information on PayPal                                     |             | ✓        |
| Saving PayPal details to Broadleaf profile                                 |             | ✓        |
| Simple Authorization and Checkout                                          | ✓           | ✓        |
| Refund Transaction                                                         |             | ✓        |
| Void Transaction                                                           |             | ✓        |

