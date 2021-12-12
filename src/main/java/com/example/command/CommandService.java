package com.example.command;

import com.example.command.contract.CommandName;
import com.example.command.contract.CommandPayload;
import com.example.command.contract.CommandResult;
import com.example.command.exceptions.CommandNotFoundException;
import com.example.command.exceptions.CommandNotImplementedException;
import com.example.command.exceptions.CommandPersistenceException;
import com.example.command.exceptions.InvalidCommandPayloadException;

import java.util.UUID;

public interface CommandService {

    CommandResult findById(UUID id) throws CommandNotFoundException;

    CommandId execute(CommandName name, CommandPayload payload)
        throws CommandNotImplementedException, InvalidCommandPayloadException, CommandPersistenceException;
}
