package com.example.command.testing;

import com.example.device.DeviceService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandTestConfiguration {

    @Bean
    DeviceService deviceService() {
        return Mockito.mock(DeviceService.class);
    }
}
