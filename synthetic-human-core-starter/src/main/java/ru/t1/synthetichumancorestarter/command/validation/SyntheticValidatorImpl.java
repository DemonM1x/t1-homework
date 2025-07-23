package ru.t1.synthetichumancorestarter.command.validation;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.t1.synthetichumancorestarter.command.model.SyntheticCommand;

@Component
@RequiredArgsConstructor
public class SyntheticValidatorImpl implements SyntheticValidator {

    private final Validator validator;

    @Override
    public void validate(SyntheticCommand command) {
        final var violations = validator.validate(command);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
