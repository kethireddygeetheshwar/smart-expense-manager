package com.expense.manager.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {

    @Autowired
    private DataSourceProperties properties;

    @Bean
    public DataSource dataSource() {
        String rawUrl = properties.getUrl();
        if (rawUrl == null || rawUrl.isBlank()) {
            rawUrl = "jdbc:postgresql://localhost:5432/expense_manager";
        }
        String jdbcUrl = toJdbcUrl(rawUrl);
        DataSourceProperties converted = new DataSourceProperties();
        converted.setUrl(jdbcUrl);
        converted.setUsername(properties.getUsername());
        converted.setPassword(properties.getPassword());
        converted.setDriverClassName("org.postgresql.Driver");
        return converted.initializeDataSourceBuilder().build();
    }

    private String toJdbcUrl(String raw) {
        String lower = raw.toLowerCase();
        String jdbcUrl = raw;
        if (lower.startsWith("postgres://") || lower.startsWith("postgresql://")) {
            jdbcUrl = "jdbc:postgresql://" + raw.substring(raw.indexOf("://") + 3);
        } else if (!lower.startsWith("jdbc:")) {
            jdbcUrl = "jdbc:" + raw;
        }
        if (!jdbcUrl.contains("?")) {
            jdbcUrl += "?sslmode=require";
        }
        return jdbcUrl;
    }
}
