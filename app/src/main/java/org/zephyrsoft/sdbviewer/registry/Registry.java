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
        if (registrants.containsKey(key) && key.getClass().equals(registrants.get(key).getClass())) {
            return (T) registrants.get(key);
        } else {
            throw new IllegalStateException("nothing registered for class " + key.getName());
        }
    }

    public static void register(Class<?> key, Object value) {
        registrants.put(key, value);
    }

    public static void deregister(Class<?> key) {
        registrants.remove(key);
    }
}
