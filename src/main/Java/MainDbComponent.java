package main.Java;

import java.sql.SQLException;
import java.util.List;

import main.Java.dbcomponent.DbComponent;
import main.Java.dbcomponent.adapters.IDAdapter;
import main.Java.dbcomponent.adapters.PostgresAdapter;
import main.Java.dbcomponent.adapters.SqlAdapter;
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
            // Seleccion rapida del motor para demostrar "2 bases de datos".
            // Uso:
            //   - Sin args -> postgres
            //   - args[0] = "postgres" | "h2"
            String engine = (args != null && args.length > 0 && args[0] != null) ? args[0].trim().toLowerCase() : "postgres";

            ConnectionConfig config;
            IDAdapter adapter;
            if ("h2".equals(engine)) {
                // H2 en memoria: no requiere instalar servidor ni crear base.
                config = new ConnectionConfig(
                    "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
                    "sa",
                    "",
                    "org.h2.Driver"
                );
                adapter = new SqlAdapter();
            } else {
                // Postgres (modo original).
                config = new ConnectionConfig(
                    "jdbc:postgresql://localhost:5432/simulacion_db",
                    "postgres",
                    "27418291",
                    "org.postgresql.Driver"
                );
                adapter = new PostgresAdapter();
            }

            QueryRepository repository = new PropertiesQueryRepository("dbcomponent-queries.properties");

            dbComponent = new DbComponent(adapter, config, 1, 5, repository);

            boolean healthOk = dbComponent.query("health");
            System.out.println("engine=" + engine + " | query('health') => " + healthOk);

            boolean txOk = dbComponent.transaction(List.of("health", "health"));
            System.out.println("engine=" + engine + " | transaction(['health','health']) => " + txOk);

            // Sirve para evidenciar reciclado de conexiones del pool.
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
