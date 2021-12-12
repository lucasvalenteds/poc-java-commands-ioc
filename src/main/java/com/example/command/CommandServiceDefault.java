package com.example.command;

import com.example.command.contract.CommandName;
import com.example.command.contract.CommandPayload;
import com.example.command.contract.CommandResult;
import com.example.command.exceptions.CommandNotFoundException;
import com.example.command.exceptions.CommandNotImplementedException;
import com.example.command.exceptions.CommandPersistenceException;
import com.example.command.exceptions.InvalidCommandPayloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public final class CommandServiceDefault implements CommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandServiceDefault.class);

    private final CommandFactory factory;
    private final CommandRepository repository;

    public CommandServiceDefault(CommandFactory factory, CommandRepository repository) {
        this.factory = factory;
        this.repository = repository;
    }

    public CommandResult findById(UUID id) throws CommandNotFoundException {
        return repository.findById(id);
    }

    public CommandId execute(CommandName name, CommandPayload payload)
        throws CommandNotImplementedException, InvalidCommandPayloadException, CommandPersistenceException {
        final var command = factory.create(name);

        final var commandProcessed = command.process(payload);
        repository.save(commandProcessed);
        LOGGER.info("Command processed: {}", commandProcessed);

        final var commandPersisted = CommandResultMapper.toPersisted(commandProcessed);
        repository.save(commandPersisted);
        LOGGER.info("Command persisted: {}", commandPersisted);

        final var commandExecuted = CommandResultMapper.toExecuted(commandPersisted);
        repository.save(commandExecuted);
        LOGGER.info("Command executed: {}", commandExecuted);

        return new CommandId(commandExecuted.id());
    }
}
