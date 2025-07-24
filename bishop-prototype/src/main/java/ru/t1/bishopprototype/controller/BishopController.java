package ru.t1.bishopprototype.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.bishopprototype.model.CommandRequest;
import ru.t1.bishopprototype.service.BishopPrototypeService;
import ru.t1.synthetichumancorestarter.errorhandling.exception.QueueIsFullException;

@RestController
@RequestMapping("api/v1/command")
@RequiredArgsConstructor
public class BishopController {

    private final BishopPrototypeService bishopPrototypeService;

    @PostMapping
    public void processCommand(@RequestBody CommandRequest commandRequest)
            throws IllegalArgumentException, QueueIsFullException {
        bishopPrototypeService.runCommand(commandRequest);
    }
}
