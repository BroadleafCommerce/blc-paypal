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

import com.fasterxml.jackson.annotation.JsonAlias;

import java.io.Serializable;

/**
 * @author Nathan Moore (nathandmoore)
 */
public class PayPalErrorResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    private String message;

    @JsonAlias({"debug_id"})
    private String debugId;

    @JsonAlias({"information_link"})
    private String informationLink;

    public PayPalErrorResponse() {
    }

    public String getName() {
        return this.name;
    }

    public String getMessage() {
        return this.message;
    }

    public String getDebugId() {
        return this.debugId;
    }

    public String getInformationLink() {
        return this.informationLink;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDebugId(String debugId) {
        this.debugId = debugId;
    }

    public void setInformationLink(String informationLink) {
        this.informationLink = informationLink;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof PayPalErrorResponse)) return false;
        final PayPalErrorResponse other = (PayPalErrorResponse) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final Object this$message = this.getMessage();
        final Object other$message = other.getMessage();
        if (this$message == null ? other$message != null : !this$message.equals(other$message)) return false;
        final Object this$debugId = this.getDebugId();
        final Object other$debugId = other.getDebugId();
        if (this$debugId == null ? other$debugId != null : !this$debugId.equals(other$debugId)) return false;
        final Object this$informationLink = this.getInformationLink();
        final Object other$informationLink = other.getInformationLink();
        if (this$informationLink == null ? other$informationLink != null : !this$informationLink.equals(other$informationLink))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof PayPalErrorResponse;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $message = this.getMessage();
        result = result * PRIME + ($message == null ? 43 : $message.hashCode());
        final Object $debugId = this.getDebugId();
        result = result * PRIME + ($debugId == null ? 43 : $debugId.hashCode());
        final Object $informationLink = this.getInformationLink();
        result = result * PRIME + ($informationLink == null ? 43 : $informationLink.hashCode());
        return result;
    }

    public String toString() {
        return "PayPalErrorResponse(name=" + this.getName() + ", message=" + this.getMessage() + ", debugId=" + this.getDebugId() + ", informationLink=" + this.getInformationLink() + ")";
    }
}
