package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuracion {
    private Properties properties;
    private String dbConnectionString;
    private String dbUser;
    private String dbPassword;
    private int cantidadMuestras;
    private int maxReintentos;
    private String query;
    private int maxConnections;
    private int minConnections;
    
    public Configuracion(String archivo) throws IOException {
        properties = new Properties();
        properties.load(new FileInputStream(archivo));
        this.dbConnectionString = properties.getProperty("db.connectionString");
        this.dbUser = properties.getProperty("db.user");
        this.dbPassword = properties.getProperty("db.password");
        this.cantidadMuestras = Integer.parseInt(properties.getProperty("simulacion.cantidadMuestras"));
        this.maxReintentos = Integer.parseInt(properties.getProperty("simulacion.maxReintentos"));
        this.query = properties.getProperty("simulacion.query");
        this.maxConnections = Integer.parseInt(properties.getProperty("pool.maxConnections"));
        this.minConnections = Integer.parseInt(properties.getProperty("pool.minConnections"));
    }
    
    public String getDbConnectionString() { return dbConnectionString; }
    public String getDbUser() { return dbUser; }
    public String getDbPassword() { return dbPassword; }
    public int getCantidadMuestras() { return cantidadMuestras; }
    public int getMaxReintentos() { return maxReintentos; }
    public String getQuery() { return query; }
    public int getMaxConnections() { return maxConnections; }
    public int getMinConnections() { return minConnections; }
}