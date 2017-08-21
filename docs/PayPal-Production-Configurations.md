# PayPal Production Configurations
To start accepting transactions on a production site, users must sign up for an actual Business Account. This can be done here: https://www.paypal.com/us/webapps/mpp/merchant

## System Property Updates
First, you'll need to update the following properties to ensure that you're making use of your PayPal Business Account:

- `gateway.paypal.expressCheckout.user`
- `gateway.paypal.expressCheckout.password`
- `gateway.paypal.expressCheckout.signature`

Additionally, you must update the following properties to point to PayPal's production servers:

- gateway.paypal.expressCheckout.serverUrl=https://api-3t.paypal.com/nvp
- gateway.paypal.expressCheckout.userRedirectUrl=https://www.paypal.com/cgi-bin/webscr

> Note: We suggest that you make these changes in your `production.properties` or `production-shared.properties` files as to avoid using PayPal's production resources in any development or staging environments.

