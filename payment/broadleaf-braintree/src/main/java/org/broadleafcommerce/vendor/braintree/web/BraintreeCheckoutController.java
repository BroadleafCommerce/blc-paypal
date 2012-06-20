package org.broadleafcommerce.vendor.braintree.web;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.core.checkout.service.CheckoutService;
import org.broadleafcommerce.core.checkout.service.exception.CheckoutException;
import org.broadleafcommerce.core.checkout.service.workflow.CheckoutResponse;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.CartService;
import org.broadleafcommerce.core.order.service.type.OrderStatus;
import org.broadleafcommerce.core.payment.domain.CreditCardPaymentInfo;
import org.broadleafcommerce.core.payment.domain.PaymentInfo;
import org.broadleafcommerce.core.payment.domain.PaymentResponseItem;
import org.broadleafcommerce.core.payment.domain.Referenced;
import org.broadleafcommerce.core.payment.service.CompositePaymentService;
import org.broadleafcommerce.core.payment.service.PaymentInfoService;
import org.broadleafcommerce.core.payment.service.SecurePaymentInfoService;
import org.broadleafcommerce.core.payment.service.exception.PaymentException;
import org.broadleafcommerce.core.payment.service.type.PaymentInfoType;
import org.broadleafcommerce.core.payment.service.workflow.CompositePaymentResponse;
import org.broadleafcommerce.payment.service.module.BraintreePaymentModule;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CountryService;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.broadleafcommerce.profile.core.service.StateService;
import org.broadleafcommerce.profile.web.core.CustomerState;
import org.broadleafcommerce.vendor.braintree.service.payment.MessageConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: chadharchar
 * Date: 3/19/12
 * Time: 5:45 PM
 * To change this template use File | Settings | File Templates.
 */

@Controller("braintreeCheckoutController")
@RequestMapping("/braintreeCheckout")
public class BraintreeCheckoutController {

    private static final Log LOG = LogFactory.getLog(BraintreeCheckoutController.class);

    @Resource(name="blCartService")
    protected CartService cartService;
    @Resource(name="blCustomerState")
    protected CustomerState customerState;
    @Resource(name="blCheckoutService")
    protected CheckoutService checkoutService;
    @Resource(name="blStateService")
    protected StateService stateService;
    @Resource(name="blCountryService")
    protected CountryService countryService;
    @Resource(name="blPaymentInfoService")
    protected PaymentInfoService paymentInfoService;
    @Resource(name="blSecurePaymentInfoService")
    protected SecurePaymentInfoService securePaymentInfoService;
    @Resource(name="blCustomerService")
    protected CustomerService customerService;

    @Resource(name="debitCompositePaymentService")
    protected CompositePaymentService debitCompositePaymentService;

    @Resource(name="reverseAuthorizeCompositePaymentService")
    protected CompositePaymentService reverseAuthorizeCompositePaymentService;

    @Resource(name="refundCompositePaymentService")
    protected CompositePaymentService refundCompositePaymentService;

    @Resource(name="blBraintreeModule")
    protected BraintreePaymentModule braintreePaymentModule;

    protected String braintreeCheckoutView;
    
    @RequestMapping(value = "/braintreeProcess.htm", method = {RequestMethod.GET})
    public String braintreeProcess(@RequestParam String transactionType,
                                   ModelMap model, 
                                   HttpServletRequest request){
        Order order = retrieveCartOrder(request, model);
        String queryString = request.getQueryString();

        Map<PaymentInfo, Referenced> payments = new HashMap<PaymentInfo, Referenced>();

        CreditCardPaymentInfo creditCardPaymentInfo = ((CreditCardPaymentInfo) securePaymentInfoService.create(PaymentInfoType.CREDIT_CARD));
        creditCardPaymentInfo.setReferenceNumber(order.getOrderNumber());

        order.getPaymentInfos().get(0).getAdditionalFields().put(MessageConstants.QUERYSTRING, queryString);
        payments.put(order.getPaymentInfos().get(0), creditCardPaymentInfo);

        order.setSubmitDate(Calendar.getInstance().getTime());

        PaymentResponseItem responseItem = null;

        responseItem = braintreeAuthorizeAndDebit(order, payments);

        if (responseItem.getTransactionSuccess()) {

            Customer customer = order.getCustomer();
            if (StringUtils.isEmpty(customer.getFirstName())) {
                customer.setFirstName(order.getFulfillmentGroups().get(0).getAddress().getFirstName());
            }
            if (StringUtils.isEmpty(customer.getLastName())) {
                customer.setLastName(order.getFulfillmentGroups().get(0).getAddress().getLastName());
            }
            if (StringUtils.isEmpty(customer.getEmailAddress())) {
                customer.setEmailAddress(order.getEmailAddress());
            }
            customerService.saveCustomer(customer, false);
            order.getPaymentInfos().get(0).getAdditionalFields().put(MessageConstants.BRAINTREEID, responseItem.getTransactionId());
            model.addAttribute("order", order);
            return "redirect:/orders/viewOrderConfirmation.htm?orderNumber=" + order.getOrderNumber();
        } else {
            LOG.error(responseItem.getAdditionalFields().get(MessageConstants.MESSAGE));
        }

        return "";
    }

