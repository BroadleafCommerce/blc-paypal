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

package org.broadleafcommerce.pricing.service.module;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.core.order.domain.BundleOrderItem;
import org.broadleafcommerce.core.order.domain.DiscreteOrderItem;
import org.broadleafcommerce.core.order.domain.FulfillmentGroup;
import org.broadleafcommerce.core.order.domain.FulfillmentGroupFee;
import org.broadleafcommerce.core.order.domain.FulfillmentGroupImpl;
import org.broadleafcommerce.core.order.domain.FulfillmentGroupItem;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.domain.TaxDetail;
import org.broadleafcommerce.core.order.domain.TaxDetailImpl;
import org.broadleafcommerce.core.order.domain.TaxType;
import org.broadleafcommerce.core.pricing.service.exception.TaxException;
import org.broadleafcommerce.core.pricing.service.module.TaxModule;
import org.broadleafcommerce.service.module.CyberSourceModule;
import org.broadleafcommerce.vendor.cybersource.service.CyberSourceServiceManager;
import org.broadleafcommerce.vendor.cybersource.service.message.CyberSourceBillingRequest;
import org.broadleafcommerce.vendor.cybersource.service.tax.CyberSourceTaxService;
import org.broadleafcommerce.vendor.cybersource.service.tax.message.CyberSourceTaxItemRequest;
import org.broadleafcommerce.vendor.cybersource.service.tax.message.CyberSourceTaxItemResponse;
import org.broadleafcommerce.vendor.cybersource.service.tax.message.CyberSourceTaxRequest;
import org.broadleafcommerce.vendor.cybersource.service.tax.message.CyberSourceTaxResponse;

/**
 * Tax module that utilizes the Broadleaf Commerce API for CyberSource
 * tax calculation.
 * 
 * @author jfischer
 */
public class CyberSourceTaxModule extends CyberSourceModule implements TaxModule {

    public static final String MODULENAME = "cyberSourceTaxModule";

    protected String name = MODULENAME;
    private CyberSourceServiceManager serviceManager;
    private List<String> nexus = new ArrayList<String>();
    private List<String> nonexus = new ArrayList<String>();
    private String orderAcceptanceCity;
    private String orderAcceptanceCounty;
    private String orderAcceptanceCountry;
    private String orderAcceptanceState;
    private String orderAcceptancePostalCode;

    public Order calculateTaxForOrder(Order order) throws TaxException {
    	if (orderAcceptanceCountry != null && !orderAcceptanceCountry.equalsIgnoreCase("CA") && !orderAcceptanceCountry.equalsIgnoreCase("US")) {
    		throw new TaxException("CyberSource tax calculation only supported for the United States and Canada.");
    	}
    	HashMap<Long, CyberSourceTaxItemRequest> requestLibrary = new HashMap<Long, CyberSourceTaxItemRequest>();
    	CyberSourceTaxRequest taxRequest = createTaxRequest(order, requestLibrary);
		CyberSourceTaxResponse response;
		try {
			response = callService(taxRequest);
		} catch (org.broadleafcommerce.common.vendor.service.exception.TaxException e) {
			throw new TaxException(e);
		}
		calculateTaxes(order, requestLibrary, response);
		
		return order;
    }

    private void calculateTaxes(Order order, HashMap<Long, CyberSourceTaxItemRequest> requestLibrary, CyberSourceTaxResponse response) {
        for (CyberSourceTaxItemResponse itemResponse : response.getItemResponses()) {
            CyberSourceTaxItemRequest itemRequest = requestLibrary.get(itemResponse.getId().longValue());

            FulfillmentGroupImpl searchParam = new FulfillmentGroupImpl();
            searchParam.setId(itemRequest.getNonCyberSourceFulfillmentGroupId());
            FulfillmentGroup myGroup = order.getFulfillmentGroups().get(order.getFulfillmentGroups().indexOf(searchParam));

            String searchItemParam = itemRequest.getNonCyberSourceItemIdentifier();
            Boolean isFeeTax = searchItemParam.contains("Fee") ? true : false;
            Boolean isItemTax = searchItemParam.contains("Item") ? true : false;
            int index = Integer.parseInt(searchItemParam.substring(searchItemParam.indexOf(':') + 1));
            
            if (isFeeTax || isItemTax) {
                List<TaxDetail> taxes = new ArrayList<TaxDetail>();
                taxes.add(new TaxDetailImpl(TaxType.CITY, itemResponse.getCityTaxAmount(), response.getCityRate()));
                taxes.add(new TaxDetailImpl(TaxType.COUNTY, itemResponse.getCountyTaxAmount(), response.getCountyRate()));
                taxes.add(new TaxDetailImpl(TaxType.DISTRICT, itemResponse.getDistrictTaxAmount(), response.getDistrictRate()));
                taxes.add(new TaxDetailImpl(TaxType.STATE, itemResponse.getStateTaxAmount(), response.getStateRate()));
    
                if (isFeeTax) { 
                    myGroup.getFulfillmentGroupFees().get(index).setTaxes(taxes);
                } else {
                    myGroup.getFulfillmentGroupItems().get(index).setTaxes(taxes);
                } 
            } else {
                // We are splitting this from the above logic (even though they're very similar) to provision for the 
                // possibility of having a different type of tax that doesn't apply to an item, such as shipping tax
                List<TaxDetail> taxes = myGroup.getTaxes();
                if (taxes == null) { 
                    taxes = new ArrayList<TaxDetail>();
                }
                
                taxes.add(new TaxDetailImpl(TaxType.CITY, itemResponse.getCityTaxAmount(), response.getCityRate()));
                taxes.add(new TaxDetailImpl(TaxType.COUNTY, itemResponse.getCountyTaxAmount(), response.getCountyRate()));
                taxes.add(new TaxDetailImpl(TaxType.DISTRICT, itemResponse.getDistrictTaxAmount(), response.getDistrictRate()));
                taxes.add(new TaxDetailImpl(TaxType.STATE, itemResponse.getStateTaxAmount(), response.getStateRate()));
                
                myGroup.setTaxes(taxes);
            }
        }
    }
	
