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
