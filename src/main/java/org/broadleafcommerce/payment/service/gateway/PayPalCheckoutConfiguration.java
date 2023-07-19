/*-
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2023 Broadleaf Commerce
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

import java.util.Map;

/**
 * @author Elbert Bautista (elbertbautista)
 */
public interface PayPalCheckoutConfiguration extends PaymentGatewayConfiguration {

    String getEnvironment();

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

    String getPaymentDescription();

    String getClientId();

    String getClientSecret();
}