	private CyberSourceTaxRequest createTaxRequest(Order order, HashMap<Long, CyberSourceTaxItemRequest> requestLibrary) throws TaxException {
		if (order.getPaymentInfos() == null || order.getPaymentInfos().get(0) == null || order.getPaymentInfos().get(0).getAddress() == null) {
			throw new TaxException("The order must have at least one PaymentInfo instance associated with a completed Address in order to calculate tax.");
		}
		CyberSourceTaxRequest taxRequest = new CyberSourceTaxRequest();
		setCurrency(order, taxRequest);
		CyberSourceBillingRequest billingRequest = createBillingRequest(order.getPaymentInfos().get(0));
		taxRequest.setBillingRequest(billingRequest);
		String myNexus = StringUtils.join(nexus.toArray(new String[]{}), ',');
		if (!StringUtils.isEmpty(myNexus)) taxRequest.setNexus(myNexus);
		String myNoNexus = StringUtils.join(nonexus.toArray(new String[]{}), ',');
		if (!StringUtils.isEmpty(myNoNexus)) taxRequest.setNoNexus(myNoNexus);
		taxRequest.setOrderAcceptanceCity(orderAcceptanceCity);
		taxRequest.setOrderAcceptanceCounty(orderAcceptanceCounty);
		taxRequest.setOrderAcceptanceCountry(orderAcceptanceCountry);
		taxRequest.setOrderAcceptanceState(orderAcceptanceState);
		taxRequest.setOrderAcceptancePostalCode(orderAcceptancePostalCode);
		
		for (FulfillmentGroup fulfillmentGroup : order.getFulfillmentGroups()) {
			if (fulfillmentGroup.getAddress().getCountry() != null && !fulfillmentGroup.getAddress().getCountry().getAbbreviation().equalsIgnoreCase("CA") && !fulfillmentGroup.getAddress().getCountry().getAbbreviation().equalsIgnoreCase("US")) {
				throw new TaxException("CyberSource tax calculation only supported for the United States and Canada.");
			}
			for (FulfillmentGroupItem item : fulfillmentGroup.getFulfillmentGroupItems()) {
				int itemCounter = 0;
				OrderItem orderItem = item.getOrderItem();
				if (orderItem.getTaxablePrice().greaterThan(Money.zero(taxRequest.getCurrency()))) {
					CyberSourceTaxItemRequest itemRequest = new CyberSourceTaxItemRequest();
					itemRequest.setNonCyberSourceFulfillmentGroupId(fulfillmentGroup.getId());
					itemRequest.setNonCyberSourceItemIdentifier("Item:" + itemCounter++);
					if (DiscreteOrderItem.class.isAssignableFrom(orderItem.getClass())) {
						DiscreteOrderItem discreteItem = (DiscreteOrderItem) orderItem;
						itemRequest.setProductName(discreteItem.getName());
						itemRequest.setProductSKU(discreteItem.getSku().getName());
						itemRequest.setDescription(discreteItem.getSku().getDescription());
					} else if (BundleOrderItem.class.isAssignableFrom(orderItem.getClass())){
						BundleOrderItem bundleItem = (BundleOrderItem) orderItem;
						itemRequest.setProductName(bundleItem.getName());
						itemRequest.setDescription("Bundled Order Item");
					} else {
						itemRequest.setProductName("Other");
						itemRequest.setDescription("Other product type: " + orderItem.getClass().getName());
					}
					itemRequest.setQuantity(1L);
					itemRequest.setNonCyberSourceQuantity(Integer.valueOf(item.getQuantity()).longValue());
					itemRequest.setUnitPrice(orderItem.getTaxablePrice());
					taxRequest.getItemRequests().add(itemRequest);
					requestLibrary.put(itemRequest.getId(), itemRequest);
				}
	        }
            for (FulfillmentGroupFee fulfillmentGroupFee : fulfillmentGroup.getFulfillmentGroupFees()) {
                int feeCounter = 0;
                if (fulfillmentGroupFee.getAmount().greaterThan(Money.zero(taxRequest.getCurrency()))) {
                    CyberSourceTaxItemRequest itemRequest = new CyberSourceTaxItemRequest();
                    itemRequest.setNonCyberSourceFulfillmentGroupId(fulfillmentGroup.getId());
                    itemRequest.setNonCyberSourceItemIdentifier("Fee:" + feeCounter++);
                    itemRequest.setProductName(fulfillmentGroupFee.getName() == null ? "Fee" : fulfillmentGroupFee.getName());
                    itemRequest.setDescription(fulfillmentGroupFee.getReportingCode() == null ? "None" : fulfillmentGroupFee.getReportingCode());
                    itemRequest.setQuantity(1L);
                    itemRequest.setNonCyberSourceQuantity(1L);
                    itemRequest.setUnitPrice(fulfillmentGroupFee.getAmount());
                    taxRequest.getItemRequests().add(itemRequest);
                    requestLibrary.put(itemRequest.getId(), itemRequest);
                }
            }
			
            if (fulfillmentGroup.getShippingPrice() != null && fulfillmentGroup.getShippingPrice().greaterThan(Money.zero(taxRequest.getCurrency()))) {
                CyberSourceTaxItemRequest itemRequest = new CyberSourceTaxItemRequest();
                itemRequest.setNonCyberSourceFulfillmentGroupId(fulfillmentGroup.getId());
                itemRequest.setNonCyberSourceItemIdentifier("Shipping:0");
                itemRequest.setProductName("Shipping Cost");
                itemRequest.setDescription("Taxable Shipping Cost");
                itemRequest.setQuantity(1L);
                itemRequest.setNonCyberSourceQuantity(1L);
                itemRequest.setUnitPrice(fulfillmentGroup.getShippingPrice());
                taxRequest.getItemRequests().add(itemRequest);
                requestLibrary.put(itemRequest.getId(), itemRequest);
            }
        }
		return taxRequest;
	}
    
