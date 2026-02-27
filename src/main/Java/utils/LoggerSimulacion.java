package main.Java.utils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import main.Java.modelos.Muestra;

public class LoggerSimulacion {
    private PrintWriter writer;
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public LoggerSimulacion(String tipo) throws IOException {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        writer = new PrintWriter(new FileWriter("log_" + tipo + "_" + fecha + ".log"));
        writer.println("# Log de muestras - [hora] Hilo N: EXITOSA|FALLIDA");
        writer.flush();
    }

    /** Registra cada muestra con hora y estado (exitosa/fallida). Thread-safe. */
    public synchronized void logMuestra(int idHilo, String estado) {
        String hora = LocalDateTime.now().format(FORMATO_HORA);
        writer.println("[" + hora + "] Hilo " + idHilo + ": " + estado);
        writer.flush();
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

    public void logMetricasPeticiones(String tipo, long tiempoTotal, int peticionesExitosas, boolean huboError) {
        writer.println("\n=== METRICAS (modo continuo) ===");
        writer.println("Tipo: " + tipo);
        writer.println("Tiempo: " + tiempoTotal + " ms");
        writer.println("Peticiones exitosas: " + peticionesExitosas);
        writer.println("Detenido por: " + (huboError ? "Error" : "Usuario manual"));
        writer.flush();
        writer.close();
    }
}