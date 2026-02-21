package utils;

import config.Configuracion;
import java.sql.*;

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
        try (Statement stmt = conn.createStatement()) {
            stmt.executeQuery(query);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public static void cerrarConexion(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) { }
        }
    }
}