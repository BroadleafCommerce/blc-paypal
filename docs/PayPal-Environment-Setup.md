# PayPal Environment Setup

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
    <version>${broadleaf.thirdparty.version}</version>
    <type>jar</type>
    <scope>compile</scope>
</dependency>
```

Make sure to include the dependency in your site pom.xml as well:

```xml
<dependency>
    <groupId>org.broadleafcommerce</groupId>
    <artifactId>broadleaf-paypal</artifactId>
</dependency>
```

You should now begin to setup your environment to work with Broadleaf Commerce PayPal support. The first step is to make Broadleaf Commerce aware of your PayPal account credentials. This is accomplished through environment configuration (see [[Runtime Environment Configuration]]).

Broadleaf allows you to create your own property files per environment (e.g. common.properties, local.properties, development.properties, integrationdev.properties, integrationqa.properties, staging.properties, and production.properties) 
You will need to enter the following key/value pairs in the appropriate locations and replace the "?" with your paypal api account details:

### common.properties
    paypal.version=78.0
    paypal.shipping.display=? (e.g. 0, 1, or 2)
    paypal.additional.HDRIMG=?
    paypal.additional.HDRBORDERCOLOR=? (e.g. FFFFFF)
    paypal.additional.HDRBACKCOLOR=? (e.g. FFFFFF)
    paypal.additional.PAYFLOWCOLOR=? (e.g. FFFFFF)

- paypal.shipping.display: this property determines if PayPal displays the shipping address fields on the PayPal pages. For digital goods, this field is required and must be set to 1.
    - 0 : PayPal displays the shipping address passed in.
    - 1 : PayPal does not display the shipping fields at all.
    - 2 : PayPal will obtain the shipping address from the buyer's profile.
    
### development.properties, local.properties etc...
    paypal.user=?
    paypal.password=?
    paypal.signature=?
    paypal.api.url=https://api-3t.sandbox.paypal.com/nvp
    paypal.user.redirect.url=https://www.sandbox.paypal.com/cgi-bin/webscr
    paypal.return.url=? (e.g. http://localhost:8080/paypal/process)
    paypal.cancel.url=? (e.g. http://localhost:8080/cart)

- paypal.return.url: the URL PayPal should redirect to after completing the order
- paypal.cancel.url: the URL PayPal should redirect to if a user abandons the order

### production.properties
    paypal.user=?
    paypal.password=?
    paypal.signature=?
    paypal.api.url=https://api-3t.paypal.com/nvp
    paypal.user.redirect.url=https://www.paypal.com/cgi-bin/webscr
    paypal.return.url=? (e.g. http://mycompany.com/paypal/process)
    paypal.cancel.url=? (e.g. http://mycompany.com/cart)

- paypal.return.url: the URL PayPal should redirect to after completing the order
- paypal.cancel.url: the URL PayPal should redirect to if a user abandons the order

Now that you have your environment set up, let's begin setting up the [[PayPal Module]].
