package main.Java.simulaciones;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

import main.Java.config.Configuracion;
import main.Java.utils.DatabaseUtil;
import main.Java.utils.LoggerSimulacion;

/**
 * Hilo que ejecuta peticiones continuamente hasta que ocurra un error
 * o se solicite detener. Cuenta las peticiones exitosas y registra cada muestra en el log.
 */
public class HiloPeticion extends Thread {
    private final Configuracion config;
    private final boolean usarPool;
    private PoolConexiones pool;
    private final AtomicInteger contadorExitosas;
    private final AtomicBoolean errorOcurrido;
    private final AtomicBoolean detener;
    private final int idHilo;
    private final LoggerSimulacion logger;

    public HiloPeticion(int id, Configuracion c, boolean usarPool,
                        AtomicInteger contadorExitosas, AtomicBoolean errorOcurrido,
                        AtomicBoolean detener, LoggerSimulacion logger) {
        this.idHilo = id;
        this.config = c;
        this.usarPool = usarPool;
        this.contadorExitosas = contadorExitosas;
        this.errorOcurrido = errorOcurrido;
        this.detener = detener;
        this.logger = logger;
    }

    public void setPool(PoolConexiones p) { this.pool = p; }

    @Override
    public void run() {
        while (!errorOcurrido.get() && !detener.get()) {
            Connection conn = null;
            try {
                if (usarPool && pool != null) {
                    conn = pool.obtenerConexion();
                } else {
                    conn = DatabaseUtil.getConnection(config);
                }

                if (conn != null) {
                    boolean exito = DatabaseUtil.ejecutarQuery(conn, config.getQuery(), false);

                    if (usarPool && pool != null) {
                        pool.liberarConexion(conn);
                    } else {
                        DatabaseUtil.cerrarConexion(conn);
                    }

                    if (exito) {
                        contadorExitosas.incrementAndGet();
                        if (logger != null) logger.logMuestra(idHilo, "EXITOSA");
                    } else {
                        if (logger != null) logger.logMuestra(idHilo, "FALLIDA");
                        errorOcurrido.set(true);
                        break;
                    }
                } else {
                    if (logger != null) logger.logMuestra(idHilo, "FALLIDA");
                    errorOcurrido.set(true);
                    break;
                }
            } catch (Exception e) {
                System.err.println("Error en hilo " + idHilo + ": " + e.getMessage());
                if (logger != null) logger.logMuestra(idHilo, "FALLIDA");
                errorOcurrido.set(true);
                if (conn != null && !usarPool) {
                    DatabaseUtil.cerrarConexion(conn);
                }
                break;
            }
        }
    }
}
