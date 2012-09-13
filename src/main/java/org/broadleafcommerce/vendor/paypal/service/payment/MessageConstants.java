/*
 * Copyright 2008-2012 the original author or authors.
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

    public static final String REFUNDTYPE = "REFUNDTYPE";
    public static final String SUBTOTAL = "SUBTOTAL";
    public static final String TOTALSHIPPING = "TOTALSHIPPING";
    public static final String TOTALTAX = "TOTALTAX";

    public static final String MODULEERRORCODE = "MODULEERRORCODE";
    public static final String MODULEERRORSHORTMESSAGE = "MODULEERRORSHORTMESSAGE";
    public static final String MODULEERRORLONGMESSAGE = "MODULEERRORLONGMESSAGE";
    public static final String MODULEERRORSEVERITYCODE = "MODULEERRORSEVERITYCODE";
    public static final String REDIRECTURL = "REDIRECTURL";
    public static final String EXCHANGERATE = "EXCHANGERATE";
    public static final String PAYMENTSTATUS = "PAYMENTSTATUS";
    public static final String PENDINGREASON = "PENDINGREASON";
    public static final String REASONCODE = "REASONCODE";
    public static final String HOLDDECISION = "HOLDDECISION";

    public static final String PENDINGREASONTYPE = "PENDINGREASON";
    public static final String PAYMENTSTATUSTYPE = "PAYMENTSTATUS";
    public static final String TAXAMOUNT = "TAXAMT";
    public static final String SETTLEAMOUNT = "SETTLEAMT";
    public static final String FEEAMOUNT = "FEEAMT";
    public static final String ORDERITEM = "ORDERTIME";
    public static final String PAYMENTTYPE = "PAYMENTTYPE";
    public static final String REFUNDSTATUS = "REFUNDSTATUS";
    public static final String REFUNDINFO = "REFUNDINFO";
    public static final String TOTALREFUNDEDAMT = "TOTALREFUNDEDAMT";
    public static final String NETREFUNDAMT = "NETREFUNDAMT";
    public static final String GROSSREFUNDAMT = "GROSSREFUNDAMT";
    public static final String FEEREFUNDAMT = "FEEREFUNDAMT";
    public static final String REFUNDTRANSACTIONID = "REFUNDTRANSACTIONID";
    public static final String AMOUNT = "AMT";
    public static final String ACK = "ACK";
    public static final String SUCCESS = "success";
    public static final String SUCCESSWITHWARNINGS = "successwithwarnings";
    public static final String TOKEN = "TOKEN";
    public static final String CORRELATIONID = "CORRELATIONID";
    public static final String TRANSACTIONID = "TRANSACTIONID";
    public static final String PARENTTRANSACTIONID = "PARENTTRANSACTIONID";
    public static final String RECEIPTID = "RECEIPTID";
    public static final String AUTHORIZATONID = "AUTHORIZATIONID";
    public static final String CURRENCYCODE = "CURRENCYCODE";
    public static final String NOSHIPPING = "NOSHIPPING";
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
    public static final String SUBTOTALREQUEST = "PAYMENTREQUEST_n_ITEMAMT";
    public static final String TAXREQUEST = "PAYMENTREQUEST_n_TAXAMT";
    public static final String SHIPPINGREQUEST = "PAYMENTREQUEST_n_SHIPPINGAMT";
    public static final String GRANDTOTALREQUEST = "PAYMENTREQUEST_n_AMT";
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
    public static final String ADDROVERRIDE = "ADDROVERRIDE";

    public static final String DETAILSPAYMENTAMOUNT = "PAYMENTREQUEST_n_AMT";
    public static final String DETAILSPAYMENTCURRENCYCODE = "PAYMENTREQUEST_n_CURRENCYCODE";
    public static final String DETAILSPAYMENTITEMTOTAL = "PAYMENTREQUEST_n_ITEMAMT";
    public static final String DETAILSPAYMENTSHIPPINGTOTAL = "PAYMENTREQUEST_n_SHIPPINGAMT";
    public static final String DETAILSPAYMENTSHIPPINGDISCOUNT = "PAYMENTREQUEST_n_SHIPDISCAMT";
    public static final String DETAILSPAYMENTTOTALTAX = "PAYMENTREQUEST_n_TAXAMT";
    public static final String DETAILSPAYMENTREFERENCENUMBER = "PAYMENTREQUEST_n_INVNUM";
    public static final String DETAILSPAYMENTTRANSACTIONID = "PAYMENTREQUEST_n_TRANSACTIONID";
    public static final String DETAILSPAYMENTALLOWEDMETHOD = "PAYMENTREQUEST_n_ALLOWEDPAYMENTMETHOD";
    public static final String DETAILSPAYMENTREQUESTID = "PAYMENTREQUEST_n_PAYMENTREQUESTID";
    public static final String DETAILSPAYMENTITEMNAME = "L_PAYMENTREQUEST_n_NAMEm";
    public static final String DETAILSPAYMENTITEMDESCRIPTION = "L_PAYMENTREQUEST_n_DESCm";
    public static final String DETAILSPAYMENTITEMAMOUNT = "L_PAYMENTREQUEST_n_AMTm";
    public static final String DETAILSPAYMENTITEMNUMBER = "L_PAYMENTREQUEST_n_NUMBERm";
    public static final String DETAILSPAYMENTITEMQUANTITY = "L_PAYMENTREQUEST_n_QTYm";
    public static final String DETAILSPAYMENTITEMTAX = "L_PAYMENTREQUEST_n_TAXAMTm";
    public static final String DETAILSPAYMENTERRORCODE = "PAYMENTREQUEST_n_ERRORCODE";
    public static final String DETAILSPAYMENTERRORSHORTMESSAGE = "PAYMENTREQUEST_n_SHORTMESSAGE";
    public static final String DETAILSPAYMENTERRORLONGMESSAGE = "PAYMENTREQUEST_n_LONGMESSAGE";
    public static final String DETAILSPAYMENTERRORSEVERITYCODE = "PAYMENTREQUEST_n_SEVERITYCODE";
    public static final String DETAILSPAYMENTERRORACK = "PAYMENTREQUEST_n_ACK";

    public static final String PROCESSPAYMENTTRANSACTIONID = "PAYMENTINFO_n_TRANSACTIONID";
    public static final String PROCESSPAYMENTPAYMENTTYPE = "PAYMENTINFO_n_PAYMENTTYPE";
    public static final String PROCESSPAYMENTORDERITEM = "PAYMENTINFO_n_ORDERTIME";
    public static final String PROCESSPAYMENTAMOUNT = "PAYMENTINFO_n_AMT";
    public static final String PROCESSPAYMENTCURRENCYCODE = "PAYMENTINFO_n_CURRENCYCODE";
    public static final String PROCESSPAYMENTFEEAMOUNT = "PAYMENTINFO_n_FEEAMT";
    public static final String PROCESSPAYMENTSETTLEAMOUNT = "PAYMENTINFO_n_SETTLEAMT";
    public static final String PROCESSPAYMENTTAXAMOUNT = "PAYMENTINFO_n_TAXAMT";
    public static final String PROCESSPAYMENTEXCHANGERATE = "PAYMENTINFO_n_EXCHANGERATE";
    public static final String PROCESSPAYMENTPAYMENTSTATUSTYPE = "PAYMENTINFO_n_PAYMENTSTATUS";
    public static final String PROCESSPAYMENTPENDINGREASONTYPE = "PAYMENTINFO_n_PENDINGREASON";
    public static final String PROCESSPAYMENTREASONCODETYPE = "PAYMENTINFO_n_REASONCODE";
    public static final String PROCESSPAYMENTHOLDDECISIONTYPE = "PAYMENTINFO_n_HOLDDECISION";
    
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
    public static final String BN = "BUTTONSOURCE";
    public static final String BNCODE = "BroadleafCommerce_Cart_EC";

}
