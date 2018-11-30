/*
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2014 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.payment.service.gateway;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.payment.dto.AddressDTO;
import org.broadleafcommerce.common.payment.dto.LineItemDTO;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.AbstractExternalPaymentGatewayCall;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.vendor.paypal.api.AgreementToken;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalResponse;
import org.springframework.stereotype.Service;

import com.paypal.api.payments.Address;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Details;
import com.paypal.api.payments.Item;
import com.paypal.api.payments.ItemList;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.ShippingAddress;
import com.paypal.api.payments.Transaction;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blExternalCallPayPalCheckoutService")
public class ExternalCallPayPalCheckoutServiceImpl extends AbstractExternalPaymentGatewayCall<PayPalRequest, PayPalResponse> implements ExternalCallPayPalCheckoutService {

    @Resource(name = "blPayPalCheckoutConfiguration")
    protected PayPalCheckoutConfiguration configuration;

    @Override
    public PayPalCheckoutConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void setCommonDetailsResponse(AgreementToken response, PaymentResponseDTO responseDTO, Money amount,
                                         String orderId, boolean checkoutComplete) {

        if (response != null) {
            responseDTO.rawResponse(response.toJSON());

            Address shippingAddress = response.getShippingAddress();

            if (shippingAddress != null) {
                responseDTO.shipTo()
                        .addressLine1(shippingAddress.getLine1())
                        .addressLine2(shippingAddress.getLine2())
                        .addressCityLocality(shippingAddress.getCity())
                        .addressStateRegion(shippingAddress.getState())
                        .addressPostalCode(shippingAddress.getPostalCode())
                        .addressCountryCode(shippingAddress.getCountryCode())
                        .addressPhone(shippingAddress.getPhone())
                        .done();
            }
        }

        responseDTO.amount(amount)
                .orderId(orderId)
                .successful(true)
                .valid(true)
                .completeCheckoutOnCallback(checkoutComplete);
    }

    @Override
    public void setCommonDetailsResponse(Payment response, PaymentResponseDTO responseDTO) {
        responseDTO.rawResponse(response.toJSON());
        
        if (CollectionUtils.isNotEmpty(response.getTransactions()) && 
            response.getTransactions().get(0) != null &&
            response.getTransactions().get(0).getItemList() != null) {
            ShippingAddress shippingAddress = response.getTransactions().get(0).getItemList().getShippingAddress();
            
            String shipPhone = shippingAddress.getPhone();
            String itemListPhone = response.getTransactions().get(0).getItemList().getShippingPhoneNumber();
            String payerPhone = response.getPayer().getPayerInfo().getPhone();
            String phone = "";
            if (shipPhone != null) {
                phone = shipPhone;
            } else if (itemListPhone != null) {
                phone = itemListPhone;
            } else if (payerPhone != null) {
                phone = payerPhone;
            }
            responseDTO.shipTo()
                .addressFullName(shippingAddress.getRecipientName())
                .addressLine1(shippingAddress.getLine1())
                .addressLine2(shippingAddress.getLine2())
                .addressCityLocality(shippingAddress.getCity())
                .addressStateRegion(shippingAddress.getState())
                .addressPostalCode(shippingAddress.getPostalCode())
                .addressCountryCode(shippingAddress.getCountryCode())
                .addressPhone(phone)
                .done();
            
            if (shippingAddress.getStatus() != null) {
                responseDTO.getShipTo().additionalFields(MessageConstants.ADDRESSSTATUS, shippingAddress.getStatus());
            }

            Transaction transaction = response.getTransactions().get(0);
            
            String itemTotal = "";
            String shippingDiscount = "";
            String shippingTotal = "";
            String totalTax = "";
            String total = "0.00";
            String currency = "USD";
            if (transaction.getAmount() != null && transaction.getAmount().getDetails() != null) {
                Details details = transaction.getAmount().getDetails();
                if (details.getSubtotal() != null) {
                    itemTotal = details.getSubtotal();
                }
                if (details.getShippingDiscount() != null) {
                    shippingDiscount = details.getShippingDiscount();
                }
                if (details.getShipping() != null) {
                    shippingTotal = details.getShipping();
                }
                if (details.getTax() != null) {
                    totalTax = details.getTax();
                }
            }
            if (transaction.getAmount() != null) {
                total = transaction.getAmount().getTotal();
                if (transaction.getAmount().getCurrency() != null) {
                    currency = transaction.getAmount().getCurrency();
                }
            }
            String[] customFields = transaction.getCustom().split("\\|");
            responseDTO.amount(new Money(total, currency))
                    .orderId(customFields[0])
                    .successful(true)
                    .valid(true)
                    .completeCheckoutOnCallback(Boolean.parseBoolean(customFields[1]))
                    .responseMap(MessageConstants.DETAILSPAYMENTALLOWEDMETHOD, response.getPayer().getPaymentMethod())
                    .responseMap(MessageConstants.DETAILSPAYMENTTRANSACTIONID, response.getId())
                    .responseMap(MessageConstants.DETAILSPAYMENTITEMTOTAL, itemTotal)
                    .responseMap(MessageConstants.DETAILSPAYMENTSHIPPINGDISCOUNT, shippingDiscount)
                    .responseMap(MessageConstants.DETAILSPAYMENTSHIPPINGTOTAL,shippingTotal)
                    .responseMap(MessageConstants.DETAILSPAYMENTTOTALTAX, totalTax);
            
            String payerStatus = response.getPayer().getStatus();
    
            responseDTO.customer()
                .firstName(response.getPayer().getPayerInfo().getFirstName())
                .lastName(response.getPayer().getPayerInfo().getLastName())
                .phone(response.getPayer().getPayerInfo().getPhone())
                .email(response.getPayer().getPayerInfo().getEmail())
                .done()
            .responseMap(MessageConstants.NOTE, response.getNoteToPayer())
            .responseMap(MessageConstants.PAYERSTATUS, payerStatus);
        }

    }

    @Override
    public ShippingAddress getPayPalShippingAddress(PaymentRequestDTO paymentRequestDTO) {
        ShippingAddress shipAddress = new ShippingAddress();
        AddressDTO<PaymentRequestDTO> addressDTO = paymentRequestDTO.getShipTo();
        shipAddress.setRecipientName(addressDTO.getAddressFullName());
        shipAddress.setLine1(addressDTO.getAddressLine1());
        shipAddress.setLine2(addressDTO.getAddressLine2());
        shipAddress.setCity(addressDTO.getAddressCityLocality());
        shipAddress.setState(addressDTO.getAddressStateRegion());
        shipAddress.setPostalCode(addressDTO.getAddressPostalCode());
        shipAddress.setCountryCode(addressDTO.getAddressCountryCode());
        if (StringUtils.isNotBlank(addressDTO.getAddressPhone())) {
            shipAddress.setPhone(addressDTO.getAddressPhone());
        }
        return shipAddress;
    }

    @Override
    public ItemList getPayPalItemListFromOrder(PaymentRequestDTO paymentRequestDTO, boolean shouldPopulateShipping) {
        ItemList itemList = new ItemList();
        boolean returnItemList = false;
        if (paymentRequestDTO.shipToPopulated() && shouldPopulateShipping) {
            ShippingAddress address = getPayPalShippingAddress(paymentRequestDTO);
            itemList.setShippingAddress(address);
            returnItemList = true;
        }

        if (CollectionUtils.isNotEmpty(paymentRequestDTO.getLineItems())) {
            List<Item> items = new ArrayList<>();
            for (LineItemDTO lineItem : paymentRequestDTO.getLineItems()) {
                Item item = new Item();
                item.setCategory(lineItem.getCategory());
                item.setDescription(lineItem.getDescription());
                item.setQuantity(lineItem.getQuantity());
                item.setPrice(lineItem.getTotal());
                item.setTax(lineItem.getTax());
                item.setName(lineItem.getName());
                items.add(item);
            }
            itemList.setItems(items);
            returnItemList = true;
        }
        return returnItemList ? itemList : null;
    }

    @Override
    public Amount getPayPalAmountFromOrder(PaymentRequestDTO paymentRequestDTO) {
        Details details = new Details();

        details.setShipping(paymentRequestDTO.getShippingTotal());
        details.setSubtotal(paymentRequestDTO.getOrderSubtotal());
        details.setTax(paymentRequestDTO.getTaxTotal());

        Amount amount = new Amount();
        amount.setCurrency(paymentRequestDTO.getOrderCurrencyCode());
        amount.setTotal(paymentRequestDTO.getTransactionTotal());
        amount.setDetails(details);
        return amount;
    }

    @Override
    public String getServiceName() {
        return getClass().getName();
    }

    @Override
    public PayPalResponse call(PayPalRequest paymentRequest) throws PaymentException {
        return super.process(paymentRequest);
    }


    @Override
    public PayPalResponse communicateWithVendor(PayPalRequest paymentRequest) throws Exception {
        return paymentRequest.execute();
    }

    @Override
    public Integer getFailureReportingThreshold() {
        return configuration.getFailureReportingThreshold();
    }

}
