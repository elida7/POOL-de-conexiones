package simulaciones;

import config.Configuracion;
import modelos.Muestra;
import utils.DatabaseUtil;
import utils.LoggerSimulacion;
import java.sql.Connection;

public class HiloMuestra extends Thread {
    private Muestra muestra;
    private Configuracion config;
    private LoggerSimulacion logger;
    private boolean usarPool;
    private PoolConexiones pool;
    private volatile boolean detener = false;
    
    public HiloMuestra(Muestra m, Configuracion c, LoggerSimulacion l, boolean p) {
        this.muestra = m;
        this.config = c;
        this.logger = l;
        this.usarPool = p;
    }
    
    public void setPool(PoolConexiones p) { this.pool = p; }
    public void detener() { this.detener = true; }
    
    @Override
    public void run() {
        long inicio = System.currentTimeMillis();
        boolean exito = false;
        
        while (!exito && muestra.getIntentos() < config.getMaxReintentos() && !detener) {
            muestra.incrementarIntentos();
            Connection conn = null;
            
            try {
                if (usarPool && pool != null) {
                    conn = pool.obtenerConexion();
                } else {
                    conn = DatabaseUtil.getConnection(config);
                }
                
                if (conn != null) {
                    exito = DatabaseUtil.ejecutarQuery(conn, config.getQuery());
                    
                    if (usarPool && pool != null) {
                        pool.liberarConexion(conn);
                    } else {
                        DatabaseUtil.cerrarConexion(conn);
                    }
                }
            } catch (Exception e) {
                if (!usarPool) DatabaseUtil.cerrarConexion(conn);
            }
            
            if (!exito && muestra.getIntentos() < config.getMaxReintentos()) {
                try { Thread.sleep(50); } catch (InterruptedException e) { break; }
            }
        }
        
        muestra.setExitosa(exito);
        muestra.setTiempoEjecucion(System.currentTimeMillis() - inicio);
        if (!exito) muestra.setError("Falló tras " + muestra.getIntentos() + " intentos");
        logger.log(muestra);
    }
}