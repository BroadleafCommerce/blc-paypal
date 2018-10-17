/*
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2018 Broadleaf Commerce
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

import org.broadleafcommerce.vendor.paypal.api.AgreementToken;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

public class PayPalCreateAgreementTokenRequest extends PayPalRequest {

    protected AgreementToken agreementToken;

    public PayPalCreateAgreementTokenRequest(AgreementToken agreementToken, APIContext apiContext) {
        super(apiContext);
        this.agreementToken = agreementToken;
    }

    @Override
    protected PayPalResponse executeInternal() throws PayPalRESTException {
        return new PayPalCreateAgreementTokenResponse(agreementToken.create(apiContext));
    }

    @Override
    protected boolean isRequestValid() {
        return (agreementToken != null && agreementToken.getDescription() != null &&
                agreementToken.getPayer() != null && agreementToken.getPlan() != null);
    }
}
