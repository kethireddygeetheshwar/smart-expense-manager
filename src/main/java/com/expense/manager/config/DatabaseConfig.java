package com.expense.manager.config;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource(Environment env) {
        String raw = env.getProperty("SPRING_DATASOURCE_URL");
        if (raw == null || raw.isBlank()) {
            raw = env.getProperty("spring.datasource.url");
        }
        if (raw != null && raw.startsWith("postgres://")) {
            String jdbcUrl = "jdbc:postgresql://" + raw.substring("postgres://".length());
            String username = env.getProperty("SPRING_DATASOURCE_USERNAME");
            String password = env.getProperty("SPRING_DATASOURCE_PASSWORD");
            return DataSourceBuilder.create()
                    .driverClassName("org.postgresql.Driver")
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .build();
        }
        return DataSourceBuilder.create().build();
    }
}
