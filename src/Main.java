import config.Configuracion;
import simulaciones.SimulacionPooled;
import simulaciones.SimulacionRaw;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        System.out.println("=== POOL DE CONEXIONES ===");
        
        try {
            Configuracion config = new Configuracion("config.properties");
            System.out.println("Config cargada: " + config.getCantidadMuestras() + " muestras");
            
            System.out.println("\nPresione Enter para RAW");
            sc.nextLine();
            SimulacionRaw raw = new SimulacionRaw(config);
            raw.ejecutar();
            
            System.out.println("\nPresione Enter para POOLED");
            sc.nextLine();
            SimulacionPooled pooled = new SimulacionPooled(config);
            pooled.ejecutar();
            
            System.out.println("\n=== ANALISIS ===");
            System.out.println("RAW: " + raw.getTiempoTotal() + "ms");
            System.out.println("POOLED: " + pooled.getTiempoTotal() + "ms");
            
            if (pooled.getTiempoTotal() < raw.getTiempoTotal()) {
                System.out.println("MEJOR: POOLED");
            } else {
                System.out.println("MEJOR: RAW");
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        sc.close();
    }
}