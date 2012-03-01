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

package org.broadleafcommerce.vendor.paypal.service.payment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.common.vendor.service.type.ServiceStatusType;
import org.broadleafcommerce.profile.core.service.IdGenerationService;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalPaymentRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.message.PayPalPaymentResponse;

/**
 * 
 * @author jfischer
 *
 */
public class PayPalPaymentServiceImpl implements PayPalPaymentService {
	
	private static final Log LOG = LogFactory.getLog(PayPalPaymentServiceImpl.class);

    protected String merchantId;
    protected String serverUrl;
    protected Integer failureReportingThreshold;
    protected Integer failureCount = 0;
    protected Boolean isUp = true;
    protected IdGenerationService idGenerationService;

    protected void clearStatus() {
        synchronized(failureCount) {
            isUp = true;
            failureCount = 0;
        }
    }

    protected void incrementFailure() {
        synchronized(failureCount) {
            if (failureCount >= failureReportingThreshold) {
                isUp = false;
            } else {
                failureCount++;
            }
        }
    }

    @Override
    public PayPalPaymentResponse process(PayPalPaymentRequest paymentRequest) throws PaymentException {
        //TODO put actual paypal communication code here
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Integer getFailureReportingThreshold() {
        return failureReportingThreshold;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public ServiceStatusType getServiceStatus() {
        synchronized(failureCount) {
            if (isUp) {
                return ServiceStatusType.UP;
            } else {
                return ServiceStatusType.DOWN;
            }
        }
    }

    public void setFailureReportingThreshold(Integer failureReportingThreshold) {
        this.failureReportingThreshold = failureReportingThreshold;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public IdGenerationService getIdGenerationService() {
        return idGenerationService;
    }

    public void setIdGenerationService(IdGenerationService idGenerationService) {
        this.idGenerationService = idGenerationService;
    }
}
