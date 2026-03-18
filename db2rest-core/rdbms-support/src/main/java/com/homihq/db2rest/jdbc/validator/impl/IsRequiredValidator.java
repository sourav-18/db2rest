package com.homihq.db2rest.jdbc.validator.impl;

import com.homihq.db2rest.core.exception.PlaceholderConstraintException;
import com.homihq.db2rest.jdbc.validator.ConstraintValidator;

public class IsRequiredValidator implements ConstraintValidator {

    @Override
    public void validate(Object value, String placeholderName) throws PlaceholderConstraintException {
        if (isMissingValue(value)) {
            throwConstraintViolation(placeholderName, "is required and cannot be null.");
        }
    }

    private boolean isMissingValue(Object value){
        return isNullValue(value) || isEmptyString(value);
    }

    private boolean isNullValue(Object value) {
        return value == null;
    }

    private boolean isEmptyString(Object value) {
        return value instanceof String && ((String) value).trim().isEmpty();
    }
}
