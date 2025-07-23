package ru.t1.synthetichumancorestarter.command.validation;

import ru.t1.synthetichumancorestarter.command.model.SyntheticCommand;

public interface SyntheticValidator {
    void validate(SyntheticCommand command);
}
