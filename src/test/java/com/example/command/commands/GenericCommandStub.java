package com.example.command.commands;

import com.example.command.contract.CommandName;
import com.example.command.contract.CommandPayload;

final class GenericCommandStub extends GenericCommand<CommandPayload.Empty, CommandPayload.Empty> {

    @Override
    public CommandName getName() {
        return CommandName.NoOp;
    }
}
