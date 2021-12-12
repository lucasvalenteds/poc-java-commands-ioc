package com.example.command;

import com.example.DatabaseConfiguration;
import com.example.ServiceConfiguration;
import com.example.command.contract.CommandResult;
import com.example.command.exceptions.CommandNotFoundException;
import com.example.command.testing.CommandTestBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig({ServiceConfiguration.class, DatabaseConfiguration.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommandRepositoryDefaultTest {

    private final static UUID COMMAND_ID = UUID.randomUUID();

    private static CommandResult.Processed processed;
    private static CommandResult.Persisted persisted;

    private CommandRepository repository;

    @BeforeEach
    public void beforeEach(ApplicationContext context) {
        final var jdbcTemplate = context.getBean(JdbcTemplate.class);
        final var objectMapper = context.getBean(ObjectMapper.class);
        this.repository = new CommandRepositoryDefault(jdbcTemplate, objectMapper);
    }

    @Test
    void findByIdThrowsWhenResultSetIsEmpty() {
        final var commandId = UUID.randomUUID();

        final var exception = assertThrows(
            CommandNotFoundException.class,
            () -> repository.findById(commandId)
        );

        assertEquals("Command not found", exception.getMessage());
        assertEquals(commandId, exception.getId());
        assertNotNull(exception.getCause());
        assertEquals(EmptyResultDataAccessException.class, exception.getCause().getClass());
    }

    @Test
    @Order(1)
    void savingCommandResultProcessed() {
        final CommandResult.Processed result = CommandTestBuilder.createCommandProcessed(COMMAND_ID);

        repository.save(result);

        final var resultFound = repository.findById(result.id());
        assertEquals(result, resultFound);
        assertEquals(CommandResult.Processed.class, resultFound.getClass());

        CommandRepositoryDefaultTest.processed = (CommandResult.Processed) resultFound;
    }

    @Test
    @Order(2)
    void savingCommandResultPersisted() {
        final CommandResult.Persisted result = CommandResultMapper.toPersisted(CommandRepositoryDefaultTest.processed);

        repository.save(result);
        final var resultFound = repository.findById(result.id());

        assertEquals(result, resultFound);
        assertEquals(CommandResult.Persisted.class, resultFound.getClass());

        CommandRepositoryDefaultTest.persisted = (CommandResult.Persisted) resultFound;
    }

    @Test
    @Order(3)
    void savingCommandResultExecuted() {
        final CommandResult.Executed result = CommandResultMapper.toExecuted(CommandRepositoryDefaultTest.persisted);

        repository.save(result);
        final var resultFound = repository.findById(result.id());

        assertEquals(result, resultFound);
        assertEquals(CommandResult.Executed.class, resultFound.getClass());
    }
}