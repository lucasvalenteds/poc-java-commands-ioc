package com.example.command;

import com.example.ServiceResponseError;
import com.example.command.contract.CommandName;
import com.example.command.contract.CommandPayload;
import com.example.command.exceptions.CommandNotFoundException;
import com.example.command.exceptions.CommandNotImplementedException;
import com.example.command.exceptions.CommandPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public final class CommandController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandController.class);

    private final CommandService commandService;

    public CommandController(CommandService commandService) {
        this.commandService = commandService;
    }

    @GetMapping("/commands/{commandId}")
    public ResponseEntity<Object> findCommandById(@PathVariable UUID commandId) {
        try {
            final var command = commandService.findById(commandId);
            LOGGER.info("Command found by ID: {}", command);

            return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(command);
        } catch (CommandNotFoundException exception) {
            LOGGER.warn("Command not found by ID", exception);

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ServiceResponseError(exception.getMessage()));
        }
    }

    @PostMapping("/commands/{commandName}")
    public ResponseEntity<Object> executeCommand(@PathVariable CommandName commandName,
                                                 @RequestBody CommandPayload payload) {
        try {
            final var commandId = commandService.execute(commandName, payload);
            LOGGER.info("Returning command ID {}", commandId);

            return ResponseEntity.status(HttpStatus.ACCEPTED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(commandId);
        } catch (CommandNotImplementedException exception) {
            LOGGER.warn("Tried to execute command not implemented", exception);

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ServiceResponseError(exception.getMessage()));
        } catch (CommandPersistenceException exception) {
            LOGGER.error("Error persisting implemented command with valid input", exception);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ServiceResponseError(exception.getMessage()));
        }
    }
}