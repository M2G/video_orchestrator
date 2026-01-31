package com.example.video_orchestrator.config;

import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setURL("jdbc:postgresql://127.0.0.1:5432/video_jobs_db");
        ds.setUser("postgres");
        ds.setPassword("postgres");
        return ds;
    }

    @Bean
    public Connection connection(DataSource ds) throws Exception {
        return ds.getConnection();
    }
}
