package com.example.command;

import com.example.command.contract.Command;
import com.example.command.contract.CommandName;
import com.example.command.contract.CommandPayload;
import com.example.command.exceptions.CommandNotImplementedException;
import org.springframework.context.ApplicationContext;

public final class CommandFactoryDefault implements CommandFactory {

    private final ApplicationContext context;

    public CommandFactoryDefault(ApplicationContext context) {
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    public Command<CommandPayload> create(CommandName commandName) throws CommandNotImplementedException {
        final var name = commandName.name();

        if (context.containsBean(name)) {
            return (Command<CommandPayload>) context.getBean(name, Command.class);
        }

        throw new CommandNotImplementedException(commandName);
    }
}
