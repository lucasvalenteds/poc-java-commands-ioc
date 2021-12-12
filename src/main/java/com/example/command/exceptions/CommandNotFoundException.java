package com.example.command.exceptions;

import java.io.Serial;
import java.util.UUID;

public final class CommandNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1472978273383520595L;

    public CommandNotFoundException(UUID id, Throwable throwable) {
        super("Command not found with ID " + id, throwable);
    }
}