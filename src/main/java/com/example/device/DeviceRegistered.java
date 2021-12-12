package com.example.device;

import java.util.UUID;

public final record DeviceRegistered(UUID id, String name, String ip, int logLevel) {
}
