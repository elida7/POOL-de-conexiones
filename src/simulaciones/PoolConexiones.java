package simulaciones;

import config.Configuracion;
import utils.DatabaseUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class PoolConexiones {
    private BlockingQueue<Connection> pool;
    private Configuracion config;
    private AtomicInteger creadas = new AtomicInteger(0);
    private int max;
    
    public PoolConexiones(Configuracion c) {
        this.config = c;
        this.max = c.getMaxConnections();
        this.pool = new LinkedBlockingQueue<>(max);
        
        for (int i = 0; i < c.getMinConnections(); i++) {
            try { pool.offer(crearNueva()); } catch (SQLException e) { }
        }
    }
    
    private Connection crearNueva() throws SQLException {
        creadas.incrementAndGet();
        return DatabaseUtil.getConnection(config);
    }
    
    public Connection obtenerConexion() throws SQLException {
        Connection conn = pool.poll();
        if (conn == null && creadas.get() < max) {
            conn = crearNueva();
        } else if (conn == null) {
            try { conn = pool.take(); } catch (InterruptedException e) { }
        }
        return conn;
    }
    
    public void liberarConexion(Connection conn) {
        if (conn != null) pool.offer(conn);
    }
    
    public void cerrarTodas() {
        pool.forEach(c -> { try { c.close(); } catch (SQLException e) { } });
        pool.clear();
    }
    
    public int getCreadas() { return creadas.get(); }
}