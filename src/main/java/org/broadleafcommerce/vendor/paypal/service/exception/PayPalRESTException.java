/*-
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2021 Broadleaf Commerce
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

/**
 * Represents an error response from a request against the PayPal REST APIs. The
 * {@link #getMessage()} will be a JSON string.
 *
 * @author Nathan Moore (nathandmoore)
 */
public class PayPalRESTException extends RuntimeException {

    public PayPalRESTException() {
        super();
    }

    public PayPalRESTException(String message) {
        super(message);
    }

    public PayPalRESTException(String message, Throwable cause) {
        super(message, cause);
    }

    public PayPalRESTException(Throwable cause) {
        super(cause);
    }

}
