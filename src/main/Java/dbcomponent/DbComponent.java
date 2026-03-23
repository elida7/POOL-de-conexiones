package main.Java.dbcomponent;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
     * Ejecuta una query predefinida por clave (sin parametros).
     */
    public boolean query(String key) throws SQLException {
        return query(key, List.of());
    }

    /**
     * Ejecuta una query predefinida por clave con argumentos para placeholders {@code ?}.
     */
    public boolean query(String key, List<Object> params) throws SQLException {
        // No recibimos SQL crudo: todo sale de queries predefinidas por key.
        String sql = queryRepository.getByKey(key);
        List<Object> p = params != null ? params : List.of();
        validarPlaceholders(sql, p);
        Connection connection = null;
        try {
            connection = pool.acquire();
            return ejecutar(connection, sql, p);
        } finally {
            pool.release(connection);
        }
    }

    /**
     * Ejecuta una transaccion: cada paso es una query predefinida con su propio conjunto de argumentos.
     */
    public boolean transaction(List<QueryInvocation> steps) throws SQLException {
        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("steps no puede ser null o vacia");
        }

        Connection connection = null;
        boolean originalAutoCommit = true;
        try {
            connection = pool.acquire();
            // Guardamos estado original para no "contaminar" la conexion reciclada.
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            for (QueryInvocation step : steps) {
                String sql = queryRepository.getByKey(step.getKey());
                List<Object> p = step.getParams();
                validarPlaceholders(sql, p);
                if (!ejecutar(connection, sql, p)) {
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

    private static void validarPlaceholders(String sql, List<Object> params) {
        int esperados = contarPlaceholders(sql);
        if (esperados != params.size()) {
            throw new IllegalArgumentException(
                "Cantidad de '?' (" + esperados + ") no coincide con parametros (" + params.size() + ")"
            );
        }
    }

    private static int contarPlaceholders(String sql) {
        int c = 0;
        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == '?') {
                c++;
            }
        }
        return c;
    }

    private boolean ejecutar(Connection connection, String sql, List<Object> params) throws SQLException {
        if (params.isEmpty()) {
            return ejecutarSqlSinParams(connection, sql);
        }
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ps.execute();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean ejecutarSqlSinParams(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
