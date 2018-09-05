package org.broadleafcommerce.vendor.paypal.service;

import org.broadleafcommerce.common.vendor.service.exception.PaymentException;

public interface PayPalWebProfileService {

    /**
     * Retrieves the id of the WebProfile to be used when creating a PayPal payment
     * 
     * @return
     * @throws PaymentException 
     */
    public String getWebProfileId() throws PaymentException;
}
