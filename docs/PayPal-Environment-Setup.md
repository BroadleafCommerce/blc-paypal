# PayPal Express Checkout Environment Setup

## Prerequisites

- Users must establish their own sandbox accounts with PayPal in order to use the BroadleafCommerce PayPal payment functionality in a test environment. This can be done here: https://developer.paypal.com/devscr?cmd=_signup-run
- Users must also establish PayPal API Credentials in order perform various operations on the PayPal Express API. The API credentials for your business account (api username, api password, signature) are obtained via the steps mentioned on this page: https://cms.paypal.com/us/cgi-bin/?cmd=_render-content&content_ID=developer/e_howto_api_ECAPICredentials
- Note: Users must sign up for an actual Business Account to start accepting transactions on a production site. This can be done here: https://merchant.paypal.com/cgi-bin/marketingweb?cmd=_render-content&content_ID=merchant/express_checkout&nav=2.1.5
- Please familiarize yourself with the PayPal Express Checkout Prerequisites before proceeding. https://cms.paypal.com/us/cgi-bin/?cmd=_render-content&content_ID=developer/e_howto_api_ECGettingStarted

Once you have established an account with PayPal, begin by including the PayPal Module dependency to your main pom.xml.

```xml
<dependency>
    <groupId>org.broadleafcommerce</groupId>
    <artifactId>broadleaf-paypal</artifactId>
    <version>${broadleaf.paypal-express.version}</version>
    <type>jar</type>
    <scope>compile</scope>
</dependency>
```

Make sure to include the dependency in your `site` AND `admin` pom.xml as well:

```xml
<dependency>
    <groupId>org.broadleafcommerce</groupId>
    <artifactId>broadleaf-paypal</artifactId>
</dependency>
```

You should now begin to setup your environment to work with Broadleaf Commerce PayPal support. The first step is to make Broadleaf Commerce aware of your PayPal account credentials. This is accomplished through environment configuration (see [[Runtime Environment Configuration]]).

Broadleaf allows you to create your own property files per environment (e.g. common.properties, local.properties, development.properties, integrationdev.properties, integrationqa.properties, staging.properties, and production.properties) 
You will need to enter the following key/value pairs in the appropriate locations and replace the "?" with your paypal api account details:

You can also store these configs in the Database by utilizing the `blSystemPropertiesService`. See the java docs for more details.

### Properties File Config
- gateway.paypal.expressCheckout.libVersion=78.0
- gateway.paypal.expressCheckout.serverUrl=https://api-3t.sandbox.paypal.com/nvp
- gateway.paypal.expressCheckout.userRedirectUrl=https://www.sandbox.paypal.com/cgi-bin/webscr
- gateway.paypal.expressCheckout.password=?
- gateway.paypal.expressCheckout.user=?
- gateway.paypal.expressCheckout.signature=?
- gateway.paypal.expressCheckout.useRelativeUrls=false
- gateway.paypal.expressCheckout.returnUrl=? (http://localhost:8080/paypal-express/return)
- gateway.paypal.expressCheckout.cancelUrl=? (http://localhost:8080/paypal-express/cancel)

- paypal.return.url: the URL PayPal should redirect to after completing the order
- paypal.cancel.url: the URL PayPal should redirect to if a user abandons the order

### Production Property Config
- gateway.paypal.expressCheckout.serverUrl=https://api-3t.paypal.com/nvp
- gateway.paypal.expressCheckout.userRedirectUrl=https://www.paypal.com/cgi-bin/webscr

- paypal.return.url: the URL PayPal should redirect to after completing the order
- paypal.cancel.url: the URL PayPal should redirect to if a user abandons the order

Now that you have your environment set up, let's begin setting up the [[PayPal Module]].
