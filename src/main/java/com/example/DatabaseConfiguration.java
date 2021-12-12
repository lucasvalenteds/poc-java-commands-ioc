package com.example;

import org.flywaydb.core.Flyway;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    @Bean
    public Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
            .locations("classpath:migrations")
            .dataSource(dataSource)
            .load();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        event.getApplicationContext()
            .getBean(Flyway.class)
            .migrate();
    }
}
