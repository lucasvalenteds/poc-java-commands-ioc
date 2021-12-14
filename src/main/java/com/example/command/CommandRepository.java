package com.example.command;

import com.example.command.contract.CommandResult;
import com.example.command.exceptions.CommandNotFoundException;
import com.example.command.exceptions.CommandPersistenceException;

import java.util.UUID;

public interface CommandRepository {

    CommandResult findById(UUID id) throws CommandNotFoundException;

    void save(CommandResult.Processed result) throws CommandPersistenceException;

    void save(CommandResult.Persisted result) throws CommandPersistenceException;

    void save(CommandResult.Executed result) throws CommandPersistenceException;
}
