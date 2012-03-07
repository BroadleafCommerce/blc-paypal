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

package org.broadleafcommerce.vendor.paypal.service.payment.message;

import java.util.ArrayList;
import java.util.List;

import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalCheckoutStatusType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalPayerStatusType;

/**
 * @author Jeff Fischer
 */
public class PayPalDetailsResponse extends PayPalResponse {

    protected String phoneNumber;
    protected Money payPalAdjustment;
    protected String note;
    protected PayPalCheckoutStatusType checkoutStatusType;
    protected String emailAddress;
    protected String payerId;
    protected PayPalPayerStatusType payerStatus;
    protected String countryCode;
    protected String business;
    protected String payerSalutation;
    protected String payerFirstName;
    protected String payerLastName;
    protected String payerMiddleName;
    protected String payerSuffix;
    protected List<PayPalPayerAddress> addresses = new ArrayList<PayPalPayerAddress>();
    protected PayPalPaymentDetails paymentDetails;
    protected List<PayPalDetailsErrorResponse> errorResponses = new ArrayList<PayPalDetailsErrorResponse>();

    public List<PayPalPayerAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<PayPalPayerAddress> addresses) {
        this.addresses = addresses;
    }

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String business) {
        this.business = business;
    }

    public PayPalCheckoutStatusType getCheckoutStatusType() {
        return checkoutStatusType;
    }

    public void setCheckoutStatusType(PayPalCheckoutStatusType checkoutStatusType) {
        this.checkoutStatusType = checkoutStatusType;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public List<PayPalDetailsErrorResponse> getErrorResponses() {
        return errorResponses;
    }

    public void setErrorResponses(List<PayPalDetailsErrorResponse> errorResponses) {
        this.errorResponses = errorResponses;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPayerFirstName() {
        return payerFirstName;
    }

    public void setPayerFirstName(String payerFirstName) {
        this.payerFirstName = payerFirstName;
    }

    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }

    public String getPayerLastName() {
        return payerLastName;
    }

    public void setPayerLastName(String payerLastName) {
        this.payerLastName = payerLastName;
    }

    public String getPayerMiddleName() {
        return payerMiddleName;
    }

    public void setPayerMiddleName(String payerMiddleName) {
        this.payerMiddleName = payerMiddleName;
    }

    public String getPayerSalutation() {
        return payerSalutation;
    }

    public void setPayerSalutation(String payerSalutation) {
        this.payerSalutation = payerSalutation;
    }

    public PayPalPayerStatusType getPayerStatus() {
        return payerStatus;
    }

    public void setPayerStatus(PayPalPayerStatusType payerStatus) {
        this.payerStatus = payerStatus;
    }

    public String getPayerSuffix() {
        return payerSuffix;
    }

    public void setPayerSuffix(String payerSuffix) {
        this.payerSuffix = payerSuffix;
    }

    public Money getPayPalAdjustment() {
        return payPalAdjustment;
    }

    public void setPayPalAdjustment(Money payPalAdjustment) {
        this.payPalAdjustment = payPalAdjustment;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public PayPalPaymentDetails getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(PayPalPaymentDetails paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PayPalDetailsResponse)) return false;
        if (!super.equals(o)) return false;

        PayPalDetailsResponse that = (PayPalDetailsResponse) o;

        if (addresses != null ? !addresses.equals(that.addresses) : that.addresses != null) return false;
        if (business != null ? !business.equals(that.business) : that.business != null) return false;
        if (checkoutStatusType != null ? !checkoutStatusType.equals(that.checkoutStatusType) : that.checkoutStatusType != null)
            return false;
        if (countryCode != null ? !countryCode.equals(that.countryCode) : that.countryCode != null) return false;
        if (emailAddress != null ? !emailAddress.equals(that.emailAddress) : that.emailAddress != null) return false;
        if (errorResponses != null ? !errorResponses.equals(that.errorResponses) : that.errorResponses != null)
            return false;
        if (note != null ? !note.equals(that.note) : that.note != null) return false;
        if (payPalAdjustment != null ? !payPalAdjustment.equals(that.payPalAdjustment) : that.payPalAdjustment != null)
            return false;
        if (payerFirstName != null ? !payerFirstName.equals(that.payerFirstName) : that.payerFirstName != null)
            return false;
        if (payerId != null ? !payerId.equals(that.payerId) : that.payerId != null) return false;
        if (payerLastName != null ? !payerLastName.equals(that.payerLastName) : that.payerLastName != null)
            return false;
        if (payerMiddleName != null ? !payerMiddleName.equals(that.payerMiddleName) : that.payerMiddleName != null)
            return false;
        if (payerSalutation != null ? !payerSalutation.equals(that.payerSalutation) : that.payerSalutation != null)
            return false;
        if (payerStatus != null ? !payerStatus.equals(that.payerStatus) : that.payerStatus != null) return false;
        if (payerSuffix != null ? !payerSuffix.equals(that.payerSuffix) : that.payerSuffix != null) return false;
        if (paymentDetails != null ? !paymentDetails.equals(that.paymentDetails) : that.paymentDetails != null)
            return false;
        if (phoneNumber != null ? !phoneNumber.equals(that.phoneNumber) : that.phoneNumber != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (phoneNumber != null ? phoneNumber.hashCode() : 0);
        result = 31 * result + (payPalAdjustment != null ? payPalAdjustment.hashCode() : 0);
        result = 31 * result + (note != null ? note.hashCode() : 0);
        result = 31 * result + (checkoutStatusType != null ? checkoutStatusType.hashCode() : 0);
        result = 31 * result + (emailAddress != null ? emailAddress.hashCode() : 0);
        result = 31 * result + (payerId != null ? payerId.hashCode() : 0);
        result = 31 * result + (payerStatus != null ? payerStatus.hashCode() : 0);
        result = 31 * result + (countryCode != null ? countryCode.hashCode() : 0);
        result = 31 * result + (business != null ? business.hashCode() : 0);
        result = 31 * result + (payerSalutation != null ? payerSalutation.hashCode() : 0);
        result = 31 * result + (payerFirstName != null ? payerFirstName.hashCode() : 0);
        result = 31 * result + (payerLastName != null ? payerLastName.hashCode() : 0);
        result = 31 * result + (payerMiddleName != null ? payerMiddleName.hashCode() : 0);
        result = 31 * result + (payerSuffix != null ? payerSuffix.hashCode() : 0);
        result = 31 * result + (addresses != null ? addresses.hashCode() : 0);
        result = 31 * result + (paymentDetails != null ? paymentDetails.hashCode() : 0);
        result = 31 * result + (errorResponses != null ? errorResponses.hashCode() : 0);
        return result;
    }
}
