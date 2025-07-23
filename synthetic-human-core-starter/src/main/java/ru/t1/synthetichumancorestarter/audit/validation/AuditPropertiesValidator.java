package ru.t1.synthetichumancorestarter.audit.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.t1.synthetichumancorestarter.audit.properties.AuditConfigurationProperties;
import ru.t1.synthetichumancorestarter.audit.properties.ValidAuditProperties;

public class AuditPropertiesValidator implements
        ConstraintValidator<ValidAuditProperties, AuditConfigurationProperties> {
    @Override
    public boolean isValid(AuditConfigurationProperties props, ConstraintValidatorContext context) {
        if (props == null) {
            return true;
        }

        if (props.getMode() == AuditConfigurationProperties.AuditMode.KAFKA) {
            if (props.getTopic() == null || props.getTopic().trim().isEmpty()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Topic is required when mode is KAFKA")
                        .addPropertyNode("topic").addConstraintViolation();
                return false;
            }
            if (!props.getTopic().matches("^[a-zA-Z0-9._\\-]+$")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Invalid Kafka topic name")
                        .addPropertyNode("topic").addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