    @RequestMapping(value = "/braintreeAuthorizeAndDebit.htm", method = {RequestMethod.GET})
    public PaymentResponseItem braintreeAuthorizeAndDebit(Order order,
                                   Map<PaymentInfo, Referenced> payments){

        order.setStatus(OrderStatus.SUBMITTED);
        CheckoutResponse checkoutResponse;
        PaymentResponseItem responseItem = null;

        try {
            checkoutResponse = checkoutService.performCheckout(order, payments);
            responseItem = checkoutResponse.getPaymentResponse().getResponseItems().get(order.getPaymentInfos().get(0));
        } catch (CheckoutException e) {
            LOG.error("Cannot perform checkout", e);
        }

        return responseItem;
    }

    @RequestMapping(value = "/braintreeDebit.htm", method = {RequestMethod.GET})
    public String braintreeDebit(@RequestParam String orderNumber){

        Order order = cartService.findOrderByOrderNumber(orderNumber);
        Map<PaymentInfo, Referenced> payments = new HashMap<PaymentInfo, Referenced>();
        payments.put(order.getPaymentInfos().get(0), order.getPaymentInfos().get(0).createEmptyReferenced());
        order.setStatus(OrderStatus.SUBMITTED);

        try{
            CompositePaymentResponse compositePaymentResponse = debitCompositePaymentService.executePayment(order, payments);
            PaymentResponseItem responseItem = compositePaymentResponse.getPaymentResponse().getPaymentResponseItem(order.getPaymentInfos().get(0));
            if(responseItem.getTransactionSuccess()) {
                return "redirect:/orders/viewOrderConfirmation.htm?orderNumber=" + order.getOrderNumber();
            } else {
                LOG.error(responseItem.getAdditionalFields().get(MessageConstants.MESSAGE));
            }
        } catch(PaymentException e) {
            LOG.error("Cannot perform capture", e);
        }
        return "";
    }

    @RequestMapping(value = "/braintreeRefund.htm", method = {RequestMethod.GET})
    public String braintreeRefund(@RequestParam String orderNumber){
        Order order = cartService.findOrderByOrderNumber(orderNumber);
        Map<PaymentInfo, Referenced> payments = new HashMap<PaymentInfo, Referenced>();
        payments.put(order.getPaymentInfos().get(0), order.getPaymentInfos().get(0).createEmptyReferenced());

        try{
            CompositePaymentResponse compositePaymentResponse = refundCompositePaymentService.executePayment(order, payments);
            PaymentResponseItem responseItem = compositePaymentResponse.getPaymentResponse().getPaymentResponseItem(order.getPaymentInfos().get(0));
            if(responseItem.getTransactionSuccess()) {
                return "redirect:/orders/viewOrderConfirmation.htm?orderNumber=" + order.getOrderNumber();
            } else {
                LOG.error(responseItem.getAdditionalFields().get(MessageConstants.MESSAGE));
            }
        } catch(PaymentException e) {
            LOG.error("Cannot perform refund", e);
        }
        return "";
    }

    @RequestMapping(value = "/braintreeVoid.htm", method = {RequestMethod.GET})
    public String braintreeVoid(@RequestParam String orderNumber){
        Order order = cartService.findOrderByOrderNumber(orderNumber);
        Map<PaymentInfo, Referenced> payments = new HashMap<PaymentInfo, Referenced>();
        payments.put(order.getPaymentInfos().get(0), order.getPaymentInfos().get(0).createEmptyReferenced());
        //order.setStatus(OrderStatus.VOIDED);
        try{
            CompositePaymentResponse compositePaymentResponse = reverseAuthorizeCompositePaymentService.executePayment(order, payments);
            PaymentResponseItem responseItem = compositePaymentResponse.getPaymentResponse().getPaymentResponseItem(order.getPaymentInfos().get(0));
            if(responseItem.getTransactionSuccess()) {

                return "redirect:/orders/viewOrderConfirmation.htm?orderNumber=" + order.getOrderNumber();
            } else {
                LOG.error(responseItem.getAdditionalFields().get(MessageConstants.MESSAGE));
            }
        } catch(PaymentException e) {
            LOG.error("Cannot perform void", e);
        }
        return "";
    }

    protected Order retrieveCartOrder(HttpServletRequest request, ModelMap model) {
        Customer currentCustomer = customerState.getCustomer(request);
        Order currentCartOrder = null;
        if (currentCustomer != null) {
            currentCartOrder = cartService.findCartForCustomer(currentCustomer);
            if (currentCartOrder == null) {
                currentCartOrder = cartService.createNewCartForCustomer(currentCustomer);
            }
        }

        return currentCartOrder;
    }

    public void setBraintreeCheckoutView(String braintreeCheckoutView) {
        this.braintreeCheckoutView = braintreeCheckoutView;
    }
}
