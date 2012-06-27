package org.broadleafcommerce.vendor.braintree.web;

import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.broadleafcommerce.common.time.SystemTime;
import org.broadleafcommerce.core.order.domain.FulfillmentGroup;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.CartService;
import org.broadleafcommerce.core.payment.domain.CreditCardPaymentInfo;
import org.broadleafcommerce.core.payment.domain.PaymentInfo;
import org.broadleafcommerce.core.payment.domain.PaymentInfoImpl;
import org.broadleafcommerce.core.payment.domain.Referenced;
import org.broadleafcommerce.core.payment.service.PaymentInfoService;
import org.broadleafcommerce.core.payment.service.SecurePaymentInfoService;
import org.broadleafcommerce.core.payment.service.type.PaymentInfoType;
import org.broadleafcommerce.core.web.checkout.model.CheckoutForm;
import org.broadleafcommerce.profile.core.domain.*;
import org.broadleafcommerce.profile.core.service.CountryService;
import org.broadleafcommerce.profile.core.service.CustomerAddressService;
import org.broadleafcommerce.profile.core.service.CustomerPhoneService;
import org.broadleafcommerce.profile.core.service.StateService;
import org.broadleafcommerce.profile.web.core.CustomerState;
import org.broadleafcommerce.vendor.braintree.service.payment.BraintreePaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: chadharchar
 * Date: 3/19/12
 * Time: 4:26 PM
 * To change this template use File | Settings | File Templates.
 */

public class BraintreeFormController {

    @Resource(name="blCustomerState")
    protected CustomerState customerState;
    @Resource(name="blCartService")
    protected CartService cartService;
    @Resource(name="blCustomerAddressService")
    protected CustomerAddressService customerAddressService;
    @Resource(name="blCustomerPhoneService")
    protected CustomerPhoneService customerPhoneService;
    @Resource(name="blStateService")
    protected StateService stateService;
    @Resource(name="blCountryService")
    protected CountryService countryService;
    @Resource(name="blPaymentInfoService")
    protected PaymentInfoService paymentInfoService;
    @Resource(name="blSecurePaymentInfoService")
    protected SecurePaymentInfoService securePaymentInfoService;
    
    protected String braintreeFormView;
    protected BraintreePaymentService braintreePaymentService;
    protected String braintreeContactInfoView;
    

    @RequestMapping(value = "/braintreeForm.htm", method = {RequestMethod.GET})
    public String braintreeForm(ModelMap model,
                                HttpServletRequest request, CheckoutForm checkoutForm) {
        Order order = retrieveCartOrder(request, model);
        
        TransactionRequest trParams = new TransactionRequest();
        trParams = trSetup(trParams, order, checkoutForm);
        trParams = trBilling(trParams, checkoutForm.getBillingAddress());
        trParams = trShipping(trParams, checkoutForm.getShippingAddress());

        String trData = braintreePaymentService.makeTrData(trParams);
        String trUrl = braintreePaymentService.makeTrUrl();
        model.addAttribute("trData", trData);
        model.addAttribute("trUrl", trUrl);
        
        return braintreeFormView;
    }

    public TransactionRequest trSetup(TransactionRequest trParams, Order order, CheckoutForm checkoutForm) {
        return  trParams.
                type(Transaction.Type.SALE).
                amount(new BigDecimal(order.getTotal().toString())).
                orderId(order.getOrderNumber()).
                //merchantAccountId("a_merchant_account_id").
                customer().
                firstName(order.getCustomer().getFirstName()).
                lastName(order.getCustomer().getLastName()).
                phone(checkoutForm.getBillingAddress().getPrimaryPhone()).
                email(checkoutForm.getEmailAddress()).
                done();
    }

    public TransactionRequest trShipping(TransactionRequest trParams, Address shippingAddress){
        return  trParams.
                shippingAddress().
                firstName(shippingAddress.getFirstName()).
                lastName(shippingAddress.getLastName()).
                company(shippingAddress.getCompanyName()).
                streetAddress(shippingAddress.getAddressLine1()).
                extendedAddress(shippingAddress.getAddressLine2()).
                locality(shippingAddress.getCity()).
                region(shippingAddress.getState().getAbbreviation()).
                postalCode(shippingAddress.getPostalCode()).
                countryCodeAlpha2(shippingAddress.getCountry().getAbbreviation()).
                done();
    }
    
