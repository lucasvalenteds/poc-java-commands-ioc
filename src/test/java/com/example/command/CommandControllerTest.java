package com.example.command;

import com.example.DatabaseConfiguration;
import com.example.ServiceConfiguration;
import com.example.ServiceResponseError;
import com.example.command.commands.CommandsConfiguration;
import com.example.command.commands.SetLogLevel;
import com.example.command.contract.CommandContext;
import com.example.command.contract.CommandName;
import com.example.command.contract.CommandPayload;
import com.example.command.contract.CommandResult;
import com.example.command.contract.CommandStatus;
import com.example.command.exceptions.CommandPersistenceException;
import com.example.command.testing.CommandTestConfiguration;
import com.example.command.testing.DeviceTestBuilder;
import com.example.command.testing.JsonProcessingExceptionStub;
import com.example.device.DeviceRegistered;
import com.example.device.DeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig({
    ServiceConfiguration.class,
    DatabaseConfiguration.class,
    CommandTestConfiguration.class,
    CommandConfiguration.class,
    CommandsConfiguration.class
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommandControllerTest {

    private static final UUID DEVICE_ID = UUID.randomUUID();
    private static final DeviceRegistered DEVICE_REGISTERED = DeviceTestBuilder.createDeviceRegistered(DEVICE_ID);

    private static final CommandName COMMAND_NAME = CommandName.SET_LOG_LEVEL;
    private static final CommandPayload COMMAND_PAYLOAD = new SetLogLevel.PayloadInput(DEVICE_REGISTERED.id(), 5);

    private static CommandId commandId;

    private WebTestClient webTestClient;
    private CommandService commandService;
    private DeviceService deviceService;

    @BeforeEach
    public void beforeEach(ApplicationContext context) {
        this.deviceService = context.getBean(DeviceService.class);
        this.commandService = context.getBean(CommandService.class);

        final var commandController = new CommandController(commandService);
        this.webTestClient = WebTestClient.bindToController(commandController)
            .configureClient()
            .build();
    }

    @Test
    @Order(1)
    void testExecutingCommand() {
        Mockito.when(deviceService.findById(DEVICE_ID))
            .thenReturn(DEVICE_REGISTERED);

        final var commandId = webTestClient.post()
            .uri("/commands/{commandName}", COMMAND_NAME.getPublicName())
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(COMMAND_PAYLOAD))
            .exchange()
            .expectStatus().isAccepted()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(CommandId.class)
            .returnResult()
            .getResponseBody();

        assertNotNull(commandId);
        assertNotNull(commandId.id());

        CommandControllerTest.commandId = commandId;
    }

    @Test
    @Order(2)
    void testFindingCommandExecuted() {
        final var id = commandId.id();

        final var result = webTestClient.get()
            .uri("/commands/{commandId}", id)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(CommandResult.Executed.class)
            .returnResult()
            .getResponseBody();

        assertNotNull(result);
        assertEquals(id, result.id());
        assertEquals(COMMAND_NAME, result.name());
        assertEquals(CommandStatus.DELIVERED, result.status());
        assertEquals(COMMAND_PAYLOAD, result.payloadInput());
    }

    @Test
    void testCommandNotFoundById() {
        final var id = UUID.randomUUID();

        webTestClient.get()
            .uri("/commands/{commandId}", id)
            .exchange()
            .expectStatus().isNotFound()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(ServiceResponseError.class)
            .isEqualTo(new ServiceResponseError("Command not found"));
    }

    @Test
    void testCommandNotImplemented() {
        final var commandName = CommandName.NO_OP.getPublicName();

        webTestClient.post()
            .uri("/commands/{commandName}", commandName)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(COMMAND_PAYLOAD))
            .exchange()
            .expectStatus().isNotFound()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(ServiceResponseError.class)
            .isEqualTo(new ServiceResponseError("Command not implemented yet: " + commandName));
    }

    @Test
    void testCommandNeitherImplementedNorMapped() {
        final var commandName = "TurnOff";

        webTestClient.post()
            .uri("/commands/{commandName}", commandName)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(COMMAND_PAYLOAD))
            .exchange()
            .expectStatus().isNotFound()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(ServiceResponseError.class)
            .isEqualTo(new ServiceResponseError("Command not implemented yet: " + commandName));
    }

    @Test
    void testCommandNotFound() {
        final var id = UUID.randomUUID();

        final var result = webTestClient.get()
            .uri("/commands/{commandId}", id)
            .exchange()
            .expectStatus().isNotFound()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(ServiceResponseError.class)
            .returnResult()
            .getResponseBody();

        assertNotNull(result);
        assertEquals("Command not found", result.message());
    }

    @Test
    void testInvalidPayload() {
        final var commandName = CommandName.SET_LOG_LEVEL.getPublicName();
        final var payload = new SetLogLevel.PayloadInput(UUID.randomUUID(), 10);

        final var commandId = webTestClient.post()
            .uri("/commands/{commandName}", commandName)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(payload))
            .exchange()
            .expectStatus().isAccepted()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(CommandId.class)
            .returnResult()
            .getResponseBody();

        assertNotNull(commandId);
        assertNotNull(commandId.id());
        final var result = commandService.findById(commandId.id());
        final var context = ((CommandContext.Error) result.context());
        assertEquals("Invalid command payload attribute: level", context.throwable().getMessage());
    }

    @Test
    void testDatabasePersistenceSqlError() {
        // Arrange
        final var commandService = Mockito.mock(CommandService.class);
        final var commandController = new CommandController(commandService);
        final var webTestClient = WebTestClient.bindToController(commandController)
            .configureClient()
            .build();

        final var commandName = CommandName.SET_LOG_LEVEL;
        final var payload = new SetLogLevel.PayloadInput(UUID.randomUUID(), 10);

        Mockito.when(commandService.execute(commandName, payload))
            .thenThrow(new CommandPersistenceException(new SQLException()));

        // Act
        final var error = webTestClient.post()
            .uri("/commands/{commandName}", commandName.getPublicName())
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(payload))
            .exchange()
            .expectStatus().is5xxServerError()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(ServiceResponseError.class)
            .returnResult()
            .getResponseBody();

        // Assert
        assertNotNull(error);
        assertEquals("Could not persist command", error.message());
    }

    @Test
    void testDatabasePersistenceJsonError() {
        // Arrange
        final var commandService = Mockito.mock(CommandService.class);
        final var commandController = new CommandController(commandService);
        final var webTestClient = WebTestClient.bindToController(commandController)
            .configureClient()
            .build();

        final var commandName = CommandName.SET_LOG_LEVEL;
        final var payload = new SetLogLevel.PayloadInput(UUID.randomUUID(), 10);

        Mockito.when(commandService.execute(commandName, payload))
            .thenThrow(new CommandPersistenceException(new JsonProcessingExceptionStub()));

        // Act
        final var error = webTestClient.post()
            .uri("/commands/{commandName}", commandName.getPublicName())
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(payload))
            .exchange()
            .expectStatus().is5xxServerError()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(ServiceResponseError.class)
            .returnResult()
            .getResponseBody();

        // Assert
        assertNotNull(error);
        assertEquals("Could not serialize/deserialize command", error.message());
    }
}