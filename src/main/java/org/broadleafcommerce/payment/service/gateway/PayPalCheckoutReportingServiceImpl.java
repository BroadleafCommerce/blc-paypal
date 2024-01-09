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
package org.broadleafcommerce.payment.service.gateway;

import com.paypal.orders.AddressPortable;
import com.paypal.orders.AmountBreakdown;
import com.paypal.orders.AmountWithBreakdown;
import com.paypal.orders.Money;
import com.paypal.orders.Name;
import com.paypal.orders.Order;
import com.paypal.orders.Payer;
import com.paypal.orders.Phone;
import com.paypal.orders.PhoneWithType;
import com.paypal.orders.PurchaseUnit;
import com.paypal.orders.ShippingDetail;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.payment.PaymentType;
import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.AbstractPaymentGatewayReportingService;
import org.broadleafcommerce.common.payment.service.PaymentGatewayReportingService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.vendor.paypal.service.PayPalClientProvider;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalCheckoutPaymentGatewayType;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalOrderRetrievalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalOrderRetrievalResponse;
import org.broadleafcommerce.vendor.paypal.service.util.PayPalUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Service("blPayPalCheckoutReportingService")
public class PayPalCheckoutReportingServiceImpl extends AbstractPaymentGatewayReportingService implements PaymentGatewayReportingService {

    private static final Log LOG = LogFactory.getLog(PayPalCheckoutReportingServiceImpl.class);

    @Resource(name = "blExternalCallPayPalCheckoutService")
    protected ExternalCallPayPalCheckoutService payPalCheckoutService;

    @Resource(name = "blPayPalClientProvider")
    protected PayPalClientProvider clientProvider;

    @Resource(name = "blPayPalUtils")
    protected PayPalUtils utils;

    @Override
    public PaymentResponseDTO findDetailsByTransaction(PaymentRequestDTO paymentRequestDTO) throws PaymentException {
        Map<String, Object> additionalFields = paymentRequestDTO.getAdditionalFields();
        Assert.isTrue(additionalFields.containsKey(MessageConstants.HTTP_PAYERID), "The RequestDTO must contain a payerID");
        Assert.isTrue(additionalFields.containsKey(MessageConstants.HTTP_ORDER_ID), "The RequestDTO must contain a orderId");

        String orderId = (String) additionalFields.get(MessageConstants.HTTP_ORDER_ID);
        PayPalOrderRetrievalRequest orderRetrievalRequest = new PayPalOrderRetrievalRequest(clientProvider, paymentRequestDTO, orderId);
        PayPalOrderRetrievalResponse response = payPalCheckoutService.call(orderRetrievalRequest, PayPalOrderRetrievalResponse.class);
        PaymentResponseDTO responseDTO = new PaymentResponseDTO(PaymentType.THIRD_PARTY_ACCOUNT,
                PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT_V2);
        setCommonDetailsResponse(response, responseDTO);
        String payerId = (String) additionalFields.get(MessageConstants.HTTP_PAYERID);
        responseDTO.orderId(paymentRequestDTO.getOrderId());
        responseDTO.responseMap(MessageConstants.PAYERID, payerId)
                .responseMap(MessageConstants.ORDER_ID, orderId);
        LOG.info("ResponseDTO created: " + ToStringBuilder.reflectionToString(responseDTO, ToStringStyle.MULTI_LINE_STYLE));
        return responseDTO;
    }

