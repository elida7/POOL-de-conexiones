package main.Java.modelos;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Muestra {
    private String id;
    private int numero;
    private LocalDateTime timestamp;
    private boolean exitosa;
    private long tiempoEjecucion;
    private int intentos;
    private String error;
    
    public Muestra(int numero) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.numero = numero;
        this.timestamp = LocalDateTime.now();
        this.intentos = 0;
    }
    
    public void incrementarIntentos() { intentos++; }
    public String getId() { return id; }
    public int getNumero() { return numero; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isExitosa() { return exitosa; }
    public void setExitosa(boolean exitosa) { this.exitosa = exitosa; }
    public long getTiempoEjecucion() { return tiempoEjecucion; }
    public void setTiempoEjecucion(long t) { this.tiempoEjecucion = t; }
    public int getIntentos() { return intentos; }
    public void setError(String error) { this.error = error; }
    
    @Override
    public String toString() {
        return String.format("[%s] #%d ID:%s %s intentos:%d tiempo:%dms %s",
            timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")),
            numero, id, exitosa ? "EXITOSA" : "FALLIDA",
            intentos, tiempoEjecucion,
            error == null ? "" : "Error:" + error);
    }
}