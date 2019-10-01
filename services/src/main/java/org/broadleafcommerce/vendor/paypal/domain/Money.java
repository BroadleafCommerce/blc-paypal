package org.broadleafcommerce.vendor.paypal.domain;

public class Money {

    private String currency_code;

    private String value;

    public String getCurrency_code() {
        return this.currency_code;
    }

    public Money setCurrency_code(String currency_code) {
        this.currency_code = currency_code;
        return this;
    }

    public String getValue() {
        return this.value;
    }

    public Money setValue(String value) {
        this.value = value;
        return this;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Money))
            return false;
        final Money other = (Money) o;
        if (!other.canEqual((java.lang.Object) this))
            return false;
        if (!super.equals(o))
            return false;
        final java.lang.Object this$currency_code = this.getCurrency_code();
        final java.lang.Object other$currency_code = other.getCurrency_code();
        if (this$currency_code == null ? other$currency_code != null
                : !this$currency_code.equals(other$currency_code))
            return false;
        final java.lang.Object this$value = this.getValue();
        final java.lang.Object other$value = other.getValue();
        if (this$value == null ? other$value != null : !this$value.equals(other$value))
            return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof Money;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + super.hashCode();
        final java.lang.Object $currency_code = this.getCurrency_code();
        result = result * PRIME + ($currency_code == null ? 43 : $currency_code.hashCode());
        final java.lang.Object $value = this.getValue();
        result = result * PRIME + ($value == null ? 43 : $value.hashCode());
        return result;
    }

}
