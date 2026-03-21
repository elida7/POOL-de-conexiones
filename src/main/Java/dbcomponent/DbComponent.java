package main.Java.dbcomponent;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import main.Java.dbcomponent.adapters.IDAdapter;
import main.Java.dbcomponent.core.ConnectionConfig;
import main.Java.dbcomponent.core.ConnectionPool;
import main.Java.dbcomponent.core.ConnectionProvider;
import main.Java.dbcomponent.queries.QueryRepository;

/**
 * Componente de BD desacoplado con pool interno y queries predefinidas.
 */
public class DbComponent {
    private final ConnectionPool pool;
    private final QueryRepository queryRepository;

    public DbComponent(
        IDAdapter adapter,
        ConnectionConfig config,
        int minConnections,
        int maxConnections,
        QueryRepository queryRepository
    ) throws SQLException {
        if (adapter == null) {
            throw new IllegalArgumentException("adapter no puede ser null");
        }
        if (queryRepository == null) {
            throw new IllegalArgumentException("queryRepository no puede ser null");
        }

        // El adapter decide como crear conexiones segun el motor.
        ConnectionProvider provider = adapter.createProvider(config);
        // El componente siempre trabaja contra el pool, no contra DriverManager directo.
        this.pool = new ConnectionPool(provider, minConnections, maxConnections);
        this.queryRepository = queryRepository;
    }

    /**
     * Ejecuta una query predefinida por clave.
     */
    public boolean query(String key) throws SQLException {
        // No recibimos SQL crudo: todo sale de queries predefinidas por key.
        String sql = queryRepository.getByKey(key);
        Connection connection = null;
        try {
            connection = pool.acquire();
            return ejecutarSql(connection, sql);
        } finally {
            pool.release(connection);
        }
    }

    /**
     * Ejecuta una transaccion con una lista de keys predefinidas.
     */
    public boolean transaction(List<String> keys) throws SQLException {
        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("keys no puede ser null o vacia");
        }

        Connection connection = null;
        boolean originalAutoCommit = true;
        try {
            connection = pool.acquire();
            // Guardamos estado original para no "contaminar" la conexion reciclada.
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            for (String key : keys) {
                String sql = queryRepository.getByKey(key);
                if (!ejecutarSql(connection, sql)) {
                    // Si una query falla, toda la transaccion se revierte.
                    connection.rollback();
                    return false;
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback();
            }
            throw e;
        } finally {
            if (connection != null) {
                try {
                    // Restauramos auto-commit antes de devolver al pool.
                    connection.setAutoCommit(originalAutoCommit);
                } catch (SQLException ignored) {
                }
            }
            pool.release(connection);
        }
    }

    public void close() {
        pool.closeAll();
    }

    public int getTotalConnectionsCreated() {
        return pool.getTotalCreadas();
    }

    private boolean ejecutarSql(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
