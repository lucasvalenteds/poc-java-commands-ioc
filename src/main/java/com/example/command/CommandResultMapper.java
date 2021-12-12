package com.example.command;

import com.example.command.contract.CommandResult;

import java.time.LocalDateTime;

public final class CommandResultMapper {

    private CommandResultMapper() {
    }

    public static CommandResult.Persisted toPersisted(CommandResult.Processed result) {
        final var persistedAt = LocalDateTime.now();
        return new CommandResult.Persisted(
            result.name(),
            result.status(),
            result.payloadInput(),
            result.payloadOutput(),
            result.context(),
            result.processedAt(),
            result.id(),
            persistedAt
        );
    }

    public static CommandResult.Executed toExecuted(CommandResult.Persisted result) {
        final var executedAt = LocalDateTime.now();
        return new CommandResult.Executed(
            result.name(),
            result.status(),
            result.payloadInput(),
            result.payloadOutput(),
            result.context(),
            result.processedAt(),
            result.id(),
            result.persistedAt(),
            executedAt
        );
    }
}