/*
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2014 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.payment.service.gateway;

import org.broadleafcommerce.common.payment.service.PaymentGatewayConfiguration;
import org.broadleafcommerce.common.payment.service.PaymentGatewayConfigurationService;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalShippingDisplayType;

import java.util.Map;

/**
 * @author Elbert Bautista (elbertbautista)
 */
public interface PayPalExpressConfiguration extends PaymentGatewayConfiguration {

    /**
     * The URL endpoint for the NVP API server
     * e.g. "https://api-3t.sandbox.paypal.com/nvp"
     *
     * @return String
     */
    public String getServerUrl();

    /**
     * The base URL to which you should redirect the user to after obtaining a transaction token
     * e.g. "https://www.sandbox.paypal.com/cgi-bin/webscr"
     *
     * @return String
     */
    public String getUserRedirectUrl();

    /**
     * The PayPal API version
     * e.g. "78.0"
     *
     * @return String
     */
    public String getLibVersion();

    /**
     * The PayPal Sandbox/Production Account Password
     *
     * @return String
     */
    public String getPassword();

    /**
     * The PayPal Sandbox/Production Account User
     *
     * @return String
     */
    public String getUser();

    /**
     * The PayPal Sandbox/Production Account Signature
     *
     * @return String
     */
    public String getSignature();

    /**
     * URL to which the buyer's browser is returned after choosing to pay with PayPal.
     * For digital goods, you must add JavaScript to this page to close the in-context experience.
     * Note: PayPal recommends that the value be the final review page on which the buyer confirms the order and
     * payment or billing agreement.
     *
     * Character length and limitations: 2048 single-byte characters
     * @return String
     */
    public String getReturnUrl();

    /**
     * URL to which the buyer is returned if the buyer does not approve the use of PayPal to pay you.
     * For digital goods, you must add JavaScript to this page to close the in-context experience.
     * Note: PayPal recommends that the value be the original page on which the buyer chose to pay
     * with PayPal or establish a billing agreement.
     *
     * Character length and limitations: 2048 single-byte characters
     * @return
     */
    public String getCancelUrl();

    /**
     * For digital goods, this field is required and must be set to 1.
     * 0 - PayPal displays the shipping address passed in.
     * 1 - PayPal does not display the shipping fields at all. (Default)
     * 2 - PayPal will obtain the shipping address from the buyer's profile.
     *
     * @return PayPalShippingDisplayType
     */
    public PayPalShippingDisplayType getShippingDisplayType();

    /**
     * Type declaration for the label to be displayed in MiniCart for UX. It is one of the following values:
     *  - Total
     *  - EstimatedTotal
     *
     * @return String
     */
    public String getTotalType();

    /**
     * See the PayPal API to see what additional configs you can set:
     * https://developer.paypal.com/docs/classic/api/merchant/SetExpressCheckout_API_Operation_NVP/
     *
     * e.g. Map<String, String> additionalConfigs = new HashMap<String, String>();
     * additionalConfigs.put("HDRBORDERCOLOR", "FFFFFF");
     * additionalConfigs.put("HDRBACKCOLOR", "FFFFFF");
     * additionalConfigs.put("PAYFLOWCOLOR", "FFFFFF");
     *
     * @return Map
     */
    public Map<String, String> getAdditionalConfig();

}
