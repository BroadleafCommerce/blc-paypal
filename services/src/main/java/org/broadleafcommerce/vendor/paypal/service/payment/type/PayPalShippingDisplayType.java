package org.broadleafcommerce.vendor.paypal.service.payment.type;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author elbertbautista
 */
@Getter
@AllArgsConstructor
public enum PayPalShippingDisplayType implements Serializable {

    PROVIDE_SHIPPING("0", "Provide Shipping"),
    NO_DISPLAY("1", "No Display"),
    CAPTURE_SHIPPING("2", "Capture Shipping");

    private String typeKey;
    private String friendlyName;

    public static PayPalShippingDisplayType getByTypeKey(String typeKey) {
        return Arrays.stream(values())
                .filter(value -> Objects.equals(value.getTypeKey(), typeKey))
                .findFirst()
                .orElse(null);
    }

}
