package main.Java.utils;

import java.sql.*;

import main.Java.config.Configuracion;

public class DatabaseUtil {
    
    public static Connection getConnection(Configuracion config) throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(
                config.getDbConnectionString(),
                config.getDbUser(),
                config.getDbPassword()
            );
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver no encontrado");
        }
    }
    
    public static boolean ejecutarQuery(Connection conn, String query) {
        return ejecutarQuery(conn, query, false);
    }

    public static boolean ejecutarQuery(Connection conn, String query, boolean imprimir) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeQuery(query);
            if (imprimir) System.out.println("Query ejecutada: " + query);
            return true;
        } catch (SQLException e) {
            System.err.println("ERROR SQL: " + e.getMessage());
            if (imprimir) e.printStackTrace();
            return false;
        }
    }
    
    public static void cerrarConexion(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) { }
        }
    }
}