package com.example.command.contract;

public interface Command<T extends CommandPayload> {

    CommandName getName();

    CommandResult.Processed process(T payload);
}
