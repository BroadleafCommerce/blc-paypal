/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.vendor.authorizenet.service.payment;

/**
 * Created with IntelliJ IDEA.
 * User: elbertbautista
 * Date: 6/27/12
 * Time: 11:49 AM
 */
public class AuthorizeNetGatewayRequestImpl implements AuthorizeNetGatewayRequest {

    protected String apiLoginId;
    protected String transactionKey;
    protected String relayResponseUrl;

    public String getApiLoginId() {
        return apiLoginId;
    }

    public void setApiLoginId(String apiLoginId) {
        this.apiLoginId = apiLoginId;
    }

    public String getTransactionKey() {
        return transactionKey;
    }

    public void setTransactionKey(String transactionKey) {
        this.transactionKey = transactionKey;
    }

    public String getRelayResponseUrl() {
        return relayResponseUrl;
    }

    public void setRelayResponseUrl(String relayResponseUrl) {
        this.relayResponseUrl = relayResponseUrl;
    }
}
