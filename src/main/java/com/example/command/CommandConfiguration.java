package com.example.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class CommandConfiguration {

    @Bean
    CommandFactory commandFactory(ApplicationContext context) {
        return new CommandFactoryDefault(context);
    }

    @Bean
    CommandRepository commandRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        return new CommandRepositoryDefault(jdbcTemplate, objectMapper);
    }

    @Bean
    CommandService commandService(CommandFactory commandFactory, CommandRepository commandRepository) {
        return new CommandServiceDefault(commandFactory, commandRepository);
    }
}
