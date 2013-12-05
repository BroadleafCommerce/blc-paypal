/*
 * Copyright 2008-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.payment.service.gateway;

import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.PaymentGatewayRollbackService;
import org.springframework.stereotype.Service;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalExpressRollbackService")
public class PayPalExpressRollbackServiceImpl implements PaymentGatewayRollbackService {

    @Override
    public PaymentResponseDTO rollbackAuthorize(PaymentResponseDTO originalResponse) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PaymentResponseDTO rollbackCapture(PaymentResponseDTO originalResponse) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PaymentResponseDTO rollbackAuthorizeAndCapture(PaymentResponseDTO originalResponse) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PaymentResponseDTO rollbackRefund(PaymentResponseDTO originalResponse) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
