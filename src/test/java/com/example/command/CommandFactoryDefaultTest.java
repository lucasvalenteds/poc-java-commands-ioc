package com.example.command;

import com.example.command.commands.CommandsConfiguration;
import com.example.command.commands.SetLogLevel;
import com.example.command.contract.Command;
import com.example.command.contract.CommandName;
import com.example.command.exceptions.CommandNotImplementedException;
import com.example.command.testing.CommandTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig({CommandTestConfiguration.class, CommandsConfiguration.class})
class CommandFactoryDefaultTest {

    private ApplicationContext context;
    private CommandFactory factory;

    @BeforeEach
    public void beforeEach(ApplicationContext context) {
        this.context = context;
        this.factory = new CommandFactoryDefault(context);
    }

    @Test
    void createThrowsWhenCommandNotImplemented() {
        final var exception = assertThrows(
            CommandNotImplementedException.class,
            () -> factory.create(CommandName.NoOp)
        );

        assertEquals("Command not implemented yet: NoOp", exception.getMessage());
        assertFalse(context.getBeansOfType(Command.class).isEmpty());
    }

    @Test
    void testCreateReturnsNewCommandInstance() {
        final var command = factory.create(CommandName.SetLogLevel);

        assertEquals(CommandName.SetLogLevel, command.getName());
        assertEquals(SetLogLevel.class, command.getClass());
        assertNotEquals(command, factory.create(CommandName.SetLogLevel), "Command instance should be recreated");
    }
}