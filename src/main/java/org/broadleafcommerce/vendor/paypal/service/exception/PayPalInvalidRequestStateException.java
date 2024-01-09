/*-
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2024 Broadleaf Commerce
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
package org.broadleafcommerce.vendor.paypal.service.exception;

import org.broadleafcommerce.vendor.paypal.service.payment.AbstractPayPalRequest;

/**
 * Represents an exception that occurs because a {@link AbstractPayPalRequest PayPalRequest's} state
 * was invalid prior to sending the request.
 *
 * @author Nathan Moore (nathandmoore)
 */
public class PayPalInvalidRequestStateException extends RuntimeException {

    public PayPalInvalidRequestStateException() {
        super();
    }

    public PayPalInvalidRequestStateException(String message) {
        super(message);
    }

    public PayPalInvalidRequestStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public PayPalInvalidRequestStateException(Throwable cause) {
        super(cause);
    }
}
