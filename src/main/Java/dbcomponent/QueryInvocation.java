package main.Java.dbcomponent;

import java.util.List;
import java.util.Objects;

/**
 * Una query predefinida por clave mas sus argumentos (placeholders {@code ?} en el SQL del repositorio).
 */
public final class QueryInvocation {
    private final String key;
    private final List<Object> params;

    private QueryInvocation(String key, List<Object> params) {
        this.key = Objects.requireNonNull(key, "key");
        this.params = params != null ? List.copyOf(params) : List.of();
    }

    public static QueryInvocation of(String key) {
        return new QueryInvocation(key, List.of());
    }

    public static QueryInvocation of(String key, List<Object> params) {
        return new QueryInvocation(key, params);
    }

    public String getKey() {
        return key;
    }

    public List<Object> getParams() {
        return params;
    }
}
