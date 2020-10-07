/*
 * Copyright (C) 2009 - 2020 Broadleaf Commerce
 *
 * Licensed under the Broadleaf End User License Agreement (EULA), Version 1.1 (the
 * "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt).
 *
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the
 * "Custom License") between you and Broadleaf Commerce. You may not use this file except in
 * compliance with the applicable license.
 *
 * NOTICE: All information contained herein is, and remains the property of Broadleaf Commerce, LLC
 * The intellectual and technical concepts contained herein are proprietary to Broadleaf Commerce,
 * LLC and may be covered by U.S. and Foreign Patents, patents in process, and are protected by
 * trade secret or copyright law. Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained from Broadleaf Commerce, LLC.
 */
package org.broadleafcommerce.payment.service.gateway;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.broadleafcommerce.vendor.paypal.api.AgreementToken;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalResponse;
import org.springframework.lang.Nullable;

import com.broadleafcommerce.money.util.MonetaryUtils;
import com.broadleafcommerce.paymentgateway.domain.Address;
import com.broadleafcommerce.paymentgateway.domain.LineItem;
import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.domain.PaymentResponse;
import com.broadleafcommerce.paymentgateway.domain.enums.TransactionType;
import com.broadleafcommerce.paymentgateway.service.AbstractExternalPaymentGatewayCall;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.CartBase;
import com.paypal.api.payments.Details;
import com.paypal.api.payments.Item;
import com.paypal.api.payments.ItemList;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.ShippingAddress;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.money.MonetaryAmount;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@RequiredArgsConstructor
public class DefaultPayPalCheckoutExternalCallService
        extends AbstractExternalPaymentGatewayCall<PayPalRequest, PayPalResponse>
        implements PayPalCheckoutExternalCallService {

    @Getter(AccessLevel.PROTECTED)
    private final PayPalCheckoutRestConfigurationProperties configProperties;

    @Getter(AccessLevel.PROTECTED)
    private final PayPalGatewayConfiguration gatewayConfiguration;

    @Override
    public PayPalResponse call(PayPalRequest paymentRequest) {
        return super.process(paymentRequest);
    }

    @Override
    public PayPalResponse communicateWithVendor(PayPalRequest paymentRequest) throws Exception {
        return paymentRequest.execute();
    }

    @Override
    public void setCommonDetailsResponse(@Nullable AgreementToken agreementToken,
            PaymentResponse paymentResponse,
            PaymentRequest paymentRequest,
            boolean checkoutComplete) {

        if (agreementToken != null) {
            paymentResponse.rawResponse(agreementToken.toJSON());

            com.paypal.api.payments.Address shippingAddress = agreementToken.getShippingAddress();

            if (shippingAddress != null) {
                paymentResponse.shipTo()
                        .addressLine1(shippingAddress.getLine1())
                        .addressLine2(shippingAddress.getLine2())
                        .city(shippingAddress.getCity())
                        .stateRegion(shippingAddress.getState())
                        .postalCode(shippingAddress.getPostalCode())
                        .countryCode(shippingAddress.getCountryCode())
                        .phoneNumber(shippingAddress.getPhone())
                        .done();
            }
        }

        paymentResponse.amount(paymentRequest.getTransactionTotal())
                .orderId(paymentRequest.getOrderId())
                .transactionReferenceId(paymentRequest.getTransactionReferenceId())
                .successful(true)
                .valid(true)
                .completeCheckoutOnCallback(checkoutComplete);
    }

    @Override
    public void setCommonDetailsResponse(Payment response, PaymentResponse responseDTO) {
        responseDTO.rawResponse(response.toJSON());

        if (CollectionUtils.isNotEmpty(response.getTransactions()) &&
                response.getTransactions().get(0) != null &&
                response.getTransactions().get(0).getItemList() != null) {
            ShippingAddress shippingAddress =
                    response.getTransactions().get(0).getItemList().getShippingAddress();

            String shipPhone = shippingAddress.getPhone();
            String itemListPhone =
                    response.getTransactions().get(0).getItemList().getShippingPhoneNumber();
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
                    .fullName(shippingAddress.getRecipientName())
                    .addressLine1(shippingAddress.getLine1())
                    .addressLine2(shippingAddress.getLine2())
                    .city(shippingAddress.getCity())
                    .stateRegion(shippingAddress.getState())
                    .postalCode(shippingAddress.getPostalCode())
                    .countryCode(shippingAddress.getCountryCode())
                    .phoneNumber(phone)
                    .done();

            if (shippingAddress.getStatus() != null) {
                responseDTO.getShipTo().additionalFields(MessageConstants.ADDRESSSTATUS,
                        shippingAddress.getStatus());
            }

            Transaction transaction = response.getTransactions().get(0);

            String itemTotal = getItemTotal(transaction);
            String shippingDiscount = getShippingDiscount(transaction);
            String shippingTotal = getShippingTotal(transaction);
            String totalTax = getTotalTax(transaction);
            MonetaryAmount total = getTotal(transaction);

            String[] customFields = transaction.getCustom().split("\\|");
            responseDTO.amount(total)
                    .successful(true)
                    .valid(true)
                    .completeCheckoutOnCallback(Boolean.parseBoolean(customFields[1]))
                    .responseMap(MessageConstants.DETAILSPAYMENTALLOWEDMETHOD,
                            response.getPayer().getPaymentMethod())
                    .responseMap(MessageConstants.DETAILSPAYMENTTRANSACTIONID, response.getId())
                    .responseMap(MessageConstants.DETAILSPAYMENTITEMTOTAL, itemTotal)
                    .responseMap(MessageConstants.DETAILSPAYMENTSHIPPINGDISCOUNT, shippingDiscount)
                    .responseMap(MessageConstants.DETAILSPAYMENTSHIPPINGTOTAL, shippingTotal)
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

    @Nullable
    private String getItemTotal(@Nullable Transaction transaction) {
        return Optional.ofNullable(transaction)
                .map(CartBase::getAmount)
                .map(Amount::getDetails)
                .map(Details::getSubtotal)
                .orElse(null);
    }

    @Nullable
    private String getShippingDiscount(@Nullable Transaction transaction) {
        return Optional.ofNullable(transaction)
                .map(CartBase::getAmount)
                .map(Amount::getDetails)
                .map(Details::getShippingDiscount)
                .orElse(null);
    }

    @Nullable
    private String getShippingTotal(@Nullable Transaction transaction) {
        return Optional.ofNullable(transaction)
                .map(CartBase::getAmount)
                .map(Amount::getDetails)
                .map(Details::getShipping)
                .orElse(null);
    }

    @Nullable
    private String getTotalTax(@Nullable Transaction transaction) {
        return Optional.ofNullable(transaction)
                .map(CartBase::getAmount)
                .map(Amount::getDetails)
                .map(Details::getTax)
                .orElse(null);
    }

    private MonetaryAmount getTotal(@Nullable Transaction transaction) {
        return Objects.requireNonNull(
                MonetaryUtils.toAmount(
                        Optional.ofNullable(transaction)
                                .map(CartBase::getAmount)
                                .map(Amount::getTotal)
                                .map(BigDecimal::new)
                                .orElse(BigDecimal.ZERO),
                        Optional.ofNullable(transaction)
                                .map(CartBase::getAmount)
                                .map(Amount::getCurrency)
                                .map(MonetaryUtils::getCurrency)
                                .orElse(MonetaryUtils.defaultCurrency())));
    }

    @Override
    public ShippingAddress getPayPalShippingAddress(PaymentRequest paymentRequest) {
        ShippingAddress shipAddress = new ShippingAddress();
        Address<PaymentRequest> address = paymentRequest.getShipTo();
        shipAddress.setRecipientName(address.getFullName());
        shipAddress.setLine1(address.getAddressLine1());
        shipAddress.setLine2(address.getAddressLine2());
        shipAddress.setCity(address.getCity());
        shipAddress.setState(getShortStateCode(address));
        shipAddress.setPostalCode(address.getPostalCode());
        shipAddress.setCountryCode(address.getCountryCode());
        if (StringUtils.isNotBlank(address.getPhoneNumber())) {
            shipAddress.setPhone(address.getPhoneNumber());
        }
        return shipAddress;
    }

    @Nullable
    private String getShortStateCode(Address<PaymentRequest> address) {
        String countryCode = address.getCountryCode();
        String stateRegion = address.getStateRegion();

        if (countryCode == null || stateRegion == null) {
            return stateRegion;
        }

        // PayPal supports only the State code with a short form, e.g. "AL" instead of "US-AL".
        String[] splitState = stateRegion.split(countryCode + "-");
        return splitState.length == 2 ? splitState[1] : splitState[0];
    }

    @Override
    @Nullable
    public ItemList getPayPalItemList(PaymentRequest paymentRequest,
            boolean shouldPopulateShipping) {
        ItemList itemList = new ItemList();
        boolean returnItemList = false;
        if (paymentRequest.shipToPopulated() && shouldPopulateShipping) {
            ShippingAddress address = getPayPalShippingAddress(paymentRequest);
            itemList.setShippingAddress(address);
            returnItemList = true;
        }

        if (CollectionUtils.isNotEmpty(paymentRequest.getLineItems())) {
            List<Item> items = new ArrayList<>();
            for (LineItem lineItem : paymentRequest.getLineItems()) {
                Item item = new Item();
                item.setCategory(lineItem.getCategory());
                item.setDescription(lineItem.getDescription());
                item.setQuantity(Objects.toString(lineItem.getQuantity(), null));
                item.setPrice(Objects.toString(lineItem.getTotal(), null));
                item.setTax(Objects.toString(lineItem.getTax(), null));
                item.setCurrency(paymentRequest.getOrderSubtotal().getCurrency().getCurrencyCode());
                item.setName(lineItem.getName());
                items.add(item);
            }
            itemList.setItems(items);
            returnItemList = true;
        }
        return returnItemList ? itemList : null;
    }

    @Override
    public Amount getPayPalAmountFromOrder(PaymentRequest paymentRequest) {
        Details details = new Details();

        details.setSubtotal(getStringAmount(paymentRequest.getOrderSubtotal()));
        details.setShipping(getStringAmount(paymentRequest.getShippingTotal()));
        details.setTax(getStringAmount(paymentRequest.getTaxTotal()));

        Amount amount = new Amount();
        amount.setCurrency(paymentRequest.getOrderSubtotal().getCurrency().getCurrencyCode());
        amount.setTotal(getStringAmount(paymentRequest.getTransactionTotal()));
        amount.setDetails(details);
        return amount;
    }

    @Nullable
    private String getStringAmount(@Nullable MonetaryAmount amount) {
        return Objects.toString(normalizePrice(amount), null);
    }

    @Nullable
    private BigDecimal normalizePrice(@Nullable MonetaryAmount amount) {
        if (amount == null) {
            return null;
        }

        return MonetaryUtils.toValue(amount);
    }

    @Override
    public String getServiceName() {
        return getClass().getName();
    }

    @Override
    public Integer getFailureReportingThreshold() {
        return gatewayConfiguration.getFailureReportingThreshold();
    }

    @Override
    public APIContext constructAPIContext(@lombok.NonNull PaymentRequest paymentRequest) {
        APIContext context = new APIContext(configProperties.getClientId(),
                configProperties.getClientSecret(),
                configProperties.getMode());
        context.setRequestId(buildIdempotencyKey(paymentRequest));
        context.addHTTPHeader(MessageConstants.BN, MessageConstants.BNCODE);

        if (paymentRequest.getAdditionalFields()
                .containsKey(MessageConstants.HTTP_HEADER_AUTH_ASSERTION)) {
            context.addHTTPHeader(MessageConstants.HTTP_HEADER_AUTH_ASSERTION,
                    (String) paymentRequest.getAdditionalFields()
                            .get(MessageConstants.HTTP_HEADER_AUTH_ASSERTION));
        }
        if (paymentRequest.getAdditionalFields()
                .containsKey(MessageConstants.HTTP_HEADER_CLIENT_METADATA_ID)) {
            context.addHTTPHeader(MessageConstants.HTTP_HEADER_CLIENT_METADATA_ID,
                    (String) paymentRequest.getAdditionalFields()
                            .get(MessageConstants.HTTP_HEADER_CLIENT_METADATA_ID));
        }
        if (paymentRequest.getAdditionalFields()
                .containsKey(MessageConstants.HTTP_HEADER_MOCK_RESPONSE)) {
            context.addHTTPHeader(MessageConstants.HTTP_HEADER_MOCK_RESPONSE,
                    (String) paymentRequest.getAdditionalFields()
                            .get(MessageConstants.HTTP_HEADER_MOCK_RESPONSE));
        }
        return context;
    }

    /**
     * Builds or gathers the idempotencyKey for the request. This value will guarantee that the
     * request cannot be processed twice & that if the request is made twice, that the second
     * response will be the same as the first response.
     *
     * Note: this value must be unique for each {@link TransactionType}. For example, if we want to
     * authorize & later capture a Stripe Payment, the capture request's idempotencyKey must be
     * different than the authorization request's idempotencyKey.
     *
     * @param paymentRequest the request that will be sent to PayPal
     * @return the idempotencyKey
     */
    protected String buildIdempotencyKey(@lombok.NonNull PaymentRequest paymentRequest) {
        Map<String, Object> additionalFields = paymentRequest.getAdditionalFields();

        if (additionalFields.containsKey(MessageConstants.IDEMPOTENCY_KEY)) {
            return (String) additionalFields.get(MessageConstants.IDEMPOTENCY_KEY);
        } else {
            return paymentRequest.getTransactionReferenceId();
        }
    }

}
