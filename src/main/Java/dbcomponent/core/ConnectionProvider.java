package main.Java.dbcomponent.core;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Abstraccion para desacoplar la obtencion de conexiones del componente.
 */
public interface ConnectionProvider {
    Connection getConnection() throws SQLException;
}
