package com.example.command.contract;

public interface CommandPayload {

    final record Empty() implements CommandPayload {
    }
}
