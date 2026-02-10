package com.example.video_orchestrator.config;

import com.example.postgresql.Queries;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Configuration
public class QueriesConfig {

    @Bean
    public Queries queries(DataSource dataSource) throws SQLException {
        Connection connection = dataSource.getConnection();
        return new Queries(connection);
    }
}
