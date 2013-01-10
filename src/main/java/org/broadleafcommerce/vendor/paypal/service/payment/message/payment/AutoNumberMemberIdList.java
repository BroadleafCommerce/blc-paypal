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

package org.broadleafcommerce.vendor.paypal.service.payment.message.payment;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 
 * @author jfischer
 *
 */
public class AutoNumberMemberIdList extends ArrayList<PayPalItemRequest> {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean add(PayPalItemRequest o) {
        boolean response = super.add(o);
        renumberIds();
        return response;
    }

    @Override
    public void add(int index, PayPalItemRequest element) {
        super.add(index, element);
        renumberIds();
    }

    @Override
    public boolean addAll(Collection<? extends PayPalItemRequest> c) {
        boolean response = super.addAll(c);
        renumberIds();
        return response;
    }

    @Override
    public boolean addAll(int index, Collection<? extends PayPalItemRequest> c) {
        boolean response = super.addAll(index, c);
        renumberIds();
        return response;
    }

    private void renumberIds() {
        long id = 0;
        for (PayPalItemRequest itemRequest : this) {
            itemRequest.setId(id);
            id++;
        }
    }

}
