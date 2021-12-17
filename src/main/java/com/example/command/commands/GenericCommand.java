package com.example.command.commands;

import com.example.command.contract.Command;
import com.example.command.contract.CommandContext;
import com.example.command.contract.CommandPayload;
import com.example.command.contract.CommandResult;
import com.example.command.contract.CommandStatus;
import com.example.command.exceptions.InvalidCommandPayloadException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

abstract class GenericCommand<I extends CommandPayload, O extends CommandPayload> implements Command<I> {

    private CommandContext context = new CommandContext.Empty();
    private CommandPayload output = new CommandPayload.Empty();

    private void validateInput(I input) {
        if (input == null) {
            throw new IllegalArgumentException("Command input cannot be null");
        }
    }

    protected final CommandContext getContext() {
        return this.context;
    }

    protected final void setContext(CommandContext context) throws IllegalArgumentException {
        if (context == null) {
            throw new IllegalArgumentException("Command context cannot be null");
        }

        this.context = context;
    }

    protected final void setOutput(O output) throws IllegalArgumentException {
        if (output == null) {
            throw new IllegalArgumentException("Command output cannot be null");
        }

        this.output = output;
    }

    protected void processInput(I input) throws InvalidCommandPayloadException {
        // No-op
    }

    protected void processOutput(I input) {
        // No-op
    }

    @Override
    public final CommandResult.Processed process(I input) {
        final var processedAt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
        final var commandId = UUID.randomUUID();

        try {
            this.validateInput(input);
            this.processInput(input);
            this.processOutput(input);
            return new CommandResult.Processed(
                getName(),
                CommandStatus.Received,
                input,
                output,
                context,
                processedAt,
                commandId
            );
        } catch (RuntimeException exception) {
            final var errorContext = new CommandContext.Error(exception);
            return new CommandResult.Processed(
                getName(),
                CommandStatus.Failed,
                input,
                output,
                errorContext,
                processedAt,
                commandId
            );
        }
    }
}
