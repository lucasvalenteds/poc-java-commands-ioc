package com.example.device;

import java.util.UUID;

public interface DeviceService {

    DeviceRegistered register(Device device);

    DeviceRegistered findById(UUID deviceId) throws DeviceNotFoundException;
}
