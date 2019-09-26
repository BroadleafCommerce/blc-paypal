package org.broadleafcommerce.vendor.paypal.api;

import com.paypal.api.payments.Address;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Plan;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.HttpMethod;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.base.rest.PayPalResource;

import java.util.List;

/**
 * The PayPal REST SDK does not currently contain support for Reference Transactions and Merchant
 * Initiated Billing Agreements
 *
 * This was created to support the need to call the Billing Agreement Token endpoints See:
 * https://developer.paypal.com/docs/limited-release/reference-transactions/#overview
 */
public class AgreementToken extends PayPalResource {

    /**
     * Identifier of the agreement.
     */
    private String id;
    /**
     * Description of the agreement.
     */
    private String description;
    /**
     * Details of the buyer who is enrolling in this agreement. This information is gathered from
     * execution of the approval URL.
     */
    private Payer payer;
    /**
     * Plan details for this agreement.
     */
    private Plan plan;
    /**
     * Shipping address object of the agreement, which should be provided if it is different from
     * the default address.
     */
    private Address shippingAddress;
    /**
     * Payment token
     */
    private String tokenId;
    private List<Links> links;

    public AgreementToken() {}

    public AgreementToken(String tokenId) {
        this.tokenId = tokenId;
    }

    public AgreementToken(String description, Payer payer, Plan plan) {
        this.description = description;
        this.payer = payer;
        this.plan = plan;
    }

    /**
     * Create a new billing agreement token by passing the details for the agreement, including the
     * description, payer, and billing plan in the request JSON.
     * 
     * @param apiContext {@link APIContext} used for the API call.
     * @return AgreementToken
     * @throws PayPalRESTException
     */
    public AgreementToken create(APIContext apiContext) throws PayPalRESTException {
        String resourcePath = "v1/billing-agreements/agreement-tokens";
        String payLoad = this.toJSON();
        AgreementToken agreementToken = (AgreementToken) configureAndExecute(apiContext,
                HttpMethod.POST, resourcePath, payLoad, AgreementToken.class);
        return agreementToken;
    }

    /**
     * Execute a billing agreement after buyer approval by passing the payment token to the request
     * URI.
     * 
     * @param apiContext {@link APIContext} used for the API call.
     * @param tokenId payment tokenId (e.g., BA-8A802366G0648845Y)
     * @return AgreementToken
     * @throws PayPalRESTException
     */
    public static AgreementToken execute(AgreementToken agreementToken, APIContext apiContext)
            throws PayPalRESTException {
        String resourcePath = "v1/billing-agreements/agreements";
        String payLoad = agreementToken.toJSON();
        return (AgreementToken) configureAndExecute(apiContext, HttpMethod.POST, resourcePath,
                payLoad, AgreementToken.class);
    }

    public String getId() {
        return id;
    }

    public AgreementToken setId(String id) {
        this.id = id;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AgreementToken setDescription(String description) {
        this.description = description;
        return this;
    }

    public Payer getPayer() {
        return payer;
    }

    public AgreementToken setPayer(Payer payer) {
        this.payer = payer;
        return this;
    }

    public Plan getPlan() {
        return plan;
    }

    public AgreementToken setPlan(Plan plan) {
        this.plan = plan;
        return this;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public AgreementToken setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
        return this;
    }

    public String getTokenId() {
        return tokenId;
    }

    public AgreementToken setTokenId(String tokenId) {
        this.tokenId = tokenId;
        return this;
    }

    public List<Links> getLinks() {
        return links;
    }

    public AgreementToken setLinks(List<Links> links) {
        this.links = links;
        return this;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this)
            return true;
        if (!(o instanceof AgreementToken))
            return false;
        final AgreementToken other = (AgreementToken) o;
        if (!other.canEqual((java.lang.Object) this))
            return false;
        if (!super.equals(o))
            return false;
        final java.lang.Object this$id = this.getId();
        final java.lang.Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id))
            return false;
        final java.lang.Object this$description = this.getDescription();
        final java.lang.Object other$description = other.getDescription();
        if (this$description == null ? other$description != null
                : !this$description.equals(other$description))
            return false;
        final java.lang.Object this$payer = this.getPayer();
        final java.lang.Object other$payer = other.getPayer();
        if (this$payer == null ? other$payer != null : !this$payer.equals(other$payer))
            return false;
        final java.lang.Object this$shippingAddress = this.getShippingAddress();
        final java.lang.Object other$shippingAddress = other.getShippingAddress();
        if (this$shippingAddress == null ? other$shippingAddress != null
                : !this$shippingAddress.equals(other$shippingAddress))
            return false;
        final java.lang.Object this$plan = this.getPlan();
        final java.lang.Object other$plan = other.getPlan();
        if (this$plan == null ? other$plan != null : !this$plan.equals(other$plan))
            return false;
        final java.lang.Object this$tokenId = this.getTokenId();
        final java.lang.Object other$tokenId = other.getTokenId();
        if (this$tokenId == null ? other$tokenId != null : !this$tokenId.equals(other$tokenId))
            return false;
        final java.lang.Object this$links = this.getLinks();
        final java.lang.Object other$links = other.getLinks();
        if (this$links == null ? other$links != null : !this$links.equals(other$links))
            return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof AgreementToken;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + super.hashCode();
        final java.lang.Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final java.lang.Object $description = this.getDescription();
        result = result * PRIME + ($description == null ? 43 : $description.hashCode());
        final java.lang.Object $payer = this.getPayer();
        result = result * PRIME + ($payer == null ? 43 : $payer.hashCode());
        final java.lang.Object $shippingAddress = this.getShippingAddress();
        result = result * PRIME + ($shippingAddress == null ? 43 : $shippingAddress.hashCode());
        final java.lang.Object $plan = this.getPlan();
        result = result * PRIME + ($plan == null ? 43 : $plan.hashCode());
        final java.lang.Object $tokenId = this.getTokenId();
        result = result * PRIME + ($tokenId == null ? 43 : $tokenId.hashCode());
        final java.lang.Object $links = this.getLinks();
        result = result * PRIME + ($links == null ? 43 : $links.hashCode());
        return result;
    }
}
