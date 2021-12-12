package com.example.command.contract;

import com.example.command.commands.SetLogLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, defaultImpl = CommandContext.Empty.class)
@JsonSubTypes({
    @JsonSubTypes.Type(CommandContext.Error.class),
    @JsonSubTypes.Type(SetLogLevel.PayloadContext.class),
})
public interface CommandContext {

    @JsonIgnoreProperties(ignoreUnknown = true)
    final record Empty() implements CommandContext {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    final record Error(Throwable throwable) implements CommandContext {
    }
}
