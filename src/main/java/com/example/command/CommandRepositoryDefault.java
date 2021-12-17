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
import java.util.Optional;
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

        jdbcTemplate.update(query, preparedStatement -> {
            preparedStatement.setString(1, result.name().name());
            preparedStatement.setString(2, result.status().name());
            preparedStatement.setString(3, CommandRepositoryDefault.toJson(objectMapper, result.payloadInput()));
            preparedStatement.setString(4, CommandRepositoryDefault.toJson(objectMapper, result.payloadOutput()));
            preparedStatement.setString(5, CommandRepositoryDefault.toJson(objectMapper, result.context()));
            preparedStatement.setObject(6, result.processedAt(), Types.TIMESTAMP_WITH_TIMEZONE);
            preparedStatement.setObject(7, result.id(), Types.VARCHAR);
        });
    }

    @Transactional
    public void save(CommandResult.Persisted result) throws DataAccessException {
        final var query = "update COMMANDS set STATUS = ?, PERSISTED_AT = ? WHERE ID = ?";
        jdbcTemplate.update(query, preparedStatement -> {
            preparedStatement.setString(1, result.status().name());
            preparedStatement.setObject(2, result.persistedAt(), Types.TIMESTAMP_WITH_TIMEZONE);
            preparedStatement.setObject(3, result.id(), Types.VARCHAR);
        });
    }

    @Transactional
    public void save(CommandResult.Executed result) throws DataAccessException {
        final var query = "update COMMANDS set STATUS = ?, EXECUTED_AT = ? WHERE ID = ?";
        jdbcTemplate.update(query, preparedStatement -> {
            preparedStatement.setString(1, result.status().name());
            preparedStatement.setObject(2, result.executedAt(), Types.TIMESTAMP_WITH_TIMEZONE);
            preparedStatement.setObject(3, result.id(), Types.VARCHAR);
        });
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

    private Optional<LocalDateTime> readOptionalTimestamp(ResultSet resultSet, String column) throws SQLException {
        final var timestamp = resultSet.getTimestamp(column);
        if (timestamp == null) {
            return Optional.empty();
        }
        return Optional.of(this.readTimestamp(resultSet, column));
    }

    private CommandResult mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
        // CommandResult
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
        if (persistedAt.isEmpty()) {
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
        if (executedAt.isEmpty()) {
            return new CommandResult.Persisted(
                name,
                status,
                payloadInput,
                payloadOutput,
                context,
                processedAt,
                id,
                persistedAt.get()
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
            persistedAt.get(),
            executedAt.get()
        );
    }
}