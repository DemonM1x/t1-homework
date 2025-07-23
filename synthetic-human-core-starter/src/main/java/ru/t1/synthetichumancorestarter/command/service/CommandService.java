package ru.t1.synthetichumancorestarter.command.service;

import ru.t1.synthetichumancorestarter.command.model.SyntheticCommand;
import ru.t1.synthetichumancorestarter.errorhandling.exception.QueueIsFullException;

public interface CommandService {
    void processCommand(SyntheticCommand command) throws QueueIsFullException;
}
