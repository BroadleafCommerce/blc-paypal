package org.broadleafcommerce.payment.service.gateway;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.broadleafcommerce.vendor.paypal.api.AgreementToken;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalRequest;
import org.broadleafcommerce.vendor.paypal.service.payment.PayPalResponse;

import com.broadleafcommerce.money.CurrencyContext;
import com.broadleafcommerce.money.SimpleCurrencyContext;
import com.broadleafcommerce.paymentgateway.domain.Address;
import com.broadleafcommerce.paymentgateway.domain.LineItem;
import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;
import com.broadleafcommerce.paymentgateway.domain.PaymentResponse;
import com.broadleafcommerce.paymentgateway.service.AbstractExternalPaymentGatewayCall;
import com.broadleafcommerce.paymentgateway.service.exception.PaymentException;
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
import java.util.Objects;
import java.util.Optional;

import javax.money.Monetary;

import lombok.RequiredArgsConstructor;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@RequiredArgsConstructor
public class DefaultExternalCallPayPalCheckoutService
        extends AbstractExternalPaymentGatewayCall<PayPalRequest, PayPalResponse>
        implements ExternalCallPayPalCheckoutService {

    private final PayPalCheckoutConfiguration configuration;

    @Override
    public PayPalCheckoutConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void setCommonDetailsResponse(AgreementToken agreementToken,
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
                .currencyContext(paymentRequest.getCurrencyContext())
                .orderId(paymentRequest.getOrderId())
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
            BigDecimal total = getTotal(transaction);
            CurrencyContext currencyContext = getCurrency(transaction);

            String[] customFields = transaction.getCustom().split("\\|");
            responseDTO.amount(total)
                    .currencyContext(currencyContext)
                    .orderId(customFields[0])
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

    private String getItemTotal(Transaction transaction) {
        return Optional.ofNullable(transaction)
                .map(CartBase::getAmount)
                .map(Amount::getDetails)
                .map(Details::getSubtotal)
                .orElse(null);
    }

    private String getShippingDiscount(Transaction transaction) {
        return Optional.ofNullable(transaction)
                .map(CartBase::getAmount)
                .map(Amount::getDetails)
                .map(Details::getShippingDiscount)
                .orElse(null);
    }

    private String getShippingTotal(Transaction transaction) {
        return Optional.ofNullable(transaction)
                .map(CartBase::getAmount)
                .map(Amount::getDetails)
                .map(Details::getShipping)
                .orElse(null);
    }

    private String getTotalTax(Transaction transaction) {
        return Optional.ofNullable(transaction)
                .map(CartBase::getAmount)
                .map(Amount::getDetails)
                .map(Details::getTax)
                .orElse(null);
    }

    private BigDecimal getTotal(Transaction transaction) {
        return Optional.ofNullable(transaction)
                .map(CartBase::getAmount)
                .map(Amount::getTotal)
                .map(BigDecimal::new)
                .orElse(BigDecimal.ZERO);
    }

    private CurrencyContext getCurrency(Transaction transaction) {
        return Optional.ofNullable(transaction)
                .map(CartBase::getAmount)
                .map(Amount::getCurrency)
                .map(currencyCode -> {
                    return new SimpleCurrencyContext(Monetary.getCurrency(currencyCode));
                })
                .orElse(null);
    }

    @Override
    public ShippingAddress getPayPalShippingAddress(PaymentRequest paymentRequest) {
        ShippingAddress shipAddress = new ShippingAddress();
        Address<PaymentRequest> address = paymentRequest.getShipTo();
        shipAddress.setRecipientName(address.getFullName());
        shipAddress.setLine1(address.getAddressLine1());
        shipAddress.setLine2(address.getAddressLine2());
        shipAddress.setCity(address.getCity());
        shipAddress.setState(address.getStateRegion());
        shipAddress.setPostalCode(address.getPostalCode());
        shipAddress.setCountryCode(address.getCountryCode());
        if (StringUtils.isNotBlank(address.getPhoneNumber())) {
            shipAddress.setPhone(address.getPhoneNumber());
        }
        return shipAddress;
    }

    @Override
    public ItemList getPayPalItemListFromOrder(PaymentRequest paymentRequest,
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
                item.setCurrency(paymentRequest.getCurrencyCode());
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

        details.setShipping(Objects.toString(paymentRequest.getShippingTotal(), null));
        details.setSubtotal(Objects.toString(paymentRequest.getOrderSubtotal(), null));
        details.setTax(Objects.toString(paymentRequest.getTaxTotal(), null));

        Amount amount = new Amount();
        amount.setCurrency(paymentRequest.getCurrencyCode());
        amount.setTotal(Objects.toString(paymentRequest.getTransactionTotal(), null));
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

    @Override
    public APIContext constructAPIContext(PaymentRequest paymentRequest) {
        APIContext context = initializeAPIContext();
        if (paymentRequest.getAdditionalFields()
                .containsKey(MessageConstants.HTTP_HEADER_REQUEST_ID)) {
            context.setRequestId((String) paymentRequest.getAdditionalFields()
                    .get(MessageConstants.HTTP_HEADER_REQUEST_ID));
        }
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

    private APIContext initializeAPIContext() {
        APIContext context = new APIContext(configuration.getCheckoutRestClientId(),
                configuration.getCheckoutRestSecret(),
                configuration.getCheckoutRestMode());
        context.addHTTPHeader(MessageConstants.BN, MessageConstants.BNCODE);
        return context;
    }

}
