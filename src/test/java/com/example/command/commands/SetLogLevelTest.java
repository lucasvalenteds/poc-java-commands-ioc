package com.example.command.commands;

import com.example.command.contract.CommandContext;
import com.example.command.contract.CommandName;
import com.example.command.contract.CommandPayload;
import com.example.command.contract.CommandStatus;
import com.example.command.exceptions.InvalidCommandPayloadException;
import com.example.command.testing.DeviceTestBuilder;
import com.example.device.DeviceRegistered;
import com.example.device.DeviceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SetLogLevelTest {

    private final DeviceService deviceService = Mockito.mock(DeviceService.class);
    private final SetLogLevel command = new SetLogLevel(deviceService);

    @ParameterizedTest
    @ValueSource(ints = {0, 7})
    void throwsWhenLogLevelIsLowerThanOneOrGreaterThanSix(int level) {
        final var deviceId = UUID.randomUUID();
        final var payload = new SetLogLevel.PayloadInput(deviceId, level);

        final var exception = assertThrows(
            InvalidCommandPayloadException.class,
            () -> command.processInput(payload)
        );

        assertEquals("Invalid command payload attribute", exception.getMessage());
        assertEquals("Invalid command payload attribute: level", exception.toString());
        assertEquals("level", exception.getProperty());
        assertEquals(payload.level(), exception.getValue());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6})
    void allowsLogLevelBetweenOneAndSix(int level) {
        final var deviceId = UUID.randomUUID();
        final var payload = new SetLogLevel.PayloadInput(deviceId, level);

        assertDoesNotThrow(() -> command.processInput(payload));
    }

    @Test
    void testProcessSucceeding() {
        final var deviceId = UUID.randomUUID();
        final var level = 1;
        final var payload = new SetLogLevel.PayloadInput(deviceId, level);
        final var deviceRegistered = DeviceTestBuilder.createDeviceRegistered(deviceId);
        this.mockDeviceServiceToSucceed(deviceRegistered);

        final var result = command.process(payload);

        assertEquals(CommandName.SetLogLevel, result.name());
        assertEquals(CommandStatus.Received, result.status());
        assertEquals(payload, result.payloadInput());
        assertEquals(new SetLogLevel.PayloadContext(deviceRegistered), result.context());
        assertEquals(new SetLogLevel.PayloadOutput("Device Lamp log level changed to 1"), result.payloadOutput());
        assertNotNull(result.processedAt());
        assertNotNull(result.id());
    }

    @Test
    void testProcessFailing() {
        final var deviceId = UUID.randomUUID();
        final var level = 1;
        final var payload = new SetLogLevel.PayloadInput(deviceId, level);
        final var deviceRegistered = DeviceTestBuilder.createDeviceRegistered(deviceId);
        this.mockDeviceServiceToFail(deviceRegistered);

        final var result = command.process(payload);

        assertEquals(CommandName.SetLogLevel, result.name());
        assertEquals(CommandStatus.Failed, result.status());
        assertEquals(payload, result.payloadInput());
        assertEquals(new CommandPayload.Empty(), result.payloadOutput());
        assertAll(
            () -> assertEquals(CommandContext.Error.class, result.context().getClass()),
            () -> assertEquals("Device not found", ((CommandContext.Error) result.context()).throwable().getMessage())
        );
        assertNotNull(result.processedAt());
        assertNotNull(result.id());
    }

    private void mockDeviceServiceToSucceed(DeviceRegistered deviceRegistered) {
        Mockito.when(deviceService.findById(deviceRegistered.id()))
            .thenReturn(deviceRegistered);
    }

    private void mockDeviceServiceToFail(DeviceRegistered deviceRegistered) {
        Mockito.when(deviceService.findById(deviceRegistered.id()))
            .thenThrow(new IllegalArgumentException("Device not found"));
    }
}