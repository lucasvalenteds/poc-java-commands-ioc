package com.example.command;

import com.example.command.contract.CommandResult;
import com.example.command.contract.CommandStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class CommandResultMapper {

    private CommandResultMapper() {
    }

    public static CommandResult.Persisted toPersisted(CommandResult.Processed result) {
        final var persistedAt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
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
        final var executedAt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
        return new CommandResult.Executed(
            result.name(),
            CommandStatus.Delivered,
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