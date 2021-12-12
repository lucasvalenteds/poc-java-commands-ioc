package com.example.command;

import com.example.command.contract.Command;
import com.example.command.contract.CommandName;
import com.example.command.contract.CommandPayload;
import com.example.command.exceptions.CommandNotImplementedException;

public interface CommandFactory {

    Command<CommandPayload> create(CommandName commandName) throws CommandNotImplementedException;
}
