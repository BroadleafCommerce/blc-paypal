package org.broadleafcommerce.payment.service.gateway;

import org.apache.commons.lang3.StringUtils;
import org.broadleafcommerce.vendor.paypal.service.PayPalWebExperienceProfileService;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

/**
 * @author Elbert Bautista (elbertbautista)
 * @author Chris Kittrell
 */
@Getter
@Setter
@CommonsLog
@ConfigurationProperties("broadleaf.paypalcheckout.rest")
public class PayPalCheckoutRestConfigurationProperties {

    @PostConstruct
    public void init() {
        if (StringUtils.isBlank(mode)) {
            log.error(
                    "The PayPal mode (live vs sandbox) must be provided via the 'broadleaf.paypalcheckout.rest.mode' property.");
        }
        if (StringUtils.isBlank(clientId)) {
            log.error(
                    "The PayPal client id must be provided via the 'broadleaf.paypalcheckout.rest.client-id' property.");
        }
        if (StringUtils.isBlank(clientSecret)) {
            log.error(
                    "The PayPal client secret must be provided via the 'broadleaf.paypalcheckout.rest.client-secret' property.");
        }
    }

    /**
     * URL to which the buyer's browser is returned after choosing to pay with PayPal. For digital
     * goods, you must add JavaScript to this page to close the in-context experience. Note: PayPal
     * recommends that the value be the final review page on which the buyer confirms the order and
     * payment or billing agreement.
     *
     * Character length and limitations: 2048 single-byte characters
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String returnUrl;

    /**
     * URL to which the buyer is returned if the buyer does not approve the use of PayPal to pay
     * you. For digital goods, you must add JavaScript to this page to close the in-context
     * experience. Note: PayPal recommends that the value be the original page on which the buyer
     * chose to pay with PayPal or establish a billing agreement.
     *
     * Character length and limitations: 2048 single-byte characters
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String cancelUrl;

    /**
     * Identifies whether production ("live") or non-production/test ("sandbox") transactions should
     * be made.
     *
     * @return the execution mode (sandbox vs live) of transactions through the PayPal API
     */
    private String mode = "sandbox";

    /**
     * The WebProfile to be used when creating payments. For more information on WebProfiles go to
     * {@link https://developer.paypal.com/docs/integration/direct/payment-experience/}.
     * {@link PayPalWebExperienceProfileService#getWebExperienceProfileId(PaymentRequest)} should be
     * used instead if you want to find the web profile id to create a payment since it has the
     * ability to create new WebProfiles based on injected beans along with using this method
     *
     * @return The WebProfile to be used when creating payments
     */
    private String webProfileId;

    /**
     * The client id provided by PayPal that is used in API calls to identify the charging
     * company/entity.
     *
     * @return The client id provided by PayPal that is used in API calls to identify the charging
     *         company/entity.
     */
    private String clientId;

    /**
     * The client secret provided by PayPal that is used in API calls to verify the applications
     * usage of the client id.
     *
     * @return The client secret provided by PayPal that is used in API calls to verify the
     *         applications usage of the client id.
     */
    private String clientSecret;

    /**
     * Simple description of the entity that is charging the customer. For example, this may read as
     * "My Test Store".
     *
     * Note, this is the transaction description that will be listed on the customer's credit card
     * statement.
     *
     * @return Simple description of the entity that is charging the customer
     */
    private String paymentDescription;

    /**
     * Type declaration for the label to be displayed in MiniCart for UX. It is one of the following
     * values: - Total - EstimatedTotal
     *
     * @return String
     */
    private String totalType = MessageConstants.TOTAL;

    /**
     * Whether or not we should populate each cart item's shipping address if it's already known
     * when we initially create the PayPal Payment.
     *
     * @return boolean
     */
    @Getter(AccessLevel.NONE)
    private boolean shouldPopulateShippingOnCreatePayment = true;

    /**
     * The Paypal NVP API only allows a single field with custom logic in it:
     * PAYMENTREQUEST_n_CUSTOM. Because of this, all of the fields returned here are serialized
     * together like so:
     *
     * {@code ccoc=true_12345|key1=value1|key2=value2|key3=value3}
     *
     * Note that Broadleaf uses a piece of this to determine if we should complete checkout on
     * callback or not. This is done as "ccoc=true_12345" where {@code true} is the value of
     * {@link PaymentRequest#isCompleteCheckoutOnCallback()}. So, the minimum string that will be
     * contained in the custom field is {@code ccoc=true_12345}, plus whatever other fields you
     * have.
     *
     * Also note that the entire custom field string after serialization is 256 characters. An
     * IllegalArgumentException will be thrown otherwise.
     */
    private Map<String, String> additionalCustomFields = new HashMap<>();


    public String getReturnUrl(@NonNull PaymentRequest paymentRequest) {
        if (isAbsoluteUrl(returnUrl)) {
            return returnUrl;
        } else {
            String siteBaseUrl = paymentRequest.getSiteBaseUrl();

            if (StringUtils.isNotBlank(siteBaseUrl)) {
                return siteBaseUrl + returnUrl;
            } else {
                throw new IllegalArgumentException(
                        "Since the value provided for 'broadleaf.paypalcheckout.rest.returnUrl' is a relative url, a siteBaseUrl must be provided on the PaymentRequest.");
            }
        }
    }

    public String getCancelUrl(@NonNull PaymentRequest paymentRequest) {
        if (isAbsoluteUrl(cancelUrl)) {
            return cancelUrl;
        } else {
            String siteBaseUrl = paymentRequest.getSiteBaseUrl();

            if (StringUtils.isNotBlank(siteBaseUrl)) {
                return siteBaseUrl + cancelUrl;
            } else {
                throw new IllegalArgumentException(
                        "Since the value provided for 'broadleaf.paypalcheckout.rest.cancelUrl' is a relative url, a siteBaseUrl must be provided on the PaymentRequest.");
            }
        }
    }

    protected boolean isAbsoluteUrl(String url) {
        try {
            URI uri = new URI(url);

            return uri.isAbsolute();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("The provided url (" + url + ") is not valid.", e);
        }
    }

    public boolean shouldPopulateShippingOnCreatePayment() {
        return shouldPopulateShippingOnCreatePayment;
    }

}
