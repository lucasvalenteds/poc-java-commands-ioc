package com.example.command;

import com.example.command.contract.CommandContext;
import com.example.command.contract.CommandName;
import com.example.command.contract.CommandPayload;
import com.example.command.contract.CommandResult;
import com.example.command.contract.CommandStatus;
import com.example.command.exceptions.CommandNotFoundException;
import com.example.command.exceptions.CommandPersistenceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public final class CommandRepositoryDefault implements CommandRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public CommandRepositoryDefault(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void save(CommandResult.Processed result) throws DataAccessException {
        final var query = """
            insert into COMMANDS
             (NAME, STATUS, PAYLOAD_INPUT, PAYLOAD_OUTPUT, CONTEXT, PROCESSED_AT, ID)
             values (?, ?, ?, ?, ?, ?, ?)
            """;
        final var arguments = new Object[]{
            result.name().name(),
            result.status().name(),
            CommandRepositoryDefault.toJson(objectMapper, result.payloadInput()),
            CommandRepositoryDefault.toJson(objectMapper, result.payloadOutput()),
            CommandRepositoryDefault.toJson(objectMapper, result.context()),
            result.processedAt(),
            result.id()
        };
        final var types = new int[]{
            Types.VARCHAR,
            Types.VARCHAR,
            Types.VARCHAR,
            Types.VARCHAR,
            Types.VARCHAR,
            Types.TIMESTAMP_WITH_TIMEZONE,
            Types.VARCHAR
        };
        jdbcTemplate.update(query, arguments, types);
    }

    @Transactional
    public void save(CommandResult.Persisted result) throws DataAccessException {
        final var query = "update COMMANDS set STATUS = ?, PERSISTED_AT = ? WHERE ID = ?";
        final var arguments = new Object[]{result.status().name(), result.persistedAt(), result.id()};
        final var types = new int[]{Types.VARCHAR, Types.TIMESTAMP_WITH_TIMEZONE, Types.VARCHAR};
        jdbcTemplate.update(query, arguments, types);
    }

    @Transactional
    public void save(CommandResult.Executed result) throws DataAccessException {
        final var query = "update COMMANDS set STATUS = ?, EXECUTED_AT = ? WHERE ID = ?";
        final var arguments = new Object[]{result.status().name(), result.executedAt(), result.id()};
        final var types = new int[]{Types.VARCHAR, Types.TIMESTAMP_WITH_TIMEZONE, Types.VARCHAR};
        jdbcTemplate.update(query, arguments, types);
    }

    public CommandResult findById(UUID id) throws CommandNotFoundException {
        final var query = """
            select NAME, STATUS, PAYLOAD_INPUT, PAYLOAD_OUTPUT, CONTEXT, PROCESSED_AT, ID, PERSISTED_AT, EXECUTED_AT
             from COMMANDS
             where ID = ?
            """;
        final var arguments = new Object[]{id};
        final var types = new int[]{Types.VARCHAR};

        try {
            return jdbcTemplate.queryForObject(query, arguments, types, this::mapRow);
        } catch (EmptyResultDataAccessException exception) {
            throw new CommandNotFoundException(id, exception);
        }
    }

    private static String toJson(ObjectMapper objectMapper, Object object) throws CommandPersistenceException {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException exception) {
            throw new CommandPersistenceException(exception);
        }
    }

    private <T> T readJsonColumn(ResultSet resultSet, String column, Class<T> type) throws CommandPersistenceException {
        try {
            final var payloadOutput = resultSet.getString(column);
            return objectMapper.readValue(payloadOutput, type);
        } catch (JsonProcessingException exception) {
            throw new CommandPersistenceException(exception);
        } catch (SQLException exception) {
            throw new CommandPersistenceException(exception);
        }
    }

    private CommandContext toCommandContext(ResultSet resultSet) throws CommandPersistenceException {
        return this.readJsonColumn(resultSet, "CONTEXT", CommandContext.class);
    }

    private CommandPayload toCommandPayloadInput(ResultSet resultSet) throws CommandPersistenceException {
        return this.readJsonColumn(resultSet, "PAYLOAD_INPUT", CommandPayload.class);
    }

    private CommandPayload toCommandPayloadOutput(ResultSet resultSet) throws CommandPersistenceException {
        return this.readJsonColumn(resultSet, "PAYLOAD_OUTPUT", CommandPayload.class);
    }

    private LocalDateTime readTimestamp(ResultSet resultSet, String column) throws SQLException {
        final var instant = resultSet.getObject(column, Instant.class);
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private LocalDateTime readOptionalTimestamp(ResultSet resultSet, String column) throws SQLException {
        final var timestamp = resultSet.getTimestamp(column);
        if (timestamp == null) {
            return null;
        }
        return this.readTimestamp(resultSet, column);
    }

    private CommandResult mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
        // CommandResult.Processed
        final var name = CommandName.valueOf(resultSet.getString("NAME"));
        final var status = CommandStatus.valueOf(resultSet.getString("STATUS"));
        final var payloadInput = this.toCommandPayloadInput(resultSet);
        final var payloadOutput = this.toCommandPayloadOutput(resultSet);
        final var context = this.toCommandContext(resultSet);

        // CommandResult.Processed
        final var id = UUID.fromString(resultSet.getString("ID"));
        final var processedAt = this.readTimestamp(resultSet, "PROCESSED_AT");

        // CommandResult.Persisted
        final var persistedAt = this.readOptionalTimestamp(resultSet, "PERSISTED_AT");
        if (persistedAt == null) {
            return new CommandResult.Processed(
                name,
                status,
                payloadInput,
                payloadOutput,
                context,
                processedAt,
                id
            );
        }

        // CommandResult.Executed
        final var executedAt = this.readOptionalTimestamp(resultSet, "EXECUTED_AT");
        if (executedAt == null) {
            return new CommandResult.Persisted(
                name,
                status,
                payloadInput,
                payloadOutput,
                context,
                processedAt,
                id,
                persistedAt
            );
        }

        return new CommandResult.Executed(
            name,
            status,
            payloadInput,
            payloadOutput,
            context,
            processedAt,
            id,
            persistedAt,
            executedAt
        );
    }
}