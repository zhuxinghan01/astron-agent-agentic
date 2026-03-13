package com.iflytek.astron.console.hub.validation.point;

import com.iflytek.astron.console.hub.enums.point.RequestType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RequestTypeValidator implements ConstraintValidator<ValidRequestType, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        try {
            RequestType.fromCode(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
