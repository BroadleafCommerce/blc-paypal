package org.broadleafcommerce.vendor.braintree.service.payment;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Environment;

/**
 * Created by IntelliJ IDEA.
 * User: chadharchar
 * Date: 3/22/12
 * Time: 5:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class BraintreeGatewayRequestImpl implements BraintreeGatewayRequest{


    protected String publicKey;
    protected String privateKey;
    protected String merchantId;
    protected String redirectUrl;
    protected String transactionType;

    @Override
    public BraintreeGateway buildRequest() {
        return new BraintreeGateway(
                Environment.SANDBOX,
                merchantId,
                publicKey,
                privateKey);
    }

    @Override
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String getPublicKey() {
        return publicKey;
    }

    @Override
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public String getPrivateKey() {
        return privateKey;
    }

    @Override
    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    @Override
    public String getMerchantId() {
        return merchantId;
    }

    @Override
    public String getRedirectUrl() {
        return redirectUrl;
    }

    @Override
    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    @Override
    public String getTransactionType() {
        return transactionType;
    }

    @Override
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

}
