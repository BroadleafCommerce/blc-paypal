/*-
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2024 Broadleaf Commerce
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

import com.paypal.orders.AddressPortable;
import com.paypal.orders.AmountBreakdown;
import com.paypal.orders.AmountWithBreakdown;
import com.paypal.orders.ApplicationContext;
import com.paypal.orders.Item;
import com.paypal.orders.Money;
import com.paypal.orders.Name;
import com.paypal.orders.Order;
import com.paypal.orders.OrderRequest;
import com.paypal.orders.Patch;
import com.paypal.orders.Payee;
import com.paypal.orders.Payer;
import com.paypal.orders.PurchaseUnitRequest;
import com.paypal.orders.ShippingDetail;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.broadleafcommerce.common.payment.dto.AddressDTO;
import org.broadleafcommerce.common.payment.dto.GatewayCustomerDTO;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.service.CurrentOrderPaymentRequestService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.payment.service.gateway.ExternalCallPayPalCheckoutService;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreateOrderRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCreateOrderResponse;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalUpdateOrderRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalUpdateOrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;

@Service("blPayPalPaymentService")
public class PayPalPaymentServiceImpl implements PayPalPaymentService {

    protected static final String REPLACE_OP_TYPE = "replace";

    @Resource(name = "blExternalCallPayPalCheckoutService")
    protected ExternalCallPayPalCheckoutService externalCallService;

    @Autowired(required = false)
    protected CurrentOrderPaymentRequestService currentOrderPaymentRequestService;

    @Resource(name = "blPayPalClientProvider")
    protected PayPalClientProvider clientProvider;

    @Value("${gateway.paypal.checkout.rest.populate.shipping.create.order:true}")
    protected boolean shouldPopulateShippingOnOrderCreation;

    @Override
    public Order createPayPalOrderForCurrentOrder(boolean performCheckoutOnReturn) throws PaymentException {
        PaymentRequestDTO paymentRequest = getPaymentRequestForCurrentOrder();

        PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest()
                .amountWithBreakdown(constructAmountWithBreakdown(paymentRequest))
                .payee(constructPayee(paymentRequest))
                .description(externalCallService.getConfiguration().getPaymentDescription())
                .customId(String.format("%s|%s", paymentRequest.getOrderId(),
                        performCheckoutOnReturn));

        purchaseUnitRequest.items(constructItems(paymentRequest));

        if (shouldPopulateShippingOnOrderCreation) {
            purchaseUnitRequest.shippingDetail(constructShippingDetail(paymentRequest));
        }

        String shippingPreference = (String) paymentRequest
                .getAdditionalFields().get(MessageConstants.SHIPPING_PREFERENCE);
        String locale = Optional
                .ofNullable((Locale) paymentRequest.getAdditionalFields().get(MessageConstants.LOCALE))
                .map(Locale::toLanguageTag)
                .map(loc -> loc.replace("_", "-"))
                .orElse(null);
        OrderRequest order = new OrderRequest()
                .checkoutPaymentIntent(getIntent(performCheckoutOnReturn))
                .payer(constructPayer(paymentRequest))
                .purchaseUnits(Collections.singletonList(purchaseUnitRequest))
                .applicationContext(new ApplicationContext()
                        .shippingPreference(shippingPreference)
                        .locale(locale));

        return createOrder(order, paymentRequest);
    }

    @Override
    public void updatePayPalOrderForFulfillment() throws PaymentException {
        PaymentRequestDTO paymentRequest = getPaymentRequestForCurrentOrder();

        String orderId = getPayPalOrderIdFromCurrentOrder();

        if (StringUtils.isBlank(orderId)) {
            throw new PaymentException(
                    "Unable to update the current PayPal payment because no PayPal payment id was found on the order");
        }

        PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest()
                .amountWithBreakdown(constructAmountWithBreakdown(paymentRequest))
                .payee(constructPayee(paymentRequest))
                .description(externalCallService.getConfiguration().getPaymentDescription())
                .customId(paymentRequest.getOrderId() + "|" + true);

        purchaseUnitRequest.items(constructItems(paymentRequest));

        if (shouldPopulateShippingOnOrderCreation) {
            purchaseUnitRequest.shippingDetail(constructShippingDetail(paymentRequest));
        }

        Patch amountPatch = new Patch()
                .op(REPLACE_OP_TYPE)
                .path("/purchase_units/@reference_id=='default'")
                .value(purchaseUnitRequest);

        updateOrder(orderId, Collections.singletonList(amountPatch), paymentRequest);
    }

    protected Order createOrder(OrderRequest orderRequest,
                                PaymentRequestDTO paymentRequest) throws PaymentException {
        PayPalCreateOrderRequest request = new PayPalCreateOrderRequest(
                clientProvider,
                paymentRequest,
                orderRequest);
        PayPalCreateOrderResponse response =
                externalCallService.call(request, PayPalCreateOrderResponse.class);
        return response.getContent();
    }

    protected void updateOrder(String orderId,
                               List<Patch> patches,
                               PaymentRequestDTO paymentRequest) throws PaymentException {
        Assert.hasText(orderId, "OrderId cannot be blank");
        Assert.notEmpty(patches, "Patches cannot be empty");
        PayPalUpdateOrderRequest request = new PayPalUpdateOrderRequest(
                clientProvider,
                paymentRequest,
                orderId,
                patches);
        externalCallService.call(request, PayPalUpdateOrderResponse.class);
    }

    protected Payee constructPayee(PaymentRequestDTO paymentRequest) {
        String merchantId =
                (String) paymentRequest.getAdditionalFields().get(MessageConstants.PAYEE_MERCHANT_ID);

        if (StringUtils.isBlank(merchantId)) {
            return null;
        }

        String merchantEmail =
                (String) paymentRequest.getAdditionalFields().get(MessageConstants.PAYEE_MERCHANT_EMAIL);

        return new Payee().merchantId(merchantId).email(merchantEmail);
    }

    protected Payer constructPayer(PaymentRequestDTO paymentRequestDTO) {
        return new Payer().email(getPayerEmail(paymentRequestDTO));
    }

    protected String getPayerEmail(PaymentRequestDTO paymentRequestDTO) {
        return Optional.ofNullable(paymentRequestDTO.getCustomer())
                .map(GatewayCustomerDTO::getEmail)
                .orElse(null);
    }

    protected List<Item> constructItems(PaymentRequestDTO paymentRequest) {
        if (!CollectionUtils.isNotEmpty(paymentRequest.getLineItems())) {
            return Collections.emptyList();
        }

        return paymentRequest.getLineItems()
                .stream()
                .map(lineItem -> new Item()
                        .category(lineItem.getCategory())
                        .description(lineItem.getDescription())
                        .name(lineItem.getName())
                        .quantity(Objects.toString(lineItem.getQuantity(), null))
                        .sku(lineItem.getSystemId())
                        .tax(convertToMoney(lineItem.getTax(), paymentRequest.getOrderCurrencyCode()))
                        .unitAmount(convertToMoney(lineItem.getAmount(), paymentRequest.getOrderCurrencyCode())))
                .collect(Collectors.toList());
    }

    protected ShippingDetail constructShippingDetail(
            PaymentRequestDTO paymentRequest) {
        AddressDTO<PaymentRequestDTO> address = paymentRequest.getShipTo();

        if (address == null) {
            return null;
        }

        return new ShippingDetail()
                .name(new Name().fullName(address.getAddressFullName()))
                .addressPortable(new AddressPortable()
                        .addressLine1(address.getAddressLine1())
                        .addressLine2(address.getAddressLine2())
                        .adminArea2(address.getAddressCityLocality())
                        .adminArea1(address.getAddressStateRegion())
                        .postalCode(address.getAddressPostalCode())
                        .countryCode(address.getAddressCountryCode()));
    }

    protected AmountWithBreakdown constructAmountWithBreakdown(
            PaymentRequestDTO paymentRequest) {
        AmountBreakdown details = new AmountBreakdown()
                .itemTotal(convertToMoney(paymentRequest.getOrderSubtotal(), paymentRequest.getOrderCurrencyCode()))
                .shipping(convertToMoney(paymentRequest.getShippingTotal(), paymentRequest.getOrderCurrencyCode()))
                .taxTotal(convertToMoney(paymentRequest.getTaxTotal(), paymentRequest.getOrderCurrencyCode()));

        return new AmountWithBreakdown()
                .currencyCode(paymentRequest.getOrderCurrencyCode())
                .value(paymentRequest.getTransactionTotal())
                .amountBreakdown(details);
    }

    protected Money convertToMoney(String value, String currencyCode) {
        return new Money().value(value).currencyCode(currencyCode);
    }

    @Override
    public PaymentRequestDTO getPaymentRequestForCurrentOrder() throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            return currentOrderPaymentRequestService.getPaymentRequestFromCurrentOrder();
        } else {
            throw new PaymentException("Unable to get PaymentRequestDTO for current order");
        }
    }

    @Override
    public String getPayPalOrderIdFromCurrentOrder() throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            return currentOrderPaymentRequestService.retrieveOrderAttributeFromCurrentOrder(MessageConstants.ORDER_ID);
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
    public void setPayPalOrderIdOnCurrentOrder(String orderId) throws PaymentException {
        if (currentOrderPaymentRequestService != null) {
            currentOrderPaymentRequestService.addOrderAttributeToCurrentOrder(MessageConstants.ORDER_ID, orderId);
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

    public String getIntent(boolean performCheckoutOnReturn) {
        if (externalCallService.getConfiguration().isPerformAuthorizeAndCapture()) {
            return "CAPTURE";
        }
        return "AUTHORIZE";
    }
}
