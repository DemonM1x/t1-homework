package ru.t1.synthetichumancorestarter.audit.properties;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.t1.synthetichumancorestarter.audit.validation.AuditPropertiesValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AuditPropertiesValidator.class)
public @interface ValidAuditProperties {
    String message() default "Invalid audit properties";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
