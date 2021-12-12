package com.example.command.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.Serial;
import java.sql.SQLException;

public final class CommandPersistenceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6903833369328841170L;

    public CommandPersistenceException(JsonProcessingException exception) {
        super("Could not serialize/deserialize command", exception);
    }

    public CommandPersistenceException(SQLException exception) {
        super("Could not persist command", exception);
    }
}