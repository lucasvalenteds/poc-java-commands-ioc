package com.example.command;

import com.example.DatabaseConfiguration;
import com.example.ServiceConfiguration;
import com.example.command.commands.CommandsConfiguration;
import com.example.command.contract.CommandContext;
import com.example.command.contract.CommandName;
import com.example.command.contract.CommandPayload;
import com.example.command.contract.CommandResult;
import com.example.command.contract.CommandStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig({
    ServiceConfiguration.class,
    DatabaseConfiguration.class,
    CommandConfiguration.class,
    CommandsConfiguration.class
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommandServiceDefaultTest {

    private static CommandId commandId;

    private CommandService service;

    @BeforeEach
    public void beforeEach(ApplicationContext context) {
        final var factory = context.getBean(CommandFactory.class);
        final var repository = context.getBean(CommandRepository.class);
        this.service = new CommandServiceDefault(factory, repository);
    }

    @Test
    @Order(1)
    void executingCommandImplemented() {
        final var command = CommandName.TurnOn;
        final var payload = new CommandPayload.Empty();

        final var commandId = service.execute(command, payload);

        assertNotNull(commandId);
        assertNotNull(commandId.id());

        CommandServiceDefaultTest.commandId = commandId;
    }

    @Test
    @Order(2)
    void findingCommandById() {
        final var id = commandId.id();

        final var result = service.findById(id);

        assertEquals(CommandName.TurnOn, result.name());
        assertEquals(CommandStatus.Delivered, result.status());
        assertEquals(new CommandPayload.Empty(), result.payloadInput());
        assertEquals(new CommandPayload.Empty(), result.payloadOutput());
        assertEquals(new CommandContext.Empty(), result.context());
        assertEquals(CommandResult.Executed.class, result.getClass());

        final var executed = (CommandResult.Executed) result;
        assertEquals(id, executed.id());
        assertNotNull(executed.processedAt());
        assertNotNull(executed.persistedAt());
        assertNotNull(executed.executedAt());
    }
}