# PayPal Quick Start

Broadleaf Commerce offers an out-of-the-box PayPal solution that requires little configuration and is easily set up. For a more customized solution, please see [[PayPal Advance Configuration]].

**You must have completed the [[PayPal Environment Setup]] before continuing**

## 1) Adding PayPal Support

First, you will need to add the quick-start PayPal application context `bl-paypal-applicationContext.xml` to your web.xml.
Your `patchConfigLocations` should look something like this:

```xml
    <context-param>
        <param-name>patchConfigLocation</param-name>
        <param-value>
            classpath:/bl-open-admin-contentClient-applicationContext.xml
            classpath:/bl-cms-contentClient-applicationContext.xml
            classpath:/bl-paypal-applicationContext.xml
            classpath:/applicationContext.xml
            /WEB-INF/applicationContext-datasource.xml
            /WEB-INF/applicationContext-email.xml
            /WEB-INF/applicationContext-security.xml
            /WEB-INF/applicationContext.xml
        </param-value>
    </context-param>
```

> IMPORTANT: The order in which the application contexts are specified matters to the merge process. Make sure the "bl-paypal-applicationContext.xml" is specified BEFORE your applicationContext.xml that defines your "blConfiguration" bean. If you have customized your Runtime Environment Properties or Checkout Workflow, make sure to add this file in the appropriate order so that Broadleaf will pick up the the correct bean.

## 2) Create a PayPal Controller

Next, you will need to create a basic controller that extends `BroadleafPayPalController` to provide default `@RequestMappings` for your application.
Here is an example controller with the minimum amount of code needed to get PayPal integrated.

```java
@Controller
public class PayPalController extends BroadleafPayPalController {

    //This is the URL that will initiate the checkout process with PayPal.
    @RequestMapping("/paypal/checkout")
    public String paypalCheckout(HttpServletRequest request) throws PaymentException {
        return super.paypalCheckout(request);
    }

    //This is the URL that PayPal will redirect back to on callback.
    //This should match ${paypal.return.url} in your properties file.
    //For example:  ${paypal.return.url}=http://localhost:8080/mycompany/paypal/process
    @RequestMapping("/paypal/process")
    public String paypalProcess(HttpServletRequest request, HttpServletResponse response, Model model, @RequestParam String token, @RequestParam("PayerID") String payerID) throws CheckoutException, PricingException {
        return super.paypalProcess(request, response, model, token, payerID);
    }

}
```

## 3) Add a PayPal button to your page

Finally, create a link in your checkout flow that points to the controller("/paypal/checkout") you just created above.
Your page may contain a button like this:

```html
<a href="/mycompany/paypal/checkout">
    <img src="img/paypal.gif" alt="Pay with Paypal" width="100" />
</a>
```

## Done!
> Note: For troubleshooting, it is helpful to look at the `BLC_PAYMENT_RESPONSE_ITEM` table to see the response/error messages that are generated when calling the PayPal API.

At this point, all the configuration should be complete and you are now ready to test your integration with PayPal. Add something to your cart and proceed with PayPal checkout.
To customize your integration with PayPal even further, see [[PayPal Advance Configuration]] 
