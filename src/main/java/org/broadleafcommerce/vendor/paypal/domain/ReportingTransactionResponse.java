/*-
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2019 Broadleaf Commerce
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
package org.broadleafcommerce.vendor.paypal.domain;

import com.paypal.base.rest.PayPalModel;

import java.util.List;

public class ReportingTransactionResponse extends PayPalModel{

    public ReportingTransactionResponse(){

    }

    private List<TransactionDetail> transaction_details;

    private String account_number;

    private String start_date;

    private String end_date;

    private String last_refreshed_datetime;

    private Integer page;

    private Integer total_items;

    private Integer total_pages;

    public List<TransactionDetail> getTransaction_details() {
        return this.transaction_details;
    }

    public ReportingTransactionResponse setTransaction_details(List<TransactionDetail> transaction_details) {
        this.transaction_details = transaction_details;
        return this;
    }

    public String getAccount_number() {
        return this.account_number;
    }

    public ReportingTransactionResponse setAccount_number(String account_number) {
        this.account_number = account_number;
        return this;
    }

    public String getStart_date() {
        return this.start_date;
    }

    public ReportingTransactionResponse setStart_date(String start_date) {
        this.start_date = start_date;
        return this;
    }

    public String getEnd_date() {
        return this.end_date;
    }

    public ReportingTransactionResponse setEnd_date(String end_date) {
        this.end_date = end_date;
        return this;
    }

    public String getLast_refreshed_datetime() {
        return this.last_refreshed_datetime;
    }

    public ReportingTransactionResponse setLast_refreshed_datetime(String last_refreshed_datetime) {
        this.last_refreshed_datetime = last_refreshed_datetime;
        return this;
    }

    public Integer getPage() {
        return this.page;
    }

    public ReportingTransactionResponse setPage(Integer page) {
        this.page = page;
        return this;
    }

    public Integer getTotal_items() {
        return this.total_items;
    }

    public ReportingTransactionResponse setTotal_items(Integer total_items) {
        this.total_items = total_items;
        return this;
    }

    public Integer getTotal_pages() {
        return this.total_pages;
    }

    public ReportingTransactionResponse setTotal_pages(Integer total_pages) {
        this.total_pages = total_pages;
        return this;
    }


}
