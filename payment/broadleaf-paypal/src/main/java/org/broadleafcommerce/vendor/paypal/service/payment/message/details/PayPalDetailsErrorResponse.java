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

package org.broadleafcommerce.vendor.paypal.service.payment.message.details;

import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalErrorResponse;

/**
 * @author Jeff Fischer
 */
public class PayPalDetailsErrorResponse extends PayPalErrorResponse {
    
    protected String ack;

    public String getAck() {
        return ack;
    }

    public void setAck(String ack) {
        this.ack = ack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PayPalDetailsErrorResponse)) return false;
        if (!super.equals(o)) return false;

        PayPalDetailsErrorResponse that = (PayPalDetailsErrorResponse) o;

        if (ack != null ? !ack.equals(that.ack) : that.ack != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (ack != null ? ack.hashCode() : 0);
        return result;
    }
}
