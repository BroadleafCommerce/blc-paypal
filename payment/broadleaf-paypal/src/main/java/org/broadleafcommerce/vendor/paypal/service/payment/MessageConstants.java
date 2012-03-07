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

import org.omg.CORBA.PUBLIC_MEMBER;

/**
 * @author Jeff Fischer
 */
public class MessageConstants {
    
    public static final String ACK = "ACK";
    
    public static final String SUCCESS = "success";
    public static final String SUCCESSWITHWARNINGS = "successwithwarnings";
    public static final String TOKEN = "TOKEN";
    public static final String CORRELATIONID = "CORRELATIONID";
    public static final String TRANSACTIONID = "TRANSACTIONID";

    public static final String USER = "USER";
    public static final String PASSWORD = "PWD";
    public static final String SIGNATURE = "SIGNATURE";
    public static final String VERSION = "VERSION";
    public static final String PAYMENTACTION = "PAYMENTREQUEST_n_PAYMENTACTION";
    public static final String RETURNURL = "RETURNURL";
    public static final String CANCELURL = "CANCELURL";
    public static final String METHOD = "METHOD";
    public static final String NAMEREQUEST = "L_PAYMENTREQUEST_n_NAMEm";
    public static final String NUMBERREQUEST = "L_PAYMENTREQUEST_n_NUMBERm";
    public static final String DESCRIPTIONREQUEST = "L_PAYMENTREQUEST_n_DESCm";
    public static final String AMOUNTREQUEST = "L_PAYMENTREQUEST_n_AMTm";
    public static final String QUANTITYREQUEST = "L_PAYMENTREQUEST_n_QTYm";
    public static final String SUBTOTALREQUEST = "PAYMENTREQUEST_n_ITEMAMTm";
    public static final String TAXREQUEST = "PAYMENTREQUEST_n_TAXAMTm";
    public static final String SHIPPINGREQUEST = "PAYMENTREQUEST_n_SHIPPINGAMTm";
    public static final String SHIPPINGDISCOUNTREQUEST = "PAYMENTREQUEST_n_SHIPDISCAMTm";
    public static final String GRANDTOTALREQUEST = "PAYMENTREQUEST_n_AMTm";
    public static final String ERRORCODE = "L_ERRORCODEn";
    public static final String ERRORSHORTMESSAGE = "L_SHORTMESSAGEn";
    public static final String ERRORLONGMESSAGE = "L_LONGMESSAGEn";
    public static final String ERRORSEVERITYCODE = "L_SEVERITYCODEn";
    public static final String ERRORPASSTHROUGHNAME = "L_ERRORPARAMIDn";
    public static final String ERRORPASSTHROUGHVALUE = "L_ERRORPARAMVALUEn";
    public static final String INVNUM = "INVNUM";
    public static final String PHONENUM = "PHONENUM";
    public static final String PAYPALADJUSTMENT = "PAYPALADJUSTMENT";
    public static final String NOTE = "NOTE";
    public static final String CHECKOUTSTATUS = "CHECKOUTSTATUS";
    public static final String EMAILADDRESS = "EMAIL";
    public static final String PAYERID = "PAYERID";
    public static final String PAYERSTATUS = "PAYERSTATUS";
    public static final String COUNTRYCODE = "COUNTRYCODE";
    public static final String BUSINESS = "BUSINESS";
    public static final String PAYERSALUTATION = "SALUTATION";
    public static final String PAYERFIRSTNAME = "FIRSTNAME";
    public static final String PAYERLASTNAME = "LASTNAME";
    public static final String PAYERMIDDLENAME = "MIDDLENAME";
    public static final String PAYERSUFFIX = "SUFFIX";
    public static final String SHIPTONAME = "PAYMENTREQUEST_n_SHIPTONAME";
    public static final String SHIPTOSTREET = "PAYMENTREQUEST_n_SHIPTOSTREET";
    public static final String SHIPTOSTREET2 = "PAYMENTREQUEST_n_SHIPTOSTREET2";
    public static final String SHIPTOCITY = "PAYMENTREQUEST_n_SHIPTOCITY";
    public static final String SHIPTOSTATE = "PAYMENTREQUEST_n_SHIPTOSTATE";
    public static final String SHIPTOZIP = "PAYMENTREQUEST_n_SHIPTOZIP";
    public static final String SHIPTOCOUNTRYCODE = "PAYMENTREQUEST_n_SHIPTOCOUNTRYCODE";
    public static final String SHIPTOPHONENUMBER = "PAYMENTREQUEST_n_SHIPTOPHONENUM";
    public static final String ADDRESSSTATUS = "PAYMENTREQUEST_n_ADDRESSSTATUS";
    public static final String PAYMENTREQUESTAMOUNT = "PAYMENTREQUEST_n_AMT";
    public static final String PAYMENTREQUESTCURRENCYCODE = "PAYMENTREQUEST_n_CURRENCYCODE";
    public static final String PAYMENTREQUESTITEMTOTAL = "PAYMENTREQUEST_n_ITEMAMT";
    public static final String PAYMENTREQUESTSHIPPINGTOTAL = "PAYMENTREQUEST_n_SHIPPINGAMT";
    public static final String PAYMENTREQUESTSHIPPINGDICSOUNT = "PAYMENTREQUEST_n_SHIPDISCAMT";
    public static final String PAYMENTREQUESTTOTALTAX = "PAYMENTREQUEST_n_TAXAMT";
    public static final String PAYMENTREQUESTREFERENCENUMBER = "PAYMENTREQUEST_n_INVNUM";
    public static final String PAYMENTREQUESTTRANSACTIONID = "PAYMENTREQUEST_n_TRANSACTIONID";
    public static final String PAYMENTREQUESTPAYMENTMETHOD = "PAYMENTREQUEST_n_ALLOWEDPAYMENTMETHOD";
    public static final String PAYMENTREQUESTPAYMENTREQUESTID = "PAYMENTREQUEST_n_PAYMENTREQUESTID";
    public static final String PAYMENTREQUESTITEMNAME = "L_PAYMENTREQUEST_n_NAMEm";
    public static final String PAYMENTREQUESTITEMDESCRIPTION = "L_PAYMENTREQUEST_n_DESCm";
    public static final String PAYMENTREQUESTITEMAMOUNT = "L_PAYMENTREQUEST_n_AMTm";
    public static final String PAYMENTREQUESTITEMNUMBER = "L_PAYMENTREQUEST_n_NUMBERm";
    public static final String PAYMENTREQUESTITEMQUANTITY = "L_PAYMENTREQUEST_n_QTYm";
    public static final String PAYMENTREQUESTITEMTAX = "L_PAYMENTREQUEST_n_TAXAMTm";
    public static final String PAYMENTREQUESTERRORCODE = "PAYMENTREQUEST_n_ERRORCODE";
    public static final String PAYMENTREQUESTERORRSHORTMESSAGE = "PAYMENTREQUEST_n_SHORTMESSAGE";
    public static final String PAYMENTREQUESTERRORLONGMESSAGE = "PAYMENTREQUEST_n_LONGMESSAGE";
    public static final String PAYMENTREQUESTERRORSEVERITYCODE = "PAYMENTREQUEST_n_SEVERITYCODE";
    public static final String PAYMENTREQUESTERRORACK = "PAYMENTREQUEST_n_ACK";

    
    public static final String COMPLETETYPE = "COMPLETETYPE";
    public static final String CAPTURECOMPLETE = "Complete";

    public static final String SALEACTION = "Sale";
    public static final String AUTHORIZATIONACTION = "Authorization";
    public static final String EXPRESSCHECKOUTACTION = "SetExpressCheckout";
    public static final String PROCESSPAYMENTACTION = "DoExpressCheckoutPayment";
    public static final String PAYMENTDETAILSACTION = "GetExpressCheckoutDetails";
    public static final String REFUNDACTION = "RefundTransaction";
    public static final String CAPTUREACTION = "DoCapture";
    public static final String VOIDACTION = "DoVoid";

}
