/*
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2018 Broadleaf Commerce
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
package org.broadleafcommerce.vendor.paypal.service;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.broadleafcommerce.common.payment.dto.AddressDTO;
import org.broadleafcommerce.common.payment.dto.LineItemDTO;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.service.CurrentOrderPaymentRequestService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.payment.service.gateway.ExternalCallPayPalCheckoutService;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreatePaymentRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreatePaymentResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalUpdatePaymentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Details;
import com.paypal.api.payments.Item;
import com.paypal.api.payments.ItemList;
import com.paypal.api.payments.Patch;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.ShippingAddress;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

@Service("blPayPalPaymentService")
public class PayPalPaymentServiceImpl implements PayPalPaymentService {

    @Resource(name = "blPayPalApiContext")
    protected APIContext apiContext;

    @Resource(name = "blExternalCallPayPalCheckoutService")
    protected ExternalCallPayPalCheckoutService externalCallService;

    @Resource(name = "blPayPalWebProfileService")
    protected PayPalWebProfileService webProfileService;

    @Autowired(required = false)
    protected CurrentOrderPaymentRequestService currentOrderPaymentRequestService;

    @Override
    public Payment createPayPalPaymentForCurrentOrder(boolean performCheckoutOnReturn) throws PaymentException {
        PaymentRequestDTO paymentRequestDTO = getPaymentRequestForCurrentOrder();

        // Set payer details
        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        // Set redirect URLs
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(externalCallService.getConfiguration().getCancelUrl());
        redirectUrls.setReturnUrl(externalCallService.getConfiguration().getReturnUrl());

        Amount amount = getPayPalAmountFromOrder(paymentRequestDTO);

        // Transaction information
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription(externalCallService.getConfiguration().getPaymentDescription());
        transaction.setCustom(paymentRequestDTO.getOrderId() + "|" + performCheckoutOnReturn);

        ItemList itemList = getPayPalItemListFromOrder(paymentRequestDTO);
        if (itemList != null) {
            transaction.setItemList(itemList);
        }

        // Add transaction to a list
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        // Add payment details
        Payment payment = new Payment();
        payment.setIntent("authorize");
        payment.setPayer(payer);
        payment.setRedirectUrls(redirectUrls);
        payment.setTransactions(transactions);

        String profileId = webProfileService.getWebProfileId();
        if (StringUtils.isNotBlank(profileId)) {
            payment.setExperienceProfileId(profileId);
        }
        return createPayment(payment);
    }

    @Override
    public void updatePayPalPaymentForFulfillment() throws PaymentException {
        PaymentRequestDTO paymentRequestDTO = getPaymentRequestForCurrentOrder();
        String paymentId = getPayPalPaymentIdFromCurrentOrder();
        if (paymentRequestDTO == null) {
            throw new PaymentException("Unable to update the current PayPal payment because the PaymentRequestDTO was null");
        }
        if (StringUtils.isBlank(paymentId)) {
            throw new PaymentException("Unable to update the current PayPal payment because no PayPal payment id was found on the order");
        }
        List<Patch> patches = new ArrayList<>();

        Patch amountPatch = new Patch();
        amountPatch.setOp("replace");
        amountPatch.setPath("/transactions/0/amount");
        Amount amount = getPayPalAmountFromOrder(paymentRequestDTO);
        amountPatch.setValue(amount);
        patches.add(amountPatch);

        ItemList itemList = getPayPalItemListFromOrder(paymentRequestDTO);
        if (itemList != null) {
            Patch shipToPatch = new Patch();
            shipToPatch.setOp("replace");
            shipToPatch.setPath("/transactions/0/item_list");
            shipToPatch.setValue(itemList);
            patches.add(shipToPatch);
        }

        Patch customPatch = new Patch();
        customPatch.setPath("/transactions/0/custom");
        customPatch.setOp("replace");
        customPatch.setValue(paymentRequestDTO.getOrderId() + "|" + true);
        patches.add(customPatch);

        Payment paypalPayment = new Payment();
        paypalPayment.setId(paymentId);
        updatePayment(paypalPayment, patches);

    }

    protected ItemList getPayPalItemListFromOrder(PaymentRequestDTO paymentRequestDTO) {
        ItemList itemList = new ItemList();
        boolean returnItemList = false;
        if (paymentRequestDTO.shipToPopulated()) {
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

    protected Amount getPayPalAmountFromOrder(PaymentRequestDTO paymentRequestDTO) {
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

    protected ShippingAddress getPayPalShippingAddress(PaymentRequestDTO paymentRequestDTO) {
        ShippingAddress shipAddress = new ShippingAddress();
        AddressDTO<PaymentRequestDTO> addressDTO = paymentRequestDTO.getShipTo();
        shipAddress.setRecipientName(addressDTO.getAddressFullName());
        shipAddress.setLine1(addressDTO.getAddressLine1());
        shipAddress.setLine2(addressDTO.getAddressLine2());
        shipAddress.setCity(addressDTO.getAddressCityLocality());
        shipAddress.setState(addressDTO.getAddressStateRegion());
        shipAddress.setPostalCode(addressDTO.getAddressPostalCode());
        shipAddress.setCountryCode(addressDTO.getAddressCountryCode());
        shipAddress.setPhone(addressDTO.getAddressPhone());
        return shipAddress;
    }

    protected Payment createPayment(Payment payment) throws PaymentException {
        PayPalCreatePaymentResponse response = (PayPalCreatePaymentResponse) externalCallService.call(new PayPalCreatePaymentRequest(payment, apiContext));
        return response.getPayment();
    }

    protected void updatePayment(Payment payment, List<Patch> patches) throws PaymentException {
        externalCallService.call(new PayPalUpdatePaymentRequest(payment, patches, apiContext));
    }

    protected PaymentRequestDTO getPaymentRequestForCurrentOrder() throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            return currentOrderPaymentRequestService.getPaymentRequestFromCurrentOrder();
        } else {
            throw new PaymentException("Unable to get PaymentRequestDTO for current order");
        }
    }

    @Override
    public String getPayPalPaymentIdFromCurrentOrder() throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            return currentOrderPaymentRequestService.retrieveOrderAttributeFromCurrentOrder(MessageConstants.PAYMENTID);
        } else {
            throw new PaymentException("Unable to retrieve PayPal payment id for current order");
        }
    }

    @Override
    public String getPayPalPayerIdFromCurrentOrder() throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            return currentOrderPaymentRequestService.retrieveOrderAttributeFromCurrentOrder(MessageConstants.PAYERID);
        } else {
            throw new PaymentException("Unable to retrieve PayPal payer id for current order");
        }
    }

    @Override
    public void setPayPalPaymentIdOnCurrentOrder(String paymentId) throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            currentOrderPaymentRequestService.addOrderAttributeToCurrentOrder(MessageConstants.PAYMENTID, paymentId);
        } else {
            throw new PaymentException("Unable to set PayPal payment id on current order");
        }
    }

    @Override
    public void setPayPalPayerIdOnCurrentOrder(String payerId) throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            currentOrderPaymentRequestService.addOrderAttributeToCurrentOrder(MessageConstants.PAYERID, payerId);
        } else {
            throw new PaymentException("Unable to set PayPal payer id on current order");
        }
    }

}
