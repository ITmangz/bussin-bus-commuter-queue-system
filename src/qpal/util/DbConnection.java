package qpal.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/qpal";
    private static final String USER = "root";
    private static final String PASSWORD = "";

     public static Connection getConnection() throws SQLException {
        java.util.Properties properties = new java.util.Properties();
        properties.setProperty("user", System.getProperty("qpal.db.user", USER));
        properties.setProperty("password", System.getProperty("qpal.db.password", PASSWORD));
        properties.setProperty("connectTimeout", "5000");
        properties.setProperty("socketTimeout", "15000");
        Connection connection = DriverManager.getConnection(System.getProperty("qpal.db.url", URL), properties);
        try (java.sql.Statement statement=connection.createStatement()) {
            statement.execute("SET time_zone = '+08:00'");
        } catch(SQLException ex) { connection.close(); throw ex; }
        return connection;
    }
}