    private CyberSourceTaxResponse callService(CyberSourceTaxRequest taxRequest) throws org.broadleafcommerce.common.vendor.service.exception.TaxException {
		CyberSourceTaxService service = (CyberSourceTaxService) serviceManager.getValidService(taxRequest);
        CyberSourceTaxResponse response = (CyberSourceTaxResponse) service.process(taxRequest);		
		return response;
	}
    
    private void setCurrency(Order order, CyberSourceTaxRequest taxRequest) {
		Currency currency = order.getTotal().getCurrency();
        if (currency == null) {
        	currency = Money.defaultCurrency();
        }
        taxRequest.setCurrency(currency.getCurrencyCode());
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CyberSourceServiceManager getServiceManager() {
		return serviceManager;
	}

	public void setServiceManager(CyberSourceServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

	public List<String> getNexus() {
		return nexus;
	}

	public void setNexus(List<String> nexus) {
		this.nexus = nexus;
	}

	public List<String> getNonexus() {
		return nonexus;
	}

	public void setNonexus(List<String> nonexus) {
		this.nonexus = nonexus;
	}

	public java.lang.String getOrderAcceptanceCounty() {
		return orderAcceptanceCounty;
	}

	public void setOrderAcceptanceCounty(java.lang.String orderAcceptanceCounty) {
		this.orderAcceptanceCounty = orderAcceptanceCounty;
	}

	public java.lang.String getOrderAcceptanceCountry() {
		return orderAcceptanceCountry;
	}

	public void setOrderAcceptanceCountry(java.lang.String orderAcceptanceCountry) {
		this.orderAcceptanceCountry = orderAcceptanceCountry;
	}

	public java.lang.String getOrderAcceptanceState() {
		return orderAcceptanceState;
	}

	public void setOrderAcceptanceState(java.lang.String orderAcceptanceState) {
		this.orderAcceptanceState = orderAcceptanceState;
	}

	public java.lang.String getOrderAcceptancePostalCode() {
		return orderAcceptancePostalCode;
	}

	public void setOrderAcceptancePostalCode(java.lang.String orderAcceptancePostalCode) {
		this.orderAcceptancePostalCode = orderAcceptancePostalCode;
	}

	public String getOrderAcceptanceCity() {
		return orderAcceptanceCity;
	}

	public void setOrderAcceptanceCity(String orderAcceptanceCity) {
		this.orderAcceptanceCity = orderAcceptanceCity;
	}
	
}
