package com.commonground.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbUtil {

    private static final String DB_URL  = buildUrl();
    private static final String DB_USER = System.getenv().getOrDefault("MYSQLUSER",     "cguser");
    private static final String DB_PASS = System.getenv().getOrDefault("MYSQLPASSWORD", "cgpass123");

    private static String buildUrl() {
        String host = System.getenv().getOrDefault("MYSQLHOST",     "localhost");
        String port = System.getenv().getOrDefault("MYSQLPORT",     "3306");
        String db   = System.getenv().getOrDefault("MYSQLDATABASE", "CommonGround_db");
        return "jdbc:mysql://" + host + ":" + port + "/" + db
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }
}
