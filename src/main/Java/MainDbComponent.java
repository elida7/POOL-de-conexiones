package main.Java;

import java.sql.SQLException;
import java.util.List;

import main.Java.dbcomponent.DbComponent;
import main.Java.dbcomponent.adapters.IDAdapter;
import main.Java.dbcomponent.adapters.PostgresAdapter;
import main.Java.dbcomponent.core.ConnectionConfig;
import main.Java.dbcomponent.queries.PropertiesQueryRepository;
import main.Java.dbcomponent.queries.QueryRepository;

/**
 * Demo minima de uso de DbComponent sin simulador ni hilos.
 */
public class MainDbComponent {
    public static void main(String[] args) {
        DbComponent dbComponent = null;
        try {
            ConnectionConfig config = new ConnectionConfig(
                "jdbc:postgresql://localhost:5432/simulacion_db",
                "postgres",
                "27418291",
                "org.postgresql.Driver"
            );

            IDAdapter adapter = new PostgresAdapter();
            QueryRepository repository = new PropertiesQueryRepository("dbcomponent-queries.properties");

            dbComponent = new DbComponent(adapter, config, 1, 5, repository);

            boolean healthOk = dbComponent.query("health");
            System.out.println("query('health') => " + healthOk);

            boolean txOk = dbComponent.transaction(List.of("health", "health"));
            System.out.println("transaction(['health','health']) => " + txOk);

            System.out.println("Conexiones creadas por el pool: " + dbComponent.getTotalConnectionsCreated());
        } catch (SQLException e) {
            System.err.println("Error SQL en demo DbComponent: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error general en demo DbComponent: " + e.getMessage());
        } finally {
            if (dbComponent != null) {
                dbComponent.close();
            }
        }
    }
}
