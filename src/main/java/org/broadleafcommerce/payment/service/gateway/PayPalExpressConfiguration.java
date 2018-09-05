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

import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.service.PaymentGatewayConfiguration;
import org.broadleafcommerce.vendor.paypal.service.PayPalWebProfileService;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalShippingDisplayType;

import java.util.Map;

/**
 * @author Elbert Bautista (elbertbautista)
 */
public interface PayPalExpressConfiguration extends PaymentGatewayConfiguration {

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
     * Gets the the property driven id of the WebProfile to be used when creating payments. For more information on WebProfiles go to {@link https://developer.paypal.com/docs/integration/direct/payment-experience/}
     * {@link PayPalWebProfileService#getWebProfileId()} should be used instead if you want to find the web profile id to create a payment since it has the ability to create new WebProfiles based on injected beans along with using this method
     * 
     * @return
     */
    public String getWebProfileId();

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
     * <p>
     * See the PayPal API to see what additional configs you can set:
     * https://developer.paypal.com/docs/classic/api/merchant/SetExpressCheckout_API_Operation_NVP/
     * 
     * <p>
     * e.g. Map<String, String> additionalConfigs = new HashMap<String, String>();
     * additionalConfigs.put("HDRBORDERCOLOR", "FFFFFF");
     * additionalConfigs.put("HDRBACKCOLOR", "FFFFFF");
     * additionalConfigs.put("PAYFLOWCOLOR", "FFFFFF");
     * 
     * <p>
     * This adds additional NVP items to the Paypal request that are ONLY pre-specified in the Paypal API docs.
     * Any other fields will be ignored. If you want to use completely custom fields, see {@link #getAdditionalCustomFields()}
     *
     * @return Map
     */
    public Map<String, String> getAdditionalConfig();

    /**
     * <p>
     * The Paypal NVP API only allows a single field with custom logic in it: PAYMENTREQUEST_n_CUSTOM.
     * Because of this, all of the fields returned here are serialized together like so:
     * 
     * <pre>
     * {@code ccoc=true_12345|key1=value1|key2=value2|key3=value3}
     * </pre>
     * 
     * <p>
     * Note that Broadleaf uses a piece of this to determine if we should complete checkout on callback or not. This is done
     * as "ccoc=true_12345" where {@code true} is the value of {@link PaymentRequestDTO#isCompleteCheckoutOnCallback()}. So,
     * the minimum string that will be contained in the custom field is {@code ccoc=true_12345}, plus whatever other fields you have.
     * 
     * <p>
     * Also note that the entire custom field string after serialization is 256 characters. An IllegalArgumentException will be thrown
     * otherwise.
     */
    public Map<String, String> getAdditionalCustomFields();
}
