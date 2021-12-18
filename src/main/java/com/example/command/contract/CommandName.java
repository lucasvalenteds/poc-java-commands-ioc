package com.example.command.contract;

import com.example.command.exceptions.CommandNotImplementedException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CommandName {
    SET_LOG_LEVEL("SetLogLevel"),
    TURN_ON("TurnOn"),
    NO_OP("NoOp");

    private final String publicName;

    CommandName(String publicName) {
        this.publicName = publicName;
    }

    @JsonCreator
    public static CommandName fromString(final String string) {
        for (CommandName commandName : CommandName.values()) {
            if (commandName.getPublicName().equals(string)) {
                return commandName;
            }
        }

        throw new CommandNotImplementedException(string);
    }

    @JsonValue
    public String getPublicName() {
        return publicName;
    }
}