    public TransactionRequest trBilling(TransactionRequest trParams, Address billingAddress){
        return  trParams.
                billingAddress().
                firstName(billingAddress.getFirstName()).
                lastName(billingAddress.getLastName()).
                company(billingAddress.getCompanyName()).
                streetAddress(billingAddress.getAddressLine1()).
                extendedAddress(billingAddress.getAddressLine2()).
                locality(billingAddress.getCity()).
                region(billingAddress.getState().getAbbreviation()).
                postalCode(billingAddress.getPostalCode()).
                countryCodeAlpha2(billingAddress.getCountry().getAbbreviation()).
                done();
    }
    
    

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/braintreeContactInfo.htm", method = {RequestMethod.GET})
    public String braintreeContactInfo(@ModelAttribute CheckoutForm checkoutForm,
                           BindingResult errors,
                           ModelMap model,
                           HttpServletRequest request) {

        model.addAttribute("stateList", stateService.findStates());
        List<Country> countries = countryService.findCountries();
        Collections.sort(countries, new ReverseComparator(new BeanComparator("abbreviation")));
        model.addAttribute("countryList", countries);

        Customer currentCustomer = customerState.getCustomer(request);
        model.addAttribute("customer", currentCustomer);

        List<CustomerPhone> customerPhones = customerPhoneService.readAllCustomerPhonesByCustomerId(currentCustomer.getId());
        while(customerPhones.size() < 2) {
            customerPhones.add(new CustomerPhoneImpl());
        }

        customerAddressService.readActiveCustomerAddressesByCustomerId(currentCustomer.getId());
        model.addAttribute("order", retrieveCartOrder(request, model));
        return braintreeContactInfoView;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(params = "braintreeProcessInfo", method = {RequestMethod.POST})
    public String braintreeProcessInfo(@ModelAttribute CheckoutForm checkoutForm,
                                       BindingResult errors,
                                       ModelMap model,
                                       HttpServletRequest request) {

        if (checkoutForm.getIsSameAddress()) {
            copyAddress(checkoutForm);
        }

        //checkoutFormValidator.validate(checkoutForm, errors);

        if (errors.hasErrors()) {
            return braintreeContactInfo(checkoutForm, errors, model, request);
        }

        checkoutForm.getBillingAddress().setCountry(countryService.findCountryByAbbreviation(checkoutForm.getBillingAddress().getCountry().getAbbreviation()));
        checkoutForm.getBillingAddress().setState(stateService.findStateByAbbreviation(checkoutForm.getBillingAddress().getState().getAbbreviation()));
        checkoutForm.getShippingAddress().setCountry(countryService.findCountryByAbbreviation(checkoutForm.getShippingAddress().getCountry().getAbbreviation()));
        checkoutForm.getShippingAddress().setState(stateService.findStateByAbbreviation(checkoutForm.getShippingAddress().getState().getAbbreviation()));

        Order order = retrieveCartOrder(request, model);
        order.setOrderNumber(new SimpleDateFormat("yyyyMMddHHmmssS").format(SystemTime.asDate()));

        List<FulfillmentGroup> groups = order.getFulfillmentGroups();
        if(groups.size() < 1){
            return "redirect:/basket/currentCart.htm";
        }
        FulfillmentGroup group = groups.get(0);
        group.setOrder(order);
        group.setAddress(checkoutForm.getShippingAddress());
        group.setShippingPrice(order.getTotalShipping());

        Map<PaymentInfo, Referenced> payments = new HashMap<PaymentInfo, Referenced>();
        CreditCardPaymentInfo creditCardPaymentInfo = ((CreditCardPaymentInfo) securePaymentInfoService.create(PaymentInfoType.CREDIT_CARD));
        creditCardPaymentInfo.setReferenceNumber(order.getOrderNumber());
        PaymentInfo paymentInfo = paymentInfoService.create();
        paymentInfo.setAddress(checkoutForm.getBillingAddress());
        paymentInfo.setOrder(order);
        paymentInfo.setType(PaymentInfoType.CREDIT_CARD);
        paymentInfo.setAmount(order.getTotal());
        payments.put(paymentInfo, creditCardPaymentInfo);

        List<PaymentInfo> paymentInfos = new ArrayList<PaymentInfo>();
        paymentInfos.add(paymentInfo);
        order.setPaymentInfos(paymentInfos);
        paymentInfo.setReferenceNumber(order.getOrderNumber());

        return braintreeForm(model, request, checkoutForm);
    }

    public void setBraintreeFormView(String braintreeFormView) {
        this.braintreeFormView = braintreeFormView;
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

    private CheckoutForm copyAddress (CheckoutForm checkoutForm) {
        checkoutForm.getShippingAddress().setFirstName(checkoutForm.getBillingAddress().getFirstName());
        checkoutForm.getShippingAddress().setLastName(checkoutForm.getBillingAddress().getLastName());
        checkoutForm.getShippingAddress().setAddressLine1(checkoutForm.getBillingAddress().getAddressLine1());
        checkoutForm.getShippingAddress().setAddressLine2(checkoutForm.getBillingAddress().getAddressLine2());
        checkoutForm.getShippingAddress().setCity(checkoutForm.getBillingAddress().getCity());
        checkoutForm.getShippingAddress().setState(checkoutForm.getBillingAddress().getState());
        checkoutForm.getShippingAddress().setPostalCode(checkoutForm.getBillingAddress().getPostalCode());
        checkoutForm.getShippingAddress().setCountry(checkoutForm.getBillingAddress().getCountry());
        checkoutForm.getShippingAddress().setPrimaryPhone(checkoutForm.getBillingAddress().getPrimaryPhone());

        return checkoutForm;
    }

    public String getBraintreeContactInfoView() {
        return braintreeContactInfoView;
    }

    public void setBraintreeContactInfoView(String braintreeContactInfoView) {
        this.braintreeContactInfoView = braintreeContactInfoView;
    }

    public BraintreePaymentService getBraintreePaymentService() {
        return braintreePaymentService;
    }

    public void setBraintreePaymentService(BraintreePaymentService braintreePaymentService) {
        this.braintreePaymentService = braintreePaymentService;
    }
}
