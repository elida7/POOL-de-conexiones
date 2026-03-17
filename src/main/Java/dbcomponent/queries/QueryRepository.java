package main.Java.dbcomponent.queries;

/**
 * Fuente de queries predefinidas identificadas por clave.
 */
public interface QueryRepository {
    String getByKey(String key);
}
