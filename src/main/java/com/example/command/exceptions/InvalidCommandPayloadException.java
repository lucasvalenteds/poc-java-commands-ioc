package com.example.command.exceptions;

import java.io.Serial;

public final class InvalidCommandPayloadException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1446577454171290293L;

    private final String property;
    private final Object value;

    public InvalidCommandPayloadException(String property, Object value, Throwable throwable) {
        super("Invalid command payload attribute", throwable);
        this.property = property;
        this.value = value;
    }

    public InvalidCommandPayloadException(String property, Object value) {
        this(property, value, null);
    }

    public String getProperty() {
        return property;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getMessage() + ": " + this.property;
    }
}
