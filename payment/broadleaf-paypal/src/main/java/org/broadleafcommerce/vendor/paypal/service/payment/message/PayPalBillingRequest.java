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

/**
 * 
 * @author jfischer
 *
 */
public class PayPalBillingRequest implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private String title;
    private String firstName;
    private String middleName;
    private String lastName;
    private String suffix;
    private String street1;
    private String street2;
    private String street3;
    private String street4;
    private String city;
    private String county;
    private String state;
    private String postalCode;
    private String country;
    private String company;
    private String companyTaxID;
    private String phoneNumber;
    private String email;
    private String ipAddress;
    private String ipNetworkAddress;
    private String dateOfBirth;
    private String driversLicenseNumber;
    private String driversLicenseState;
    private String ssn;

    /**
     * Gets the title value for this BillTo.
     *
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title value for this BillTo.
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the firstName value for this BillTo.
     *
     * @return firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the firstName value for this BillTo.
     *
     * @param firstName
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the middleName value for this BillTo.
     *
     * @return middleName
     */
    public String getMiddleName() {
        return middleName;
    }

    /**
     * Sets the middleName value for this BillTo.
     *
     * @param middleName
     */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    /**
     * Gets the lastName value for this BillTo.
     *
     * @return lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the lastName value for this BillTo.
     *
     * @param lastName
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the suffix value for this BillTo.
     *
     * @return suffix
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * Sets the suffix value for this BillTo.
     *
     * @param suffix
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    /**
     * Gets the street1 value for this BillTo.
     *
     * @return street1
     */
    public String getStreet1() {
        return street1;
    }

    /**
     * Sets the street1 value for this BillTo.
     *
     * @param street1
     */
    public void setStreet1(String street1) {
        this.street1 = street1;
    }

    /**
     * Gets the street2 value for this BillTo.
     *
     * @return street2
     */
    public String getStreet2() {
        return street2;
    }

    /**
     * Sets the street2 value for this BillTo.
     *
     * @param street2
     */
    public void setStreet2(String street2) {
        this.street2 = street2;
    }

    /**
     * Gets the street3 value for this BillTo.
     *
     * @return street3
     */
    public String getStreet3() {
        return street3;
    }

    /**
     * Sets the street3 value for this BillTo.
     *
     * @param street3
     */
    public void setStreet3(String street3) {
        this.street3 = street3;
    }

    /**
     * Gets the street4 value for this BillTo.
     *
     * @return street4
     */
    public String getStreet4() {
        return street4;
    }

    /**
     * Sets the street4 value for this BillTo.
     *
     * @param street4
     */
    public void setStreet4(String street4) {
        this.street4 = street4;
    }

    /**
     * Gets the city value for this BillTo.
     *
     * @return city
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the city value for this BillTo.
     *
     * @param city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Gets the county value for this BillTo.
     *
     * @return county
     */
    public String getCounty() {
        return county;
    }

    /**
     * Sets the county value for this BillTo.
     *
     * @param county
     */
    public void setCounty(String county) {
        this.county = county;
    }

    /**
     * Gets the state value for this BillTo.
     *
     * @return state
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the state value for this BillTo.
     *
     * @param state
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Gets the postalCode value for this BillTo.
     *
     * @return postalCode
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * Sets the postalCode value for this BillTo.
     *
     * @param postalCode
     */
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * Gets the country value for this BillTo.
     *
     * @return country
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the country value for this BillTo.
     *
     * @param country
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Gets the company value for this BillTo.
     *
     * @return company
     */
    public String getCompany() {
        return company;
    }

    /**
     * Sets the company value for this BillTo.
     *
     * @param company
     */
    public void setCompany(String company) {
        this.company = company;
    }

    /**
     * Gets the companyTaxID value for this BillTo.
     *
     * @return companyTaxID
     */
    public String getCompanyTaxID() {
        return companyTaxID;
    }

    /**
     * Sets the companyTaxID value for this BillTo.
     *
     * @param companyTaxID
     */
    public void setCompanyTaxID(String companyTaxID) {
        this.companyTaxID = companyTaxID;
    }

    /**
     * Gets the phoneNumber value for this BillTo.
     *
     * @return phoneNumber
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the phoneNumber value for this BillTo.
     *
     * @param phoneNumber
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Gets the email value for this BillTo.
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email value for this BillTo.
     *
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the ipAddress value for this BillTo.
     *
     * @return ipAddress
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Sets the ipAddress value for this BillTo.
     *
     * @param ipAddress
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Gets the ipNetworkAddress value for this BillTo.
     *
     * @return ipNetworkAddress
     */
    public String getIpNetworkAddress() {
        return ipNetworkAddress;
    }

    /**
     * Sets the ipNetworkAddress value for this BillTo.
     *
     * @param ipNetworkAddress
     */
    public void setIpNetworkAddress(String ipNetworkAddress) {
        this.ipNetworkAddress = ipNetworkAddress;
    }

    /**
     * Gets the dateOfBirth value for this BillTo.
     *
     * @return dateOfBirth
     */
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Sets the dateOfBirth value for this BillTo.
     *
     * @param dateOfBirth
     */
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Gets the driversLicenseNumber value for this BillTo.
     *
     * @return driversLicenseNumber
     */
    public String getDriversLicenseNumber() {
        return driversLicenseNumber;
    }

    /**
     * Sets the driversLicenseNumber value for this BillTo.
     *
     * @param driversLicenseNumber
     */
    public void setDriversLicenseNumber(String driversLicenseNumber) {
        this.driversLicenseNumber = driversLicenseNumber;
    }

    /**
     * Gets the driversLicenseState value for this BillTo.
     *
     * @return driversLicenseState
     */
    public String getDriversLicenseState() {
        return driversLicenseState;
    }

    /**
     * Sets the driversLicenseState value for this BillTo.
     *
     * @param driversLicenseState
     */
    public void setDriversLicenseState(String driversLicenseState) {
        this.driversLicenseState = driversLicenseState;
    }

    /**
     * Gets the ssn value for this BillTo.
     *
     * @return ssn
     */
    public String getSsn() {
        return ssn;
    }

    /**
     * Sets the ssn value for this BillTo.
     *
     * @param ssn
     */
    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

}
