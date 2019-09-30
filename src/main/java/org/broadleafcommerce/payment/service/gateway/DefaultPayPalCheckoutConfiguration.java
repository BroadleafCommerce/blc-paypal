package org.broadleafcommerce.payment.service.gateway;

import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCheckoutPaymentGatewayType;
import org.broadleafcommerce.vendor.paypal.service.payment.type.PayPalShippingDisplayType;
import org.springframework.core.env.Environment;

import com.broadleafcommerce.paymentgateway.domain.enums.PaymentGatewayType;
import com.broadleafcommerce.paymentgateway.service.configuration.AbstractPaymentGatewayConfiguration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@RequiredArgsConstructor
public class DefaultPayPalCheckoutConfiguration extends AbstractPaymentGatewayConfiguration
        implements PayPalCheckoutConfiguration {

    private final Environment env;

    protected int failureReportingThreshold = 1;
    protected boolean performAuthorizeAndCapture = false;

    @Override
    public String getReturnUrl() {
        String url = env.getProperty("gateway.paypal.checkout.rest.returnUrl");
        try {
            URI u = new URI(url);
            if (u.isAbsolute()) {
                return url;
            } else {
                return null; // TODO: what to do with the baseUrl?
                // String baseUrl = urlResolver.getSiteBaseUrl();
                // return baseUrl + url;
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                    "The value for 'gateway.paypal.checkout.rest.returnUrl' is not valid.",
                    e);
        }
    }

    @Override
    public String getCancelUrl() {
        String url = env.getProperty("gateway.paypal.checkout.rest.cancelUrl");
        try {
            URI u = new URI(url);
            if (u.isAbsolute()) {
                return url;
            } else {
                return null; // TODO: what to do with the baseUrl?
                // String baseUrl = urlResolver.getSiteBaseUrl();
                // return baseUrl + url;
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                    "The value for 'gateway.paypal.checkout.rest.cancelUrl' is not valid.",
                    e);
        }
    }

    @Override
    public String getWebProfileId() {
        return env.getProperty("gateway.paypal.checkout.rest.webProfileId");
    }

    @Override
    public String getPaymentDescription() {
        return env.getProperty("gateway.paypal.checkout.rest.description");
    }

    @Override
    public String getSmartPaymentEnvironment() {
        return env.getProperty("gateway.paypal.smart.button.env");
    }

    @Override
    public PayPalShippingDisplayType getShippingDisplayType() {
        String shippingType = env.getProperty("gateway.paypal.expressCheckout.shippingDisplayType");

        PayPalShippingDisplayType displayType =
                PayPalShippingDisplayType.getByTypeKey(shippingType);
        if (displayType != null) {
            return displayType;
        }

        return PayPalShippingDisplayType.NO_DISPLAY;
    }

    @Override
    public String getTotalType() {
        return MessageConstants.TOTAL;
    }

    @Override
    public Map<String, String> getAdditionalConfig() {
        Map<String, String> additionalConfigs = new HashMap<>();
        additionalConfigs.put("HDRBORDERCOLOR", "FFFFFF");
        additionalConfigs.put("HDRBACKCOLOR", "FFFFFF");
        additionalConfigs.put("PAYFLOWCOLOR", "FFFFFF");
        return additionalConfigs;
    }

    @Override
    public Map<String, String> getAdditionalCustomFields() {
        // intentionally unimplemented, used as an extension point
        return new HashMap<>();
    }

    @Override
    public String getCheckoutRestClientId() {
        return env.getProperty("gateway.paypal.checkout.rest.clientId");
    }

    @Override
    public String getCheckoutRestSecret() {
        return env.getProperty("gateway.paypal.checkout.rest.secret");
    }

    @Override
    public String getCheckoutRestMode() {
        return env.getProperty("gateway.paypal.checkout.rest.mode");
    }

    @Override
    public boolean handlesAuthorize() {
        return true;
    }

    @Override
    public boolean handlesCapture() {
        return true;
    }

    @Override
    public boolean handlesAuthorizeAndCapture() {
        return true;
    }

    @Override
    public boolean handlesReverseAuthorize() {
        return true;
    }

    @Override
    public boolean handlesVoid() {
        return true;
    }

    @Override
    public boolean handlesRefund() {
        return true;
    }

    @Override
    public boolean handlesPartialCapture() {
        return false;
    }

    @Override
    public boolean handlesMultipleShipment() {
        return false;
    }

    @Override
    public boolean handlesRecurringPayment() {
        return false;
    }

    @Override
    public boolean handlesSavedCustomerPayment() {
        return false;
    }

    @Override
    public boolean isPerformAuthorizeAndCapture() {
        return performAuthorizeAndCapture;
    }

    @Override
    public void setPerformAuthorizeAndCapture(boolean performAuthorizeAndCapture) {
        this.performAuthorizeAndCapture = performAuthorizeAndCapture;
    }

    @Override
    public int getFailureReportingThreshold() {
        return failureReportingThreshold;
    }

    @Override
    public void setFailureReportingThreshold(int failureReportingThreshold) {
        this.failureReportingThreshold = failureReportingThreshold;
    }

    @Override
    public boolean handlesMultiplePayments() {
        return false;
    }

    @Override
    public PaymentGatewayType getGatewayType() {
        return PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT;
    }

}
