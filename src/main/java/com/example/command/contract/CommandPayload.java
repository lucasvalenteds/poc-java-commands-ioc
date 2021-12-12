package com.example.command.contract;

import com.example.command.commands.SetLogLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, defaultImpl = CommandPayload.Empty.class)
@JsonSubTypes({
    @JsonSubTypes.Type(SetLogLevel.class),
})
public interface CommandPayload {

    @JsonIgnoreProperties(ignoreUnknown = true)
    final record Empty() implements CommandPayload {
    }
}
