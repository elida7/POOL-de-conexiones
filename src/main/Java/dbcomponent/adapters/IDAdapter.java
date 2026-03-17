package main.Java.dbcomponent.adapters;

import java.sql.SQLException;

import main.Java.dbcomponent.core.ConnectionConfig;
import main.Java.dbcomponent.core.ConnectionProvider;

/**
 * Contrato para desacoplar DbComponent del driver especifico.
 */
public interface IDAdapter {
    ConnectionProvider createProvider(ConnectionConfig config) throws SQLException;
}
