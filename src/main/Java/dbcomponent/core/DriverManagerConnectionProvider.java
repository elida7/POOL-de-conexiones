package main.Java.dbcomponent.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Implementacion basada en DriverManager para cualquier driver JDBC.
 */
public class DriverManagerConnectionProvider implements ConnectionProvider {
    private final ConnectionConfig config;

    public DriverManagerConnectionProvider(ConnectionConfig config) throws SQLException {
        this.config = config;
        cargarDriver(config.getDriverClassName());
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            config.getJdbcUrl(),
            config.getUsername(),
            config.getPassword()
        );
    }

    private void cargarDriver(String driverClassName) throws SQLException {
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC no encontrado: " + driverClassName, e);
        }
    }
}
