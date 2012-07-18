/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.vendor.usps.service.message;

import org.broadleafcommerce.common.util.WeightUnitOfMeasureType;
import org.broadleafcommerce.common.vendor.service.exception.FulfillmentPriceException;
import org.broadleafcommerce.vendor.usps.service.type.USPSShippingPriceErrorCode;

public class USPSRequestValidator {

    private final USPSVersionedRequestValidator versionedValidator;

    public USPSRequestValidator(USPSVersionedRequestValidator versionedValidator) {
        this.versionedValidator = versionedValidator;
    }

    public void validateRequest(USPSShippingPriceRequest request) throws FulfillmentPriceException {
        validatePackageQuantity(request);
        for (USPSContainerItemRequest itemRequest : request.getContainerItems()) {
            validateService(itemRequest);
            validateWeight(itemRequest);
            validateZip(itemRequest);
            validateSize(itemRequest);
            validateContainer(itemRequest);
            validateMachinable(itemRequest);
            validateDimensions(itemRequest);
            validateGirth(itemRequest);
            validateShipDate(itemRequest);
            validateOther(itemRequest);
        }
    }

    protected void validateOther(USPSContainerItemRequest itemRequest) throws FulfillmentPriceException {
        if (itemRequest.getPackageId() == null) {
            throw buildException(USPSShippingPriceErrorCode.PACKAGEIDNOTSPECIFIED.getType(), USPSShippingPriceErrorCode.PACKAGEIDNOTSPECIFIED.getMessage());
        }
        versionedValidator.validateOther(itemRequest);
    }

    protected void validateShipDate(USPSContainerItemRequest itemRequest) throws FulfillmentPriceException {
        versionedValidator.validateShipDate(itemRequest);
    }

    protected void validateGirth(USPSContainerItemRequest itemRequest) throws FulfillmentPriceException {
        versionedValidator.validateGirth(itemRequest);
    }

    protected void validateDimensions(USPSContainerItemRequest itemRequest) throws FulfillmentPriceException {
        versionedValidator.validateDimensions(itemRequest);
    }

    protected void validateMachinable(USPSContainerItemRequest itemRequest) throws FulfillmentPriceException {
        versionedValidator.validateMachinable(itemRequest);
    }

    protected void validateSize(USPSContainerItemRequest itemRequest) throws FulfillmentPriceException {
        versionedValidator.validateSize(itemRequest);
    }

    protected void validateContainer(USPSContainerItemRequest itemRequest) throws FulfillmentPriceException {
        versionedValidator.validateContainer(itemRequest);
    }

    protected void validateZip(USPSContainerItemRequest itemRequest) throws FulfillmentPriceException {
        if (itemRequest.getZipDestination() == null || itemRequest.getZipOrigination() == null) {
            throw buildException(USPSShippingPriceErrorCode.ZIPNOTSPECIFIED.getType(), USPSShippingPriceErrorCode.ZIPNOTSPECIFIED.getMessage());
        }
        if (itemRequest.getZipDestination().length() != 5 || itemRequest.getZipOrigination().length() != 5) {
            throw buildException(USPSShippingPriceErrorCode.ZIPLENGTH.getType(), USPSShippingPriceErrorCode.ZIPLENGTH.getMessage());
        }
    }

    protected void validateService(USPSContainerItemRequest itemRequest) throws FulfillmentPriceException {
        if (itemRequest.getService() == null) {
            throw USPSRequestValidator.buildException(USPSShippingPriceErrorCode.SERVICENOTSPECIFIED.getType(), USPSShippingPriceErrorCode.SERVICENOTSPECIFIED.getMessage());
        }
        versionedValidator.validateService(itemRequest);
    }

    protected void validateWeight(USPSContainerItemRequest itemRequest) throws FulfillmentPriceException {
        if (itemRequest.getWeight() == null) {
            throw buildException(USPSShippingPriceErrorCode.WEIGHTNOTSPECIFIED.getType(), USPSShippingPriceErrorCode.WEIGHTNOTSPECIFIED.getMessage());
        }
        if (itemRequest.getWeightUnitOfMeasureType() == null) {
            throw buildException(USPSShippingPriceErrorCode.UNITTYPENOTSPECIFIED.getType(), USPSShippingPriceErrorCode.UNITTYPENOTSPECIFIED.getMessage());
        }
        if (!itemRequest.getWeightUnitOfMeasureType().equals(WeightUnitOfMeasureType.KILOGRAMS) && !itemRequest.getWeightUnitOfMeasureType().equals(WeightUnitOfMeasureType.POUNDS)) {
            throw buildException(USPSShippingPriceErrorCode.UNITTYPENOTSUPPORTED.getType(), USPSShippingPriceErrorCode.UNITTYPENOTSUPPORTED.getMessage());
        }
        versionedValidator.validateWeight(itemRequest);
    }

    protected void validatePackageQuantity(USPSShippingPriceRequest request) throws FulfillmentPriceException {
        if (request.getContainerItems().size() > 25) {
            throw buildException(USPSShippingPriceErrorCode.TOOMANYCONTAINERITEMS.getType(), USPSShippingPriceErrorCode.TOOMANYCONTAINERITEMS.getMessage());
        }
    }

    public static FulfillmentPriceException buildException(String errorCode, String errorText) {
        USPSShippingPriceResponse response = new USPSShippingPriceResponse();
        response.setErrorDetected(true);
        response.setErrorCode(errorCode);
        response.setErrorText(errorText);
        FulfillmentPriceException e = new FulfillmentPriceException();
        e.setFulfillmentPriceExceptionResponse(response);

        return e;
    }
}
