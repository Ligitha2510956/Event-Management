package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Shared JDBC connection helper.
 * Every DAO (EventDAO, ParticipantDAO, RegistrationDAO) should call
 * DatabaseConnection.getConnection() instead of writing its own connection code.
 *
 * IMPORTANT: Do not commit your real password to GitHub.
 * Replace the placeholder below locally, and keep this file out of version
 * control if you put a real password in it (see .gitignore).
 */
public class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/event_registration";
    private static final String USER = "root";
    private static final String PASSWORD = "LIGLIG";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found. Add the mysql-connector-j jar to your classpath.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
