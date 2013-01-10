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
