package ru.t1.bishopprototype.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.t1.bishopprototype.model.CommandAuthor;
import ru.t1.bishopprototype.model.CommandRequest;
import ru.t1.bishopprototype.model.CommandType;
import ru.t1.synthetichumancorestarter.audit.aspect.WeylandWatchingYou;
import ru.t1.synthetichumancorestarter.command.model.CommandPriority;
import ru.t1.synthetichumancorestarter.command.model.SyntheticCommand;
import ru.t1.synthetichumancorestarter.command.service.CommandService;
import ru.t1.synthetichumancorestarter.errorhandling.exception.QueueIsFullException;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BishopPrototypeService {

    private final CommandService commandService;

    @WeylandWatchingYou
    public void runCommand(CommandRequest commandRequest) throws IllegalArgumentException, QueueIsFullException {
        final var syntheticCommandBuilder = SyntheticCommand.builder();
        syntheticCommandBuilder.description(handleDescription(commandRequest.getCommandType(), commandRequest.getParams()));
        syntheticCommandBuilder.priority(handlePriority(commandRequest.getAuthor()));
        syntheticCommandBuilder.author(commandRequest.getAuthor().toString());
        syntheticCommandBuilder.time(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
        commandService.processCommand(syntheticCommandBuilder.build());
    }

    private String handleDescription(CommandType commandType, Map<String, Object> params) {
        return switch (commandType) {
            case MOVE -> handleMove(params);
            case TURN -> handleTurn(params);
            case SPEAK -> handleSpeak(params);
            case SHUTDOWN -> "SHUTTING DOWN...";
        };
    }

    private CommandPriority handlePriority(CommandAuthor author) {
        return switch (author) {
            case COMMON_USER -> CommandPriority.COMMON;
            case WEYLAND_ADMIN -> CommandPriority.CRITICAL;
        };
    }

    private String handleMove(Map<String, Object> params) {
        String direction = (String) params.get("direction");
        Integer distance = (Integer) params.get("distance");
        if (distance == null || distance < 0 || direction == null) throw new IllegalArgumentException();
        return "Move " + direction + " to " + distance + " meters";
    }

    private String handleTurn(Map<String, Object> params) {
        Integer angle = (Integer) params.get("angle");
        if (angle == null || Math.abs(angle) > 360) throw new IllegalArgumentException();
        return "Turn by " + angle + " degrees";
    }

    private String handleSpeak(Map<String, Object> params) {
        String message = (String) params.get("message");
        if (message == null || message.isEmpty()) throw new IllegalArgumentException();
        return "Speak: " + message;
    }
}
