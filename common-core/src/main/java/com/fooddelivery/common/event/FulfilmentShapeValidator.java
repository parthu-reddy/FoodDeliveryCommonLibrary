package com.fooddelivery.common.event;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Map;

/**
 * DELIVERY: every {@link FulfilmentScopedEvent#deliveryScope()} field present ("x is required", the message the field
 * constraints gave before) and no table. DINE_IN: a table label and no delivery field. A missing fulfilmentType is left
 * to the field's own {@code @NotNull}.
 */
public class FulfilmentShapeValidator implements ConstraintValidator<FulfilmentShape, FulfilmentScopedEvent> {

    @Override
    public boolean isValid(FulfilmentScopedEvent event, ConstraintValidatorContext context) {
        if (event == null || event.getFulfilmentType() == null) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        return switch (event.getFulfilmentType()) {
            case DELIVERY -> delivery(event, context);
            case DINE_IN -> dineIn(event, context);
        };
    }

    private static boolean delivery(FulfilmentScopedEvent event, ConstraintValidatorContext context) {
        boolean ok = true;
        for (Map.Entry<String, Object> field : event.deliveryScope().entrySet()) {
            if (absent(field.getValue())) {
                ok = violation(context, field.getKey(), field.getKey() + " is required");
            }
        }
        if (event.getTableLabel() != null) {
            ok = violation(context, "tableLabel", "tableLabel must be absent for DELIVERY");
        }
        return ok;
    }

    private static boolean dineIn(FulfilmentScopedEvent event, ConstraintValidatorContext context) {
        boolean ok = true;
        if (absent(event.getTableLabel())) {
            ok = violation(context, "tableLabel", "tableLabel is required for DINE_IN");
        }
        for (Map.Entry<String, Object> field : event.deliveryScope().entrySet()) {
            if (field.getValue() != null) {
                ok = violation(context, field.getKey(), field.getKey() + " must be absent for DINE_IN");
            }
        }
        return ok;
    }

    private static boolean absent(Object value) {
        return value == null || (value instanceof String s && s.isBlank());
    }

    private static boolean violation(ConstraintValidatorContext context, String field, String message) {
        context.buildConstraintViolationWithTemplate(message).addPropertyNode(field).addConstraintViolation();
        return false;
    }
}
