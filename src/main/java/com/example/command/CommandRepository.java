package com.example.command;

import com.example.command.contract.CommandResult;
import com.example.command.exceptions.CommandNotFoundException;
import org.springframework.dao.DataAccessException;

import java.util.UUID;

public interface CommandRepository {

    CommandResult findById(UUID id) throws CommandNotFoundException;

    void save(CommandResult.Processed result) throws DataAccessException;

    void save(CommandResult.Persisted result) throws DataAccessException;

    void save(CommandResult.Executed result) throws DataAccessException;
}
