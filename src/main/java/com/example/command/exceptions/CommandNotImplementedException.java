package com.example.command.exceptions;

import com.example.command.contract.CommandName;

import java.io.Serial;

public final class CommandNotImplementedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 6846186007140913447L;

    public CommandNotImplementedException(CommandName commandName) {
        super("Command not implemented yet: " + commandName.name());
    }
}