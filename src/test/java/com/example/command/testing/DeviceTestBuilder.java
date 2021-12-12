package com.example.command.testing;

import com.example.device.DeviceRegistered;

import java.util.UUID;

public final class DeviceTestBuilder {

    private DeviceTestBuilder() {
    }

    public static DeviceRegistered createDeviceRegistered(UUID deviceId) {
        final var name = "Lamp";
        final var ip = "192.168.0.123";
        final var logLevel = 1;

        return new DeviceRegistered(deviceId, name, ip, logLevel);
    }
}
