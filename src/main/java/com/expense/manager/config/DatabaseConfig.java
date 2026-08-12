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
        if (rawUrl != null && rawUrl.startsWith("postgres://")) {
            String jdbcUrl = "jdbc:postgresql://" + rawUrl.substring("postgres://".length());
            if (!jdbcUrl.contains("?")) {
                jdbcUrl += "?sslmode=require";
            }
            DataSourceProperties converted = new DataSourceProperties();
            converted.setUrl(jdbcUrl);
            converted.setUsername(properties.getUsername());
            converted.setPassword(properties.getPassword());
            converted.setDriverClassName("org.postgresql.Driver");
            return converted.initializeDataSourceBuilder().build();
        }
        return properties.initializeDataSourceBuilder().build();
    }
}
