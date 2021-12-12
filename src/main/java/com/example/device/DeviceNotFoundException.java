package com.example.device;

import java.io.Serial;
import java.util.UUID;

public final class DeviceNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2026382339196995404L;

    private final UUID deviceId;

    public DeviceNotFoundException(UUID deviceId, Throwable throwable) {
        super("Device not found", throwable);
        this.deviceId = deviceId;
    }

    public UUID getDeviceId() {
        return deviceId;
    }
}
