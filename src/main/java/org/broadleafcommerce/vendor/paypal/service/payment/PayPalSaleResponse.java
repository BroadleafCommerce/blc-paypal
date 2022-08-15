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

import com.paypal.api.payments.Payment;
import com.paypal.api.payments.Sale;

public class PayPalSaleResponse implements PayPalResponse {

    protected Payment salePayment;
    protected Sale sale;

    public PayPalSaleResponse(Payment salePayment) {
        this.salePayment = salePayment;
    }

    public Payment getSalePayment() {
        return salePayment;
    }

    public Sale getSale() {
        if (sale == null) {
            this.sale = salePayment.getTransactions().get(0).getRelatedResources().get(0).getSale();
        }
        return sale;
    }

}
