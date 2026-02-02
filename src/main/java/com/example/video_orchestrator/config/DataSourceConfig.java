package com.example.video_orchestrator.config;

import com.example.postgresql.Queries;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setURL("jdbc:postgresql://localhost:5432/postgres");
        ds.setUser("postgres");
        ds.setPassword("postgres");
        return ds;
    }

    @Bean
    public Queries queries(DataSource dataSource) throws SQLException {
        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(true);
        return new Queries(conn);
    }
}
