package ru.t1.synthetichumancorestarter.command.executor;

import ru.t1.synthetichumancorestarter.command.model.SyntheticCommand;

public interface SyntheticCommandExecutor {
    public void execute(SyntheticCommand command);
}
