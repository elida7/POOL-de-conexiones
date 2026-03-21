package main.Java.dbcomponent.queries;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Repositorio de queries predefinidas cargadas desde .properties.
 * Convencion: las claves deben venir con prefijo "query.".
 */
public class PropertiesQueryRepository implements QueryRepository {
    private static final String QUERY_PREFIX = "query.";
    private final Properties properties;

    public PropertiesQueryRepository(String propertiesFile) throws IOException {
        this.properties = new Properties();
        loadProperties(propertiesFile);
    }

    @Override
    public String getByKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Query key no puede ser vacia");
        }

        String query = properties.getProperty(QUERY_PREFIX + key);
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("No existe query predefinida para key: " + key);
        }
        // Centralizar queries aqui evita exponer SQL libre en la API publica.
        return query;
    }

    private void loadProperties(String file) throws IOException {
        try {
            // Permite ejecutar desde IDE tomando archivo local.
            properties.load(new FileInputStream(file));
        } catch (IOException e) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(file)) {
                if (is == null) {
                    throw e;
                }
                // Permite ejecutar empaquetado leyendo desde resources.
                properties.load(is);
            }
        }
    }
}
