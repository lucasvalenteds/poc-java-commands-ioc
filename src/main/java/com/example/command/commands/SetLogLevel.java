package com.example.command.commands;

import com.example.command.contract.CommandContext;
import com.example.command.contract.CommandName;
import com.example.command.contract.CommandPayload;
import com.example.command.exceptions.InvalidCommandPayloadException;
import com.example.device.DeviceNotFoundException;
import com.example.device.DeviceRegistered;
import com.example.device.DeviceService;

import java.util.UUID;

public final class SetLogLevel extends GenericCommand<SetLogLevel.PayloadInput, SetLogLevel.PayloadOutput> {

    public final record PayloadInput(UUID deviceId, int level) implements CommandPayload {
    }

    public final record PayloadContext(DeviceRegistered deviceRegistered) implements CommandContext {
    }

    public final record PayloadOutput(String message) implements CommandPayload {
    }

    private final DeviceService deviceService;

    public SetLogLevel(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public CommandName getName() {
        return CommandName.SetLogLevel;
    }

    @Override
    protected void processInput(PayloadInput input) throws InvalidCommandPayloadException, DeviceNotFoundException {
        if (input.level() < 1 || input.level() > 6) {
            throw new InvalidCommandPayloadException("level", input.level());
        }

        final var deviceRegistered = deviceService.findById(input.deviceId());

        super.setContext(new SetLogLevel.PayloadContext(deviceRegistered));
    }

    @Override
    protected void processOutput(SetLogLevel.PayloadInput input) {
        final var context = (SetLogLevel.PayloadContext) super.getContext();

        final var deviceRegistered = context.deviceRegistered();
        final var message = "Device " + deviceRegistered.name() + " log level changed to " + input.level();

        super.setOutput(new PayloadOutput(message));
    }
}
