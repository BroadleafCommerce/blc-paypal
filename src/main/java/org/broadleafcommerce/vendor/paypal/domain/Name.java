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

public class Name {

    private String prefix;

    private String given_name;

    private String surname;

    private String middle_name;

    private String suffix;

    private String alternate_full_name;

    private String full_name;

    public String getPrefix() {
        return this.prefix;
    }

    public Name setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String getGiven_name() {
        return this.given_name;
    }

    public Name setGiven_name(String given_name) {
        this.given_name = given_name;
        return this;
    }

    public String getSurname() {
        return this.surname;
    }

    public Name setSurname(String surname) {
        this.surname = surname;
        return this;
    }

    public String getMiddle_name() {
        return this.middle_name;
    }

    public Name setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
        return this;
    }

    public String getSuffix() {
        return this.suffix;
    }

    public Name setSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    public String getAlternate_full_name() {
        return this.alternate_full_name;
    }

    public Name setAlternate_full_name(String alternate_full_name) {
        this.alternate_full_name = alternate_full_name;
        return this;
    }

    public String getFull_name() {
        return this.full_name;
    }

    public Name setFull_name(String full_name) {
        this.full_name = full_name;
        return this;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof Name)) return false;
        final Name other = (Name) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (!super.equals(o)) return false;
        final java.lang.Object this$prefix = this.getPrefix();
        final java.lang.Object other$prefix = other.getPrefix();
        if (this$prefix == null ? other$prefix != null : !this$prefix.equals(other$prefix)) return false;
        final java.lang.Object this$given_name = this.getGiven_name();
        final java.lang.Object other$given_name = other.getGiven_name();
        if (this$given_name == null ? other$given_name != null : !this$given_name.equals(other$given_name)) return false;
        final java.lang.Object this$surname = this.getSurname();
        final java.lang.Object other$surname = other.getSurname();
        if (this$surname == null ? other$surname != null : !this$surname.equals(other$surname)) return false;
        final java.lang.Object this$middle_name = this.getMiddle_name();
        final java.lang.Object other$middle_name = other.getMiddle_name();
        if (this$middle_name == null ? other$middle_name != null : !this$middle_name.equals(other$middle_name)) return false;
        final java.lang.Object this$suffix = this.getSuffix();
        final java.lang.Object other$suffix = other.getSuffix();
        if (this$suffix == null ? other$suffix != null : !this$suffix.equals(other$suffix)) return false;
        final java.lang.Object this$alternate_full_name = this.getAlternate_full_name();
        final java.lang.Object other$alternate_full_name = other.getAlternate_full_name();
        if (this$alternate_full_name == null ? other$alternate_full_name != null : !this$alternate_full_name.equals(other$alternate_full_name)) return false;
        final java.lang.Object this$full_name = this.getFull_name();
        final java.lang.Object other$full_name = other.getFull_name();
        if (this$full_name == null ? other$full_name != null : !this$full_name.equals(other$full_name)) return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof Name;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + super.hashCode();
        final java.lang.Object $prefix = this.getPrefix();
        result = result * PRIME + ($prefix == null ? 43 : $prefix.hashCode());
        final java.lang.Object $given_name = this.getGiven_name();
        result = result * PRIME + ($given_name == null ? 43 : $given_name.hashCode());
        final java.lang.Object $surname = this.getSurname();
        result = result * PRIME + ($surname == null ? 43 : $surname.hashCode());
        final java.lang.Object $middle_name = this.getMiddle_name();
        result = result * PRIME + ($middle_name == null ? 43 : $middle_name.hashCode());
        final java.lang.Object $suffix = this.getSuffix();
        result = result * PRIME + ($suffix == null ? 43 : $suffix.hashCode());
        final java.lang.Object $alternate_full_name = this.getAlternate_full_name();
        result = result * PRIME + ($alternate_full_name == null ? 43 : $alternate_full_name.hashCode());
        final java.lang.Object $full_name = this.getFull_name();
        result = result * PRIME + ($full_name == null ? 43 : $full_name.hashCode());
        return result;
    }

}
