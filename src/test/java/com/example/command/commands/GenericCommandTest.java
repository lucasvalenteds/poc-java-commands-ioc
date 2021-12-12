package com.example.command.commands;

import com.example.command.contract.CommandContext;
import com.example.command.contract.CommandPayload;
import com.example.command.contract.CommandStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GenericCommandTest {

    private final GenericCommandStub command = new GenericCommandStub();

    @Test
    void contextCannotBeNull() {
        final var exception = assertThrows(
            IllegalArgumentException.class,
            () -> command.setContext(null)
        );

        assertEquals("Command context cannot be null", exception.getMessage());
    }

    @Test
    void outputCannotBeNull() {
        final var exception = assertThrows(
            IllegalArgumentException.class,
            () -> command.setOutput(null)
        );

        assertEquals("Command output cannot be null", exception.getMessage());
    }

    @Test
    void processProduceStatusFailedWhenInputIsNull() {
        final var result = command.process(null);

        assertNotNull(result);
        assertEquals(CommandStatus.Failed, result.status());
        assertNull(result.payloadInput());
        assertNotNull(result.context());
        assertEquals(CommandContext.Error.class, result.context().getClass());

        final var context = (CommandContext.Error) result.context();
        assertEquals("Command input cannot be null", context.throwable().getMessage());
        assertEquals(IllegalArgumentException.class, context.throwable().getClass());

        assertEquals(command.getName(), result.name());
        assertEquals(new CommandPayload.Empty(), result.payloadOutput());
        assertNotNull(result.processedAt());
    }

    @Test
    void processProduceStatusReceivedAndMetadata() {
        final var input = new CommandPayload.Empty();

        final var result = command.process(input);

        assertNotNull(result);
        assertEquals(command.getName(), result.name());
        assertEquals(CommandStatus.Received, result.status());
        assertEquals(input, result.payloadInput());
        assertEquals(new CommandPayload.Empty(), result.payloadOutput());
        assertEquals(new CommandContext.Empty(), result.context());
        assertNotNull(result.processedAt());
        assertNotNull(result.id());
    }
}