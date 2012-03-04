/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.vendor.paypal.service.payment;

/**
 * @author Jeff Fischer
 */
public class MessageConstants {
    
    public static final String ACK = "ACK";
    
    public static final String SUCCESS = "success";
    public static final String SUCCESSWITHWARNINGS = "successwithwarnings";
    public static final String TOKEN = "TOKEN";
    
    public static final String USER = "USER";
    public static final String PASSWORD = "PWD";
    public static final String SIGNATURE = "SIGNATURE";
    public static final String VERSION = "VERSION";
    public static final String PAYMENTACTION = "PAYMENTREQUEST_0_PAYMENTACTION";
    public static final String RETURNURL = "RETURNURL";
    public static final String CANCELURL = "CANCELURL";
    public static final String METHOD = "METHOD";
    public static final String NAMEREQUEST = "L_PAYMENTREQUEST_0_NAME";
    public static final String NUMBERREQUEST = "L_PAYMENTREQUEST_0_NUMBER";
    public static final String DESCRIPTIONREQUEST = "L_PAYMENTREQUEST_0_DESC";
    public static final String AMOUNTREQUEST = "L_PAYMENTREQUEST_0_AMT";
    public static final String QUANTITYREQUEST = "L_PAYMENTREQUEST_0_QTY";
    public static final String SUBTOTALREQUEST = "PAYMENTREQUEST_0_ITEMAMT";
    public static final String TAXREQUEST = "PAYMENTREQUEST_0_TAXAMT";
    public static final String SHIPPINGREQUEST = "PAYMENTREQUEST_0_SHIPPINGAMT";
    public static final String SHIPPINGDISCOUNTREQUEST = "PAYMENTREQUEST_0_SHIPDISCAMT";
    public static final String GRANDTOTALREQUEST = "PAYMENTREQUEST_0_AMT";
    public static final String ERRORCODE = "L_ERRORCODE";
    public static final String ERRORSHORTMESSAGE = "L_SHORTMESSAGE";
    public static final String ERRORLONGMESSAGE = "L_LONGMESSAGE";
    public static final String ERRORSEVERITYCODE = "L_SEVERITYCODE";
    public static final String ERRORPASSTHROUGHNAME = "L_ERRORPARAMID";
    public static final String ERRORPASSTHROUGHVALUE = "L_ERRORPARAMVALUE";
    
    public static final String SALEACTION = "Sale";
    public static final String EXPRESSCHECKOUTACTION = "SetExpressCheckout";
}
