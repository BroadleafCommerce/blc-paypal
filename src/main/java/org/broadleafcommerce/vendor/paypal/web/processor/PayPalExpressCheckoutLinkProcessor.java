/*
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2014 Broadleaf Commerce
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.broadleafcommerce.vendor.paypal.web.processor;

import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.PaymentGatewayHostedService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.vendor.paypal.service.payment.MessageConstants;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.attr.AbstractAttributeModifierAttrProcessor;
import org.thymeleaf.standard.expression.StandardExpressionProcessor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * <p>A Thymeleaf processor that will generate a Redirect Link given a passed in PaymentRequestDTO.</p>
 *
 * <pre><code>
 * <a blc:paypal_express_link="${dto}" complete_checkout="${false}">
 *  <img src="https://www.paypal.com/en_US/i/btn/btn_xpressCheckout.gif" align="left" style="margin-right:7px;"/>
 * </a>
 * </code></pre>
 *
 * @author Elbert Bautista (elbertbautista)
 * @deprecated - use {@link org.broadleafcommerce.vendor.paypal.web.controller.BroadleafPayPalExpressController#redirectEndpoint(Model, HttpServletRequest)}
 */
@Deprecated
@Component("blPayPalExpressCheckoutLinkProcessor")
public class PayPalExpressCheckoutLinkProcessor extends AbstractAttributeModifierAttrProcessor {

    @Resource(name = "blPayPalExpressHostedService")
    private PaymentGatewayHostedService paymentGatewayHostedService;

    /**
     * Sets the name of this processor to be used in Thymeleaf template
     */
    public PayPalExpressCheckoutLinkProcessor() {
        super("paypal_express_link");
    }

    @Override
    public int getPrecedence() {
        return 10000;
    }

    @Override
    protected Map<String, String> getModifiedAttributeValues(Arguments arguments, Element element, String attributeName) {
        Map<String, String> attrs = new HashMap<String, String>();

        PaymentRequestDTO requestDTO = (PaymentRequestDTO) StandardExpressionProcessor.processExpression(arguments, element.getAttributeValue(attributeName));
        String url = "";

        if (requestDTO != null) {
            if ( element.getAttributeValue("complete_checkout") != null) {
                Boolean completeCheckout = (Boolean) StandardExpressionProcessor.processExpression(arguments,
                    element.getAttributeValue("complete_checkout"));
                element.removeAttribute("complete_checkout");
                requestDTO.completeCheckoutOnCallback(completeCheckout);
            }

            try {
                PaymentResponseDTO responseDTO = paymentGatewayHostedService.requestHostedEndpoint(requestDTO);
                url = responseDTO.getResponseMap().get(MessageConstants.REDIRECTURL).toString();

                //https://developer.paypal.com/docs/classic/express-checkout/integration-guide/ECCustomizing/
                if (requestDTO.isCompleteCheckoutOnCallback()) {
                    url = url + "&useraction=commit";
                }

            } catch (PaymentException e) {
                throw new RuntimeException("Unable to Create the PayPal Express Link", e);
            }
        }

        attrs.put("href", url);
        return attrs;
    }

    @Override
    protected ModificationType getModificationType(Arguments arguments, Element element, String attributeName, String newAttributeName) {
        return ModificationType.SUBSTITUTION;
    }

    @Override
    protected boolean removeAttributeIfEmpty(Arguments arguments, Element element, String attributeName, String newAttributeName) {
        return true;
    }

    @Override
    protected boolean recomputeProcessorsAfterExecution(Arguments arguments, Element element, String attributeName) {
        return false;
    }
}
