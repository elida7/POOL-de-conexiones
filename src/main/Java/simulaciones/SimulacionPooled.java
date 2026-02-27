package main.Java.simulaciones;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import main.Java.config.Configuracion;
import main.Java.utils.LoggerSimulacion;

public class SimulacionPooled {
    private Configuracion config;
    private LoggerSimulacion logger;
    private PoolConexiones pool;
    private long tiempoTotal;
    private int peticionesExitosas;
    private final AtomicBoolean detener = new AtomicBoolean(false);

    public SimulacionPooled(Configuracion c) throws IOException {
        this.config = c;
        this.logger = new LoggerSimulacion("POOLED");
        this.pool = new PoolConexiones(c);
    }

    /** Ejecuta usando bandera de detencion propia . */
    public void ejecutar() throws InterruptedException {
        Thread freno = new Thread(() -> {
            new Scanner(System.in).nextLine();
            detener.set(true);
            System.out.println("Deteniendo...");
        });
        freno.setDaemon(true);
        freno.start();
        ejecutarCon(detener);
    }

    /** Ejecuta con bandera compartida (para modo simultaneo). */
    public void ejecutarCon(AtomicBoolean detenerCompartido) throws InterruptedException {
        System.out.println("\n=== SIMULACION POOLED (con pool) ===");
        System.out.println("Hilos: " + config.getCantidadMuestras());
        System.out.println("Pool max: " + config.getMaxConnections());
        System.out.println("Peticiones continuas hasta error o Enter para detener.");

        AtomicInteger contadorExitosas = new AtomicInteger(0);
        AtomicBoolean errorOcurrido = new AtomicBoolean(false);

        List<HiloPeticion> hilos = new ArrayList<>();
        for (int i = 0; i < config.getCantidadMuestras(); i++) {
            HiloPeticion h = new HiloPeticion(i + 1, config, true, contadorExitosas, errorOcurrido, detenerCompartido, logger);
            h.setPool(pool);
            hilos.add(h);
        }

        long inicio = System.currentTimeMillis();
        for (HiloPeticion h : hilos) h.start();

        Thread progreso = new Thread(() -> {
            while (!detenerCompartido.get() && !errorOcurrido.get()) {
                try {
                    Thread.sleep(2000);
                    if (!detenerCompartido.get()) System.out.println("  [POOLED] Peticiones hasta ahora: " + contadorExitosas.get());
                } catch (InterruptedException e) { break; }
            }
        });
        progreso.setDaemon(true);
        progreso.start();

        for (HiloPeticion h : hilos) {
            h.join();
        }

        tiempoTotal = System.currentTimeMillis() - inicio;
        peticionesExitosas = contadorExitosas.get();
        pool.cerrarTodas();

        logger.logMetricasPeticiones("POOLED", tiempoTotal, peticionesExitosas, errorOcurrido.get());
        System.out.println("Tiempo: " + tiempoTotal + " ms");
        System.out.println("Peticiones exitosas (con pool): " + peticionesExitosas);
        System.out.println("Conexiones creadas: " + pool.getCreadas());
    }

    public long getTiempoTotal() { return tiempoTotal; }
    public int getPeticionesExitosas() { return peticionesExitosas; }
}