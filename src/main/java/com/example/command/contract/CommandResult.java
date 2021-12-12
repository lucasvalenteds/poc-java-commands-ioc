package com.example.command.contract;

import java.time.LocalDateTime;
import java.util.UUID;

public sealed interface CommandResult permits CommandResult.Processed, CommandResult.Persisted, CommandResult.Executed {

    final record Processed(
        CommandName name,
        CommandStatus status,
        CommandPayload payloadInput,
        CommandPayload payloadOutput,
        CommandContext context,
        LocalDateTime processedAt,
        UUID id
    ) implements CommandResult {
    }

    final record Persisted(
        CommandName name,
        CommandStatus status,
        CommandPayload payloadInput,
        CommandPayload payloadOutput,
        CommandContext context,
        LocalDateTime processedAt,
        UUID id,
        LocalDateTime persistedAt
    ) implements CommandResult {
    }

    final record Executed(
        CommandName name,
        CommandStatus status,
        CommandPayload payloadInput,
        CommandPayload payloadOutput,
        CommandContext context,
        LocalDateTime processedAt,
        UUID id,
        LocalDateTime persistedAt,
        LocalDateTime executedAt
    ) implements CommandResult {
    }

    CommandName name();

    CommandStatus status();

    CommandPayload payloadInput();

    CommandPayload payloadOutput();

    CommandContext context();
}
