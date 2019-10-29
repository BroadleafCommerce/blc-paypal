package org.broadleafcommerce.vendor.paypal.service;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;

public interface PayPalWebExperienceProfileService {

    /**
     * Retrieves the id of the WebProfile to be used when creating a PayPal payment
     * 
     * @return
     * @throws PaymentException
     */
    public String getWebExperienceProfileId(PaymentRequest paymentRequest) throws PaymentException;

}
