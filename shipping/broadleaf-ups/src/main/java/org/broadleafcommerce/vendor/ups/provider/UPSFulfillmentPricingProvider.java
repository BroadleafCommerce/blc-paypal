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

package org.broadleafcommerce.vendor.ups.provider;

import org.broadleafcommerce.core.order.domain.FulfillmentGroup;
import org.broadleafcommerce.core.order.domain.FulfillmentOption;
import org.broadleafcommerce.core.pricing.service.fulfillment.processor.FulfillmentEstimationResponse;
import org.broadleafcommerce.core.pricing.service.fulfillment.processor.FulfillmentPricingProvider;
import org.broadleafcommerce.vendor.ups.service.UPSPricingGateway;

/**
 * This class provides an implementation of a pricing provider that calls UPS to calculate price.
 *
 * <p/>
 * User: Kelly Tisdell
 * Date: 7/6/12
 */
public class UPSFulfillmentPricingProvider implements FulfillmentPricingProvider {

    protected UPSPricingGateway pricingGateway;

    @Override
    public boolean canCalculateCostForFulfillmentGroup(FulfillmentGroup fulfillmentGroup) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public FulfillmentGroup calculateCostForFulfillmentGroup(FulfillmentGroup fulfillmentGroup) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean canEstimateCostForFulfillmentGroup(FulfillmentGroup fulfillmentGroup, FulfillmentOption option) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public FulfillmentEstimationResponse estimateCostForFulfillmentGroup(FulfillmentGroup fulfillmentGroup, FulfillmentOption option) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setUPSPricingGateway(UPSPricingGateway pricingGateway) {
        this.pricingGateway = pricingGateway;
    }
}
