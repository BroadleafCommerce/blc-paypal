/*
 * #%L
 * BroadleafCommerce PayPal
 * %%
 * Copyright (C) 2009 - 2017 Broadleaf Commerce
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
/**
 * 
 */
package org.broadleafcommerce.vendor.paypal.service.payment;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Serializer for the {@link MessageConstants#DETAILSPAYMENTCUSTOM} field going between a map
 * and a String.
 * 
 * @author Phillip Verheyden (phillipuniverse)
 */
@Component("blCustomFieldSerializer")
public class CustomFieldSerializer {

    protected String serializeCustomFields(Map<String, String> fields) {
        StringBuffer sb = new StringBuffer();
        Iterator<Entry<String, String>> fieldsIt = fields.entrySet().iterator();
        while (fieldsIt.hasNext()) {
            Entry<String, String> entry = fieldsIt.next();
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(entry.getValue());
            
            if (fieldsIt.hasNext()) {
                sb.append('|');
            }
        }
        return sb.toString();
    }
    
    protected Map<String, String> deserializeCustomFields(String seralized) {
        Map<String, String> result = new HashMap<String, String>();
        String[] fields = StringUtils.split(seralized, '|');
        for (String field : fields) {
            String[] fieldParts = StringUtils.split(field, '=');
            result.put(fieldParts[0], fieldParts[1]);
        }
        return result;
    }

}