    protected void setCommonDetailsResponse(PayPalOrderRetrievalResponse response,
                                            PaymentResponseDTO responseDTO) {
        Order order = response.getContent();
        responseDTO.rawResponse(utils.convertResponseToJson(response.getResponse()));
        responseDTO.responseMap(MessageConstants.ORDER_STATUS, order.status());

        List<PurchaseUnit> purchaseUnits = order.purchaseUnits();

        if (CollectionUtils.isEmpty(purchaseUnits)
                || purchaseUnits.get(0) == null) {
            return;
        }

        PurchaseUnit purchaseUnit = purchaseUnits.get(0);
        ShippingDetail shippingDetail = purchaseUnit.shippingDetail();
        String phone = Optional.ofNullable(order.payer())
                .map(Payer::phoneWithType)
                .map(PhoneWithType::phoneNumber)
                .map(Phone::nationalNumber)
                .orElse("");

        if (shippingDetail != null) {
            AddressPortable shipAddress = shippingDetail.addressPortable();
            responseDTO.shipTo()
                    .addressFullName(shippingDetail.name().fullName())
                    .addressLine1(shipAddress.addressLine1())
                    .addressLine2(shipAddress.addressLine2())
                    .addressCityLocality(shipAddress.adminArea2())
                    .addressStateRegion(shipAddress.adminArea1())
                    .addressPostalCode(shipAddress.postalCode())
                    .addressCountryCode(shipAddress.countryCode())
                    .addressPhone(phone)
                    .done();
        }

        String itemTotal = getItemTotal(purchaseUnit);
        String shippingDiscount = getShippingDiscount(purchaseUnit);
        String shippingTotal = getShippingTotal(purchaseUnit);
        String totalTax = getTotalTax(purchaseUnit);
        AmountWithBreakdown amountWithBreakdown = purchaseUnit.amountWithBreakdown();
        org.broadleafcommerce.common.money.Money amount =
                new org.broadleafcommerce.common.money.Money(amountWithBreakdown.value(), amountWithBreakdown.currencyCode());

        String[] customFields = purchaseUnit.customId().split("\\|");
        responseDTO.amount(amount)
                .successful(true)
                .valid(true)
                .completeCheckoutOnCallback(Boolean.parseBoolean(customFields[1]))
                .responseMap(MessageConstants.DETAILSPAYMENTTRANSACTIONID, purchaseUnit.id())
                .responseMap(MessageConstants.DETAILSPAYMENTITEMTOTAL, itemTotal)
                .responseMap(MessageConstants.DETAILSPAYMENTSHIPPINGDISCOUNT,
                        shippingDiscount)
                .responseMap(MessageConstants.DETAILSPAYMENTSHIPPINGTOTAL, shippingTotal)
                .responseMap(MessageConstants.DETAILSPAYMENTTOTALTAX, totalTax);

        Payer payer = order.payer();

        if (payer != null) {
            responseDTO.customer()
                    .firstName(Optional.ofNullable(payer.name()).map(Name::givenName).orElse(""))
                    .lastName(Optional.ofNullable(payer.name()).map(Name::surname).orElse(""))
                    .phone(phone)
                    .email(payer.email())
                    .done();
        }
    }

    @Nullable
    private String getItemTotal(PurchaseUnit purchaseUnit) {
        return Optional.of(purchaseUnit)
                .map(PurchaseUnit::amountWithBreakdown)
                .map(AmountWithBreakdown::amountBreakdown)
                .map(AmountBreakdown::itemTotal)
                .map(Money::value)
                .orElse(null);
    }

    @Nullable
    private String getShippingDiscount(PurchaseUnit purchaseUnit) {
        return Optional.of(purchaseUnit)
                .map(PurchaseUnit::amountWithBreakdown)
                .map(AmountWithBreakdown::amountBreakdown)
                .map(AmountBreakdown::shippingDiscount)
                .map(Money::value)
                .orElse(null);
    }

    @Nullable
    private String getShippingTotal(PurchaseUnit purchaseUnit) {
        return Optional.of(purchaseUnit)
                .map(PurchaseUnit::amountWithBreakdown)
                .map(AmountWithBreakdown::amountBreakdown)
                .map(AmountBreakdown::shipping)
                .map(Money::value)
                .orElse(null);
    }

    @Nullable
    private String getTotalTax(PurchaseUnit purchaseUnit) {
        return Optional.of(purchaseUnit)
                .map(PurchaseUnit::amountWithBreakdown)
                .map(AmountWithBreakdown::amountBreakdown)
                .map(AmountBreakdown::taxTotal)
                .map(Money::value)
                .orElse(null);
    }
}
