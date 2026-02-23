package simulaciones;

import config.Configuracion;
import modelos.Muestra;
import utils.LoggerSimulacion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SimulacionPooled {
    private Configuracion config;
    private LoggerSimulacion logger;
    private List<Muestra> muestras;
    private PoolConexiones pool;
    private long tiempoTotal;
    private volatile boolean detener = false;
    
    public SimulacionPooled(Configuracion c) throws IOException {
        this.config = c;
        this.logger = new LoggerSimulacion("POOLED");
        this.muestras = new ArrayList<>();
        this.pool = new PoolConexiones(c);
    }
    
    public void ejecutar() throws InterruptedException {
        System.out.println("\n=== SIMULACION POOLED ===");
        System.out.println("Muestras: " + config.getCantidadMuestras());
        System.out.println("Pool max: " + config.getMaxConnections());
        System.out.println("Enter para detener");
        
        Thread freno = new Thread(() -> {
            new Scanner(System.in).nextLine();
            detener = true;
            System.out.println("Deteniendo...");
        });
        freno.setDaemon(true);
        freno.start();
        
        List<HiloMuestra> hilos = new ArrayList<>();
        long inicio = System.currentTimeMillis();
        
        for (int i = 0; i < config.getCantidadMuestras(); i++) {
            Muestra m = new Muestra(i+1);
            muestras.add(m);
            HiloMuestra h = new HiloMuestra(m, config, logger, true);
            h.setPool(pool);
            hilos.add(h);
        }
        
        for (HiloMuestra h : hilos) h.start();
        
        for (HiloMuestra h : hilos) {
            if (detener) h.detener();
            h.join();
        }
        
        tiempoTotal = System.currentTimeMillis() - inicio;
        pool.cerrarTodas();
        logger.logMetricas("POOLED", tiempoTotal, muestras);
        System.out.println("Tiempo: " + tiempoTotal + "ms");
        System.out.println("Conexiones creadas: " + pool.getCreadas());
    }
    
    public long getTiempoTotal() { return tiempoTotal; }
}