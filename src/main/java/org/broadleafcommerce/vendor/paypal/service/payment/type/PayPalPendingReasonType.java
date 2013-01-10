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
public class PayPalPendingReasonType implements Serializable, BroadleafEnumerationType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, PayPalPendingReasonType> TYPES = new HashMap<String, PayPalPendingReasonType>();

    public static final PayPalPendingReasonType NONE  = new PayPalPendingReasonType("none", "none");
    public static final PayPalPendingReasonType ADDRESS = new PayPalPendingReasonType("address", "address");
    public static final PayPalPendingReasonType AUTHORIZATION = new PayPalPendingReasonType("authorization", "authorization");
    public static final PayPalPendingReasonType ECHECK = new PayPalPendingReasonType("echeck", "echeck");
    public static final PayPalPendingReasonType INTL = new PayPalPendingReasonType("intl", "intl");
    public static final PayPalPendingReasonType MULTICURRENCY = new PayPalPendingReasonType("multi-currency", "multi-currency");
    public static final PayPalPendingReasonType ORDER = new PayPalPendingReasonType("order", "order");
    public static final PayPalPendingReasonType PAYMENTREVIEW = new PayPalPendingReasonType("paymentreview", "paymentreview");
    public static final PayPalPendingReasonType UNILATERAL = new PayPalPendingReasonType("unilateral", "unilateral");
    public static final PayPalPendingReasonType VERIFY = new PayPalPendingReasonType("verify", "verify");
    public static final PayPalPendingReasonType OTHER = new PayPalPendingReasonType("other", "other");

    public static PayPalPendingReasonType getInstance(final String type) {
        return TYPES.get(type);
    }

    private String type;
    private String friendlyType;

    public PayPalPendingReasonType() {
        //do nothing
    }

    public PayPalPendingReasonType(final String type, final String friendlyType) {
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
        PayPalPendingReasonType other = (PayPalPendingReasonType) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}
