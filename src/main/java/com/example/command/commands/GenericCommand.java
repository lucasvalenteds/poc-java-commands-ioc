package com.example.command.commands;

import com.example.command.contract.Command;
import com.example.command.contract.CommandContext;
import com.example.command.contract.CommandPayload;
import com.example.command.contract.CommandResult;
import com.example.command.contract.CommandStatus;
import com.example.command.exceptions.InvalidCommandPayloadException;

import java.time.LocalDateTime;
import java.util.UUID;

abstract class GenericCommand<INPUT extends CommandPayload, OUTPUT extends CommandPayload> implements Command<INPUT> {

    private CommandContext context = new CommandContext.Empty();
    private CommandPayload output = new CommandPayload.Empty();

    private void validateInput(INPUT input) {
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

    protected final void setOutput(OUTPUT output) throws IllegalArgumentException {
        if (output == null) {
            throw new IllegalArgumentException("Command output cannot be null");
        }

        this.output = output;
    }

    protected void processInput(INPUT input) throws InvalidCommandPayloadException {
        // No-op
    }

    protected void processOutput(INPUT input) {
        // No-op
    }

    @Override
    public final CommandResult.Processed process(INPUT input) {
        final var processedAt = LocalDateTime.now();
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
