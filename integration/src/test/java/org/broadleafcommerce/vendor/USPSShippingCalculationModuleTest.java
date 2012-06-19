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

package org.broadleafcommerce.vendor;

import javax.annotation.Resource;

import org.broadleafcommerce.pricing.service.module.USPSShippingCalculationModule;
import org.broadleafcommerce.test.BaseTest;
import org.broadleafcommerce.vendor.usps.service.USPSShippingCalculationService;
import org.springframework.test.annotation.Rollback;
import org.testng.annotations.Test;

public class USPSShippingCalculationModuleTest extends BaseTest {

    @Resource
    private USPSShippingCalculationService shippingCalculationService;
    
    @Resource
    private USPSShippingCalculationModule shippingCalculationModule;

    @Test(groups = { "testSuccessfulShippingModuleCalc" })
    @Rollback(false)
    public void testSuccessfulShippingModuleCalc() throws Exception {
        //TODO address BLC-404
        /*if (shippingCalculationService.getUspsUserName().equals("?")) {
            return;
        }

        System.out.println("***Initiating testSuccessfulShippingModuleCalc***");

        FulfillmentGroup fg = new FulfillmentGroupImpl();
        fg.setService(ShippingServiceType.USPS.getType());
        fg.setMethod(USPSServiceMethod.PRIORITYMAIL.getType());
        DiscreteOrderItem dsItem1 = new DiscreteOrderItemImpl();
        Product product1 = new ProductImpl();
        product1.setSize(USPSContainerSizeType.REGULAR);
        product1.setContainer(USPSContainerShapeType.FLATRATEBOX);
        ProductWeight weight = new ProductWeight();
        weight.setWeight(BigDecimal.valueOf(10L).add(UnitOfMeasureUtil.convertOuncesToPounds(BigDecimal.valueOf(5L))));
        weight.setWeightUnitOfMeasure(WeightUnitOfMeasureType.POUNDS);
        product1.setWeight(weight);
        dsItem1.setProduct(product1);
        Address address = new AddressImpl();
        address.setPostalCode("20008");
        fg.setAddress(address);
        FulfillmentGroupItem fgItem = new FulfillmentGroupItemImpl();
        fgItem.setOrderItem(dsItem1);
        fg.getFulfillmentGroupItems().add(fgItem);
        
        FulfillmentGroup response = shippingCalculationModule.calculateShippingForFulfillmentGroup(fg);
        assert(response.getShippingPrice().greaterThan(new Money(0D)));*/
    }

}
