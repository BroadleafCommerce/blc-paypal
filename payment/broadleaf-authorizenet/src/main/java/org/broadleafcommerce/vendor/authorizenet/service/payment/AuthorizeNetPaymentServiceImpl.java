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

import org.broadleafcommerce.common.vendor.service.monitor.ServiceStatusDetectable;
import org.broadleafcommerce.common.vendor.service.type.ServiceStatusType;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: elbertbautista
 * Date: 6/27/12
 * Time: 11:17 AM
 */
public class AuthorizeNetPaymentServiceImpl implements AuthorizeNetPaymentService, ServiceStatusDetectable<AuthorizeNetPaymentRequest> {

    protected Integer failureReportingThreshold;
    protected Integer failureCount = 0;
    protected Boolean isUp = true;
    protected AuthorizeNetGatewayRequest gatewayRequest;

    protected synchronized void clearStatus() {
        isUp = true;
        failureCount = 0;
    }

    protected synchronized void incrementFailure() {
        if (failureCount >= failureReportingThreshold) {
            isUp = false;
        } else {
            failureCount++;
        }
    }

    @Override
    public ServiceStatusType getServiceStatus() {
        if (isUp) {
            return ServiceStatusType.UP;
        } else {
            return ServiceStatusType.DOWN;
        }
    }

    @Override
    public String getServiceName() {
        return getClass().getName();
    }

    @Override
    public Object process(AuthorizeNetPaymentRequest arg) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected String communicateWithVendor(AuthorizeNetPaymentRequest paymentRequest) throws IOException {
        return null;
    }

    public Integer getFailureReportingThreshold() {
        return failureReportingThreshold;
    }

    public void setFailureReportingThreshold(Integer failureReportingThreshold) {
        this.failureReportingThreshold = failureReportingThreshold;
    }

    public AuthorizeNetGatewayRequest getGatewayRequest() {
        return gatewayRequest;
    }

    public void setGatewayRequest(AuthorizeNetGatewayRequest gatewayRequest) {
        this.gatewayRequest = gatewayRequest;
    }
}
