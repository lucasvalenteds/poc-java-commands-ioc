package com.example.command.exceptions;

import java.io.Serial;
import java.util.UUID;

public final class CommandNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 4891400208981110136L;

    private final UUID id;

    public CommandNotFoundException(UUID id, Throwable throwable) {
        super("Command not found", throwable);
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}