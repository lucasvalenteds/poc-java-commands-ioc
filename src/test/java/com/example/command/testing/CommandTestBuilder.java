package com.example.command.testing;

import com.example.command.commands.SetLogLevel;
import com.example.command.contract.CommandName;
import com.example.command.contract.CommandResult;
import com.example.command.contract.CommandStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public final class CommandTestBuilder {

    private CommandTestBuilder() {

    }

    public static CommandResult.Processed createCommandProcessed(UUID commandId) {
        final var name = CommandName.SetLogLevel;
        final var status = CommandStatus.RECEIVED;

        final var deviceId = UUID.fromString("8d3d0e4f-716c-4fc9-8c21-79c0f34d7370");
        final var level = 5;
        final var payloadInput = new SetLogLevel.PayloadInput(deviceId, level);

        final var context = new SetLogLevel.PayloadContext(DeviceTestBuilder.createDeviceRegistered(deviceId));
        final var payloadOutput = new SetLogLevel.PayloadOutput("Device log level updated");
        final var processedAt = LocalDateTime.of(2021, 12, 12, 10, 10, 10);

        return new CommandResult.Processed(name, status, payloadInput, payloadOutput, context, processedAt, commandId);
    }
}