package com.example.command.testing;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.Serial;

public final class JsonProcessingExceptionStub extends JsonProcessingException {

    @Serial
    private static final long serialVersionUID = -7261943307378933392L;

    public JsonProcessingExceptionStub() {
        super("");
    }
}
