/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    protected Environment environment;

    @Override
    public BraintreeGateway buildRequest() {
        return new BraintreeGateway(
                environment,
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

    @Override
    public String getEnvironment() {
        return environment.name();
    }

    @Override
    public void setEnvironment(String environment) {
        this.environment = Environment.valueOf(environment);
    }
}
