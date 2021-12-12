package com.example.device;

import java.util.UUID;

public interface DeviceService {

    DeviceRegistered findById(UUID deviceId) throws DeviceNotFoundException;
}
