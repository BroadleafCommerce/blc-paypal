/*
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2014 Broadleaf Commerce
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
package org.broadleafcommerce.vendor.paypal.service.payment.message;

import java.io.Serializable;

/**
 * @author Jeff Fischer
 */
public class PayPalErrorResponse implements Serializable {
    
    protected String errorCode;
    protected String shortMessage;
    protected String longMessage;
    protected String severityCode;
    protected String ack;

    public String getAck() {
        return ack;
    }

    public void setAck(String ack) {
        this.ack = ack;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getLongMessage() {
        return longMessage;
    }

    public void setLongMessage(String longMessage) {
        this.longMessage = longMessage;
    }

    public String getSeverityCode() {
        return severityCode;
    }

    public void setSeverityCode(String severityCode) {
        this.severityCode = severityCode;
    }

    public String getShortMessage() {
        return shortMessage;
    }

    public void setShortMessage(String shortMessage) {
        this.shortMessage = shortMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PayPalErrorResponse)) return false;

        PayPalErrorResponse that = (PayPalErrorResponse) o;

        if (ack != null ? !ack.equals(that.ack) : that.ack != null) return false;
        if (errorCode != null ? !errorCode.equals(that.errorCode) : that.errorCode != null) return false;
        if (longMessage != null ? !longMessage.equals(that.longMessage) : that.longMessage != null) return false;
        if (severityCode != null ? !severityCode.equals(that.severityCode) : that.severityCode != null) return false;
        if (shortMessage != null ? !shortMessage.equals(that.shortMessage) : that.shortMessage != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = errorCode != null ? errorCode.hashCode() : 0;
        result = 31 * result + (shortMessage != null ? shortMessage.hashCode() : 0);
        result = 31 * result + (longMessage != null ? longMessage.hashCode() : 0);
        result = 31 * result + (severityCode != null ? severityCode.hashCode() : 0);
        result = 31 * result + (ack != null ? ack.hashCode() : 0);
        return result;
    }
}
