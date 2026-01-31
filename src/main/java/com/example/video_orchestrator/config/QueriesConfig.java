package com.example.video_orchestrator.config;

import com.example.postgresql.Queries;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;

@Configuration
public class QueriesConfig {

    @Bean
    public Queries queries(Connection connection) {
        return new Queries(connection);
    }
}
