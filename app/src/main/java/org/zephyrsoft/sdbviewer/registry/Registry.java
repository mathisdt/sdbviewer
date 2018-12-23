package org.zephyrsoft.sdbviewer.registry;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic (service) registry. After evaluating some DI frameworks for Android,
 * it became clear that those don't offer any benefit for such a small app.
 */
public class Registry {

    private static final Map<Class<?>, Object> registrants = new HashMap<>();

    public static <T> T get(Class<T> key) {
        if (!registrants.containsKey(key)) {
            throw new IllegalStateException("nothing registered for class " + key.getName());
        } else if (!key.equals(registrants.get(key).getClass())) {
            throw new IllegalStateException("wrong instance registered for class " + key.getName()
                + " - instance is of type " + registrants.get(key).getClass().getName());
        } else {
            // noinspection unchecked
            return (T) registrants.get(key);
        }
    }

    public static void register(Class<?> key, Object value) {
        registrants.put(key, value);
    }
}
