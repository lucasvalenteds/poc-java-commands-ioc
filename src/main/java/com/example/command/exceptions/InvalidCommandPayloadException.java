package com.example.command.exceptions;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serial;

public final class InvalidCommandPayloadException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2660166554859092379L;

    @JsonIgnore
    private final String property;

    @JsonIgnore
    private final transient Object value;

    public InvalidCommandPayloadException(String property, Object value) {
        super("Invalid command payload attribute: " + property);
        this.property = property;
        this.value = value;
    }

    public String getProperty() {
        return property;
    }

    public Object getValue() {
        return value;
    }
}
