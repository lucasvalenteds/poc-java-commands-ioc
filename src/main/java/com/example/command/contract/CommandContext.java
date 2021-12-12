package com.example.command.contract;

public interface CommandContext {

    final record Empty() implements CommandContext {
    }

    final record Error(Throwable throwable) implements CommandContext {
    }
}
