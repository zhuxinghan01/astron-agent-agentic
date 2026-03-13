package com.iflytek.astron.console.hub.validation.point;

import com.iflytek.astron.console.hub.enums.point.SourceType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SourceTypeValidator implements ConstraintValidator<ValidSourceType, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        try {
            SourceType.fromCode(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
