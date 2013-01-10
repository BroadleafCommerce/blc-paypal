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
public class PayPalReasonCodeType implements Serializable, BroadleafEnumerationType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, PayPalReasonCodeType> TYPES = new HashMap<String, PayPalReasonCodeType>();

    public static final PayPalReasonCodeType NONE  = new PayPalReasonCodeType("none", "none");
    public static final PayPalReasonCodeType CHARGEBACK = new PayPalReasonCodeType("chargeback", "chargeback");
    public static final PayPalReasonCodeType GUARANTEE = new PayPalReasonCodeType("guarantee", "guarantee");
    public static final PayPalReasonCodeType BUYERCOMPLAINT = new PayPalReasonCodeType("buyer-complaint", "buyer-complaint");
    public static final PayPalReasonCodeType REFUND = new PayPalReasonCodeType("refund", "refund");
    public static final PayPalReasonCodeType OTHER = new PayPalReasonCodeType("other", "other");

    public static PayPalReasonCodeType getInstance(final String type) {
        return TYPES.get(type);
    }

    private String type;
    private String friendlyType;

    public PayPalReasonCodeType() {
        //do nothing
    }

    public PayPalReasonCodeType(final String type, final String friendlyType) {
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
        PayPalReasonCodeType other = (PayPalReasonCodeType) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}
