package com.example.command.exceptions;

import java.io.Serial;
import java.util.UUID;

public final class IncorrectDeviceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -3119366679200287795L;

    private final UUID deviceIdA;
    private final UUID deviceIdB;

    public IncorrectDeviceException(UUID deviceIdA, UUID deviceIdB) {
        super("Command payload targets another device");
        this.deviceIdA = deviceIdA;
        this.deviceIdB = deviceIdB;
    }

    public UUID getDeviceIdA() {
        return deviceIdA;
    }

    public UUID getDeviceIdB() {
        return deviceIdB;
    }
}
