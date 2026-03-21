package main.Java.dbcomponent.adapters;

import java.sql.SQLException;

import main.Java.dbcomponent.core.ConnectionConfig;
import main.Java.dbcomponent.core.ConnectionProvider;
import main.Java.dbcomponent.core.DriverManagerConnectionProvider;

/**
 * Adapter generico para cualquier base SQL con driver JDBC.
 */
public class SqlAdapter implements IDAdapter {
    @Override
    public ConnectionProvider createProvider(ConnectionConfig config) throws SQLException {
        if (config == null) {
            throw new IllegalArgumentException("ConnectionConfig no puede ser null");
        }
        if (config.getDriverClassName() == null || config.getDriverClassName().isBlank()) {
            // En adapter generico el driver siempre debe venir informado.
            throw new IllegalArgumentException("driverClassName es requerido para SqlAdapter");
        }
        return new DriverManagerConnectionProvider(config);
    }
}
