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
        if (isPostgresUrl(rawUrl)) {
            DataSourceProperties converted = new DataSourceProperties();
            converted.setDriverClassName("org.postgresql.Driver");
            converted.setUrl(toJdbcUrl(rawUrl));
            String[] creds = extractCredentials(rawUrl);
            if (creds != null) {
                converted.setUsername(creds[0]);
                converted.setPassword(creds[1]);
            } else {
                converted.setUsername(properties.getUsername());
                converted.setPassword(properties.getPassword());
            }
            return converted.initializeDataSourceBuilder().build();
        }
        return properties.initializeDataSourceBuilder().build();
    }

    private boolean isPostgresUrl(String raw) {
        String lower = raw.toLowerCase();
        return lower.startsWith("postgres://") || lower.startsWith("postgresql://");
    }

    private String toJdbcUrl(String raw) {
        String body = raw.substring(raw.indexOf("://") + 3);
        String hostPortDb = body;
        int at = body.lastIndexOf('@');
        if (at >= 0) {
            hostPortDb = body.substring(at + 1);
        }
        int slash = hostPortDb.indexOf('/');
        String hostPort = slash >= 0 ? hostPortDb.substring(0, slash) : hostPortDb;
        String db = slash >= 0 ? hostPortDb.substring(slash + 1) : "";
        if (hostPort.indexOf(':') < 0) {
            hostPort = hostPort + ":5432";
        }
        String jdbc = "jdbc:postgresql://" + hostPort + "/" + db;
        if (!jdbc.contains("?")) {
            jdbc += "?sslmode=require";
        }
        return jdbc;
    }

    private String[] extractCredentials(String raw) {
        String body = raw.substring(raw.indexOf("://") + 3);
        int at = body.indexOf('@');
        if (at < 0) {
            return null;
        }
        String userInfo = body.substring(0, at);
        int colon = userInfo.indexOf(':');
        if (colon < 0) {
            return new String[]{userInfo, ""};
        }
        return new String[]{userInfo.substring(0, colon), userInfo.substring(colon + 1)};
    }
}
