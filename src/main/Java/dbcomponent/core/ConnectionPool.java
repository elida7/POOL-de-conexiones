package main.Java.dbcomponent.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pool reutilizable y desacoplado del simulador.
 */
public class ConnectionPool {
    private final BlockingQueue<Connection> pool;
    private final ConnectionProvider provider;
    private final AtomicInteger totalCreadas;
    private final int maxConnections;

    public ConnectionPool(ConnectionProvider provider, int minConnections, int maxConnections) throws SQLException {
        if (provider == null) {
            throw new IllegalArgumentException("ConnectionProvider no puede ser null");
        }
        if (minConnections < 0 || maxConnections <= 0 || minConnections > maxConnections) {
            throw new IllegalArgumentException("Valores de pool invalidos");
        }

        this.provider = provider;
        this.maxConnections = maxConnections;
        this.totalCreadas = new AtomicInteger(0);
        this.pool = new LinkedBlockingQueue<>(maxConnections);

        inicializar(minConnections);
    }

    public Connection acquire() throws SQLException {
        Connection conn = pool.poll();
        if (conn != null) {
            return conn;
        }

        if (totalCreadas.get() < maxConnections) {
            return crearNueva();
        }

        try {
            return pool.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrumpido esperando conexion del pool", e);
        }
    }

    public void release(Connection connection) {
        if (connection == null) {
            return;
        }
        if (!pool.offer(connection)) {
            cerrarSilencioso(connection);
        }
    }

    public void closeAll() {
        for (Connection connection : pool) {
            cerrarSilencioso(connection);
        }
        pool.clear();
    }

    public int getTotalCreadas() {
        return totalCreadas.get();
    }

    private void inicializar(int minConnections) throws SQLException {
        for (int i = 0; i < minConnections; i++) {
            pool.offer(crearNueva());
        }
    }

    private Connection crearNueva() throws SQLException {
        Connection connection = provider.getConnection();
        totalCreadas.incrementAndGet();
        return connection;
    }

    private void cerrarSilencioso(Connection connection) {
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }
}
