package org.broadleafcommerce.payment.service.gateway;

import lombok.AccessLevel;
import lombok.Getter;
import org.broadleafcommerce.vendor.paypal.service.PayPalPaymentService;

import com.broadleafcommerce.paymentgateway.domain.PaymentRequest;

import lombok.RequiredArgsConstructor;

/**
 * @author Dima Myroniuk (dmyroniuk)
 */
@RequiredArgsConstructor
public class DefaultPayPalPaymentGatewayService implements PayPalPaymentGatewayService {
    @Getter(AccessLevel.PROTECTED)
    private final PayPalPaymentService payPalPaymentService;

    @Override
    public void update(PaymentRequest paymentRequest) {
        payPalPaymentService.updatePayPalPaymentForFulfillment(paymentRequest);
    }

    @Override
    public String getGatewayType() {
        return PayPalCheckoutPaymentGatewayType.PAYPAL_CHECKOUT.name();
    }

}
