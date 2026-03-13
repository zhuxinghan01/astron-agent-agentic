package com.iflytek.astron.console.hub.validation.point;

import com.iflytek.astron.console.hub.enums.point.ResourceType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ResourceTypeValidator implements ConstraintValidator<ValidResourceType, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        try {
            ResourceType.fromCode(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
