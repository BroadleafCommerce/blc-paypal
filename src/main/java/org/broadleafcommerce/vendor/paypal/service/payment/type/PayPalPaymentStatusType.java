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
package org.broadleafcommerce.vendor.paypal.service.payment.type;

import org.broadleafcommerce.common.BroadleafEnumerationType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * An extendible enumeration of transaction types.
 * 
 * @author jfischer
 */
public class PayPalPaymentStatusType implements Serializable, BroadleafEnumerationType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, PayPalPaymentStatusType> TYPES = new HashMap<String, PayPalPaymentStatusType>();

    public static final PayPalPaymentStatusType NONE  = new PayPalPaymentStatusType("None", "None");
    public static final PayPalPaymentStatusType CANCELEDREVERSAL = new PayPalPaymentStatusType("Canceled-Reversal", "Canceled-Reversal");
    public static final PayPalPaymentStatusType COMPLETED = new PayPalPaymentStatusType("Completed", "Completed");
    public static final PayPalPaymentStatusType DENIED = new PayPalPaymentStatusType("Denied", "Denied");
    public static final PayPalPaymentStatusType EXPIRED = new PayPalPaymentStatusType("Expired", "Expired");
    public static final PayPalPaymentStatusType FAILED = new PayPalPaymentStatusType("Failed", "Failed");
    public static final PayPalPaymentStatusType INPROGRESS = new PayPalPaymentStatusType("In-Progress", "In-Progress");
    public static final PayPalPaymentStatusType PARTIALLYREFUNDED = new PayPalPaymentStatusType("Partially-Refunded", "Partially-Refunded");
    public static final PayPalPaymentStatusType PENDING = new PayPalPaymentStatusType("Pending", "Pending");
    public static final PayPalPaymentStatusType REFUNDED = new PayPalPaymentStatusType("Refunded", "Refunded");
    public static final PayPalPaymentStatusType REVERSED = new PayPalPaymentStatusType("Reversed", "Reversed");
    public static final PayPalPaymentStatusType PROCESSED = new PayPalPaymentStatusType("Processed", "Processed");
    public static final PayPalPaymentStatusType VOIDED = new PayPalPaymentStatusType("Voided", "Voided");
    public static final PayPalPaymentStatusType COMPLETEDFUNDSHELD = new PayPalPaymentStatusType("Completed-Funds-Held", "Completed-Funds-Held");

    public static PayPalPaymentStatusType getInstance(final String type) {
        return TYPES.get(type);
    }

    private String type;
    private String friendlyType;

    public PayPalPaymentStatusType() {
        //do nothing
    }

    public PayPalPaymentStatusType(final String type, final String friendlyType) {
        this.friendlyType = friendlyType;
        setType(type);
    }

    public String getType() {
        return type;
    }

    public String getFriendlyType() {
        return friendlyType;
    }

    private void setType(final String type) {
        this.type = type;
        if (!TYPES.containsKey(type)) {
            TYPES.put(type, this);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PayPalPaymentStatusType other = (PayPalPaymentStatusType) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}
