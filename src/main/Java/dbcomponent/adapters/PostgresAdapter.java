package main.Java.dbcomponent.adapters;

import java.sql.SQLException;

import main.Java.dbcomponent.core.ConnectionConfig;
import main.Java.dbcomponent.core.ConnectionProvider;
import main.Java.dbcomponent.core.DriverManagerConnectionProvider;

/**
 * Adapter concreto para PostgreSQL.
 */
public class PostgresAdapter implements IDAdapter {
    public static final String POSTGRES_DRIVER = "org.postgresql.Driver";

    @Override
    public ConnectionProvider createProvider(ConnectionConfig config) throws SQLException {
        if (config == null) {
            throw new IllegalArgumentException("ConnectionConfig no puede ser null");
        }

        String driver = config.getDriverClassName();
        if (driver == null || driver.isBlank()) {
            ConnectionConfig resolved = new ConnectionConfig(
                config.getJdbcUrl(),
                config.getUsername(),
                config.getPassword(),
                POSTGRES_DRIVER
            );
            return new DriverManagerConnectionProvider(resolved);
        }

        return new DriverManagerConnectionProvider(config);
    }
}
