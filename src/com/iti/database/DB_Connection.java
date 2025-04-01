package com.iti.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DB_Connection {
    private static HikariDataSource dataSource;
    private static final String URL;
    private static final String USER;
    private static final String PASS;
    static {
 	   URL="jdbc:postgresql://localhost:5432/prepaid_balance_db";
 	   USER="postgres";
 	   PASS="12345";
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(URL);
            config.setUsername(USER);
            config.setPassword(PASS);
            config.setMaximumPoolSize(10); 
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setMaxLifetime(60000);
            config.setConnectionTimeout(3000);

            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing DataSource", e);
        }
    }

    public static DataSource getDataSource() {
        return dataSource;
    }
}
