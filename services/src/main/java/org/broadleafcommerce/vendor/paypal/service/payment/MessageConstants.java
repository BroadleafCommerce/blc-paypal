/*
 * Copyright (C) 2009 - 2020 Broadleaf Commerce
 *
 * Licensed under the Broadleaf End User License Agreement (EULA), Version 1.1 (the
 * "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt).
 *
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the
 * "Custom License") between you and Broadleaf Commerce. You may not use this file except in
 * compliance with the applicable license.
 *
 * NOTICE: All information contained herein is, and remains the property of Broadleaf Commerce, LLC
 * The intellectual and technical concepts contained herein are proprietary to Broadleaf Commerce,
 * LLC and may be covered by U.S. and Foreign Patents, patents in process, and are protected by
 * trade secret or copyright law. Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained from Broadleaf Commerce, LLC.
 */
package org.broadleafcommerce.vendor.paypal.service.payment;

/**
 * @author Jeff Fischer
 */
public class MessageConstants {

    public static final String CHECKOUT_COMPLETE = "CheckoutComplete";
    public static final String TOTAL = "Total";
    public static final String ESTIMATEDTOTAL = "EstimatedTotal";
    public static final String AUTHORIZATIONID = "AUTHORIZATIONID";
    public static final String CAPTUREID = "CAPTUREID";
    public static final String SALEID = "SALEID";
    public static final String REFUNDID = "REFUNDID";
    public static final String NOTE = "NOTE";
    public static final String PAYERID = "PAYERID";
    public static final String PAYMENTID = "PAYMENTID";
    public static final String PAYERSTATUS = "PAYERSTATUS";
    public static final String ADDRESSSTATUS = "PAYMENTREQUEST_n_ADDRESSSTATUS";
    public static final String DETAILSPAYMENTITEMTOTAL = "PAYMENTREQUEST_n_ITEMAMT";
    public static final String DETAILSPAYMENTSHIPPINGTOTAL = "PAYMENTREQUEST_n_SHIPPINGAMT";
    public static final String DETAILSPAYMENTSHIPPINGDISCOUNT = "PAYMENTREQUEST_n_SHIPDISCAMT";
    public static final String DETAILSPAYMENTTOTALTAX = "PAYMENTREQUEST_n_TAXAMT";
    public static final String DETAILSPAYMENTTRANSACTIONID = "PAYMENTREQUEST_n_TRANSACTIONID";
    public static final String DETAILSPAYMENTALLOWEDMETHOD =
            "PAYMENTREQUEST_n_ALLOWEDPAYMENTMETHOD";
    public static final String BILLINGAGREEMENTID = "BILLING_AGREEMENT_ID";
    public static final String BILLINGECTOKEN = "BILLING_EC_TOKEN";
    public static final String AGREEMENTTOKENID = "AGREEMENT_TOKEN_ID";
    public static final String PAYER_PAYMENTMETHOD_PAYPAL = "paypal";
    public static final String PAYER_INFO_EMAIL = "PAYPAL_PAYER_INFO_EMAIL";
    public static final String PAYER_INFO_FIRST_NAME = "PAYPAL_PAYER_INFO_FIRST_NAME";
    public static final String PAYER_INFO_LAST_NAME = "PAYPAL_PAYER_INFO_LAST_NAME";
    public static final String PLAN_TYPE_MERCHANTINITIATEDBILLING = "MERCHANT_INITIATED_BILLING";
    public static final String MERCHANTPREF_ACCEPTEDPAYMENTTYPE_INSTANT = "INSTANT";
    public static final String HTTP_HEADER_AUTH_ASSERTION = "PayPal-Auth-Assertion";
    public static final String HTTP_HEADER_CLIENT_METADATA_ID = "PayPal-Client-Metadata-Id";
    public static final String IDEMPOTENCY_KEY = "idempotency_key";
    public static final String HTTP_HEADER_REQUEST_ID = "PayPal-Request-Id";
    public static final String HTTP_HEADER_MOCK_RESPONSE = "PayPal-Mock-Response";
    public static final String BN = "PayPal-Partner-Attribution-Id";
    public static final String BNCODE = "BroadleafCommerce_Cart_EC";
    public static final String HTTP_PAYERID = "payerId";
    public static final String HTTP_PAYMENTID = "paymentId";
    public static final String HTTP_TOKEN = "token";
    public static final String HTTP_BILLINGTOKEN = "billingToken";
    public static final String HTTP_REQUEST = "HTTP_REQUEST";
    public static final String ERROR_NAME = "errorName";
    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String ERROR_DEBUG_ID = "errorDebugId";
    public static final String CUSTOM_FIELD = "custom_field";
    public static final String TRANSACTION_STATUS = "TRANSACTION_STATUS";
    public static final String PAYMENT_SUBMITTED_TIME = "PAYMENT_SUBMITTED_TIME";

}
