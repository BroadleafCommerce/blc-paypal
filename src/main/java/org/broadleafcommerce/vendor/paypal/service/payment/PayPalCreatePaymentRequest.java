package org.broadleafcommerce.vendor.paypal.service.payment;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Details;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;


public class PayPalCreatePaymentRequest extends PayPalRequest {

    protected Payment payment;

    public PayPalCreatePaymentRequest(Payment payment, APIContext apiContext) {
        super(apiContext);
        this.payment = payment;
    }

    @Override
    protected PayPalResponse executeInternal() throws PayPalRESTException {
        return new PayPalCreatePaymentResponse(payment.create(apiContext));
    }

    @Override
    protected boolean isRequestValid() {
        boolean baseCaseValid = payment != null && payment.getPayer() != null
                && CollectionUtils.isNotEmpty(payment.getTransactions());
        if (baseCaseValid) {
            Transaction transaction = payment.getTransactions().get(0);
            if (transaction != null && transaction.getAmount() != null) {
                Amount amount = transaction.getAmount();
                Details details = amount.getDetails();
                boolean amountConditions = (amount.getCurrency() == null
                        || StringUtils.isNotBlank(amount.getCurrency()))
                        && StringUtils.isNotBlank(amount.getTotal());
                boolean detailsConditions = details != null
                        && StringUtils.isNotBlank(details.getSubtotal())
                        && (details.getShipping() == null
                                || StringUtils.isNotBlank(details.getShipping()))
                        && (details.getTax() == null || StringUtils.isNotBlank(details.getTax()));
                return amountConditions && detailsConditions;
            }
        }
        return false;
    }

}
