package utils;

import modelos.Muestra;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LoggerSimulacion {
    private PrintWriter writer;
    
    public LoggerSimulacion(String tipo) throws IOException {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        writer = new PrintWriter(new FileWriter("log_" + tipo + "_" + fecha + ".log"));
    }
    
    public void log(Muestra m) {
        writer.println(m.toString());
        writer.flush();
    }
    
    public void logMetricas(String tipo, long tiempoTotal, List<Muestra> muestras) {
        long exitosas = muestras.stream().filter(Muestra::isExitosa).count();
        long fallidas = muestras.size() - exitosas;
        double promedio = muestras.stream().mapToInt(Muestra::getIntentos).average().orElse(0);
        
        writer.println("\n=== METRICAS ===");
        writer.println("Tipo: " + tipo);
        writer.println("Tiempo: " + tiempoTotal + "ms");
        writer.println("Exitosas: " + exitosas + " (" + (exitosas*100/muestras.size()) + "%)");
        writer.println("Fallidas: " + fallidas + " (" + (fallidas*100/muestras.size()) + "%)");
        writer.println("Promedio reintentos: " + String.format("%.2f", promedio));
        writer.flush();
        writer.close();
    }
}