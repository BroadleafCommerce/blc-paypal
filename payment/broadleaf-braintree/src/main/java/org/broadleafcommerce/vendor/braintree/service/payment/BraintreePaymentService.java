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

import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.common.vendor.service.type.ServiceStatusType;
import org.broadleafcommerce.vendor.braintree.service.payment.type.BraintreeMethodType;

/**
 * Created by IntelliJ IDEA.
 * User: chadharchar
 * Date: 4/2/12
 * Time: 9:14 AM
 * To change this template use File | Settings | File Templates.
 */
public interface BraintreePaymentService {

    public Result<Transaction> process(BraintreePaymentRequest paymentRequest) throws PaymentException;
    
    public String makeTrData(TransactionRequest request);

    public String makeTrUrl();

    public ServiceStatusType getServiceStatus();

    public Integer getFailureReportingThreshold();

    public void setFailureReportingThreshold(Integer failureReportingThreshold);

    public String getServerUrl();

    public void setServerUrl(String serverUrl);
}
