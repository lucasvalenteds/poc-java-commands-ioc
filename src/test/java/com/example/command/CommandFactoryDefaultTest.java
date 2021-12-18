package com.example.command;

import com.example.command.commands.CommandsConfiguration;
import com.example.command.commands.SetLogLevel;
import com.example.command.commands.TurnOn;
import com.example.command.contract.Command;
import com.example.command.contract.CommandName;
import com.example.command.exceptions.CommandNotImplementedException;
import com.example.command.testing.CommandTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.stream.Stream;

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
            () -> factory.create(CommandName.NO_OP)
        );

        assertEquals("Command not implemented yet: NoOp", exception.getMessage());
        assertFalse(context.getBeansOfType(Command.class).isEmpty());
    }

    static Stream<Arguments> commands() {
        return Stream.of(
            Arguments.of(CommandName.SET_LOG_LEVEL, SetLogLevel.class),
            Arguments.of(CommandName.TURN_ON, TurnOn.class)
        );
    }

    @ParameterizedTest(name = "{0} is mapped to {1}")
    @MethodSource("commands")
    void testCreateReturnsNewCommandInstance(CommandName commandName, Class<?> commandClass) {
        final var command = factory.create(commandName);

        assertEquals(commandName, command.getName());
        assertEquals(commandClass, command.getClass());
        assertNotEquals(command, factory.create(commandName), "Command instance should be recreated");
    }
}