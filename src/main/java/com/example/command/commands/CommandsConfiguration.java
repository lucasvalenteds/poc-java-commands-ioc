package com.example.command.commands;

import com.example.command.contract.Command;
import com.example.device.DeviceService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@SuppressWarnings("rawtypes")
public class CommandsConfiguration {

    @Bean("SetLogLevel")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    Command setLogLevel(DeviceService deviceService) {
        return new SetLogLevel(deviceService);
    }

    @Bean("TurnOn")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    Command turnOn() {
        return new TurnOn();
    }
}
