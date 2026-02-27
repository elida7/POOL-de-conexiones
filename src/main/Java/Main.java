package main.Java;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import main.Java.config.Configuracion;
import main.Java.simulaciones.SimulacionPooled;
import main.Java.simulaciones.SimulacionRaw;

public class Main {
    public static void main(String[] args) {

        System.out.println("=== POOL DE CONEXIONES ===");
        System.out.println("RAW y POOLED se ejecutan AL MISMO TIEMPO (simultaneamente).");
        System.out.println("Presiona Enter para detener AMBAS simulaciones.");

        try {
            Configuracion config = new Configuracion("config.properties");
            AtomicBoolean detener = new AtomicBoolean(false);

            Thread freno = new Thread(() -> {
                new Scanner(System.in).nextLine();
                detener.set(true);
                System.out.println("Deteniendo ambas...");
            });
            freno.setDaemon(true);
            freno.start();

            System.out.println("\n--- Iniciando RAW y POOLED a la vez ---");
            SimulacionRaw raw = new SimulacionRaw(config);
            SimulacionPooled pooled = new SimulacionPooled(config);

            Thread hiloRaw = new Thread(() -> {
                try {


                    raw.ejecutarCon(detener);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            Thread hiloPooled = new Thread(() -> {
                try {
                    pooled.ejecutarCon(detener);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            hiloRaw.start();
            hiloPooled.start();
            hiloRaw.join();
            hiloPooled.join();

            long tiempoRaw = raw.getTiempoTotal();
            long tiempoPooled = pooled.getTiempoTotal();

            System.out.println("\n========== COMPARACIÓN DE DESEMPEÑO ==========");
            System.out.println("SIN POOL (RAW):");
            System.out.println("  - Tiempo: " + tiempoRaw + " ms");
            System.out.println("  - Peticiones exitosas: " + raw.getPeticionesExitosas());
            System.out.println("CON POOL (POOLED):");
            System.out.println("  - Tiempo: " + tiempoPooled + " ms");
            System.out.println("  - Peticiones exitosas: " + pooled.getPeticionesExitosas());
            System.out.println("-----------------------------------------------");
            if (pooled.getPeticionesExitosas() > raw.getPeticionesExitosas()) {
                System.out.println(">>> MEJOR: POOLED (" + (pooled.getPeticionesExitosas() - raw.getPeticionesExitosas()) + " más peticiones en menos tiempo)");
            } else if (raw.getPeticionesExitosas() > pooled.getPeticionesExitosas()) {
                System.out.println(">>> MEJOR: RAW (" + (raw.getPeticionesExitosas() - pooled.getPeticionesExitosas()) + " más peticiones)");
            } else if (tiempoPooled < tiempoRaw) {
                System.out.println(">>> MEJOR DESEMPEÑO: POOLED (más rápido en " + (tiempoRaw - tiempoPooled) + " ms)");
            } else if (tiempoRaw < tiempoPooled) {
                System.out.println(">>> MEJOR DESEMPEÑO: RAW (más rápido en " + (tiempoPooled - tiempoRaw) + " ms)");
            } else {
                System.out.println(">>> EMPATE: Ambos métodos tuvieron el mismo tiempo y peticiones.");
            }
            System.out.println("===============================================");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        
    }
}
