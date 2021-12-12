package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@PropertySource("classpath:application.properties")
public class ServiceConfiguration {

    @Bean
    DataSource dataSource(Environment environment) {
        final var dataSource = new JdbcDataSource();

        dataSource.setURL(environment.getRequiredProperty("database.url", String.class));
        dataSource.setUser(environment.getRequiredProperty("database.user", String.class));
        dataSource.setPassword(environment.getRequiredProperty("database.password", String.class));

        return dataSource;
    }

    @Bean
    JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}