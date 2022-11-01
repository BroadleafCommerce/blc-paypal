/*-
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2022 Broadleaf Commerce
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
package org.broadleafcommerce.vendor.paypal.service.payment;

/**
 * @author Jeff Fischer
 */
public class MessageConstants {

    public static final String CHECKOUT_COMPLETE = "CheckoutComplete";
    public static final String TOTAL = "Total";
    public static final String AUTHORIZATONID = "AUTHORIZATIONID";
    public static final String CAPTUREID = "CAPTUREID";
    public static final String REFUNDID = "REFUNDID";
    public static final String PAYERID = "PAYERID";
    public static final String ORDER_ID = "ORDER_ID";
    public static final String ORDER_STATUS = "ORDER_STATUS";
    public static final String DETAILSPAYMENTITEMTOTAL = "PAYMENTREQUEST_n_ITEMAMT";
    public static final String DETAILSPAYMENTSHIPPINGTOTAL = "PAYMENTREQUEST_n_SHIPPINGAMT";
    public static final String DETAILSPAYMENTSHIPPINGDISCOUNT = "PAYMENTREQUEST_n_SHIPDISCAMT";
    public static final String DETAILSPAYMENTTOTALTAX = "PAYMENTREQUEST_n_TAXAMT";
    public static final String DETAILSPAYMENTTRANSACTIONID = "PAYMENTREQUEST_n_TRANSACTIONID";
    public static final String HTTP_HEADER_AUTH_ASSERTION = "PayPal-Auth-Assertion";
    public static final String HTTP_HEADER_CLIENT_METADATA_ID = "PayPal-Client-Metadata-Id";
    public static final String HTTP_HEADER_REQUEST_ID = "PayPal-Request-Id";
    public static final String HTTP_HEADER_MOCK_RESPONSE = "PayPal-Mock-Response";
    public static final String IDEMPOTENCY_KEY = "idempotency_key";
    public static final String BN = "PayPal-Partner-Attribution-Id";
    public static final String BNCODE = "BroadleafCommerce_Cart_EC";
    public static final String HTTP_PAYERID = "payerId";
    public static final String HTTP_ORDER_ID = "orderId";
    public static final String HTTP_REQUEST = "HTTP_REQUEST";
    public static final String EXCEPTION_NAME = "EXCEPTION_NAME";
    public static final String EXCEPTION_MESSAGE = "EXCEPTION_MESSAGE";
    public static final String EXCEPTION_DEBUG_ID = "EXCEPTION_DEBUG_ID";
    /**
     * An additional field to store the merchant ID of the partner merchant when using PayPal's
     * marketplace offering.
     */
    public static final String PAYEE_MERCHANT_ID = "PAYEE_MERCHANT_ID";
    /**
     * An additional field to store the merchant email of the partner merchant when using PayPal's
     * marketplace offering.
     */
    public static final String PAYEE_MERCHANT_EMAIL = "PAYEE_MERCHANT_EMAIL";
    /**
     * An additional field to store the shipping preference. This controls whether the customer
     * inputs shipping info with PayPal or if it is provided some other way. Possible values are:
     * <ul>
     * <li><code>GET_FROM_FILE</code>: The customer provides the address on the PayPal site</li>
     * <li><code>NO_SHIPPING</code>: The shipping address is not shown on the PayPal site</li>
     * <li><code>SET_PROVIDED_ADDRESS</code>: The merchant provides the shipping address that is
     * show and the customer cannot edit it.</li>
     * </ul>
     */
    public static final String SHIPPING_PREFERENCE = "SHIPPING_PREFERENCE";
    public static final String LOCALE = "LOCALE";

}
