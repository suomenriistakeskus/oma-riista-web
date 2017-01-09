package fi.riista.util.i18n;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Properties;

public class LinkedProperties extends Properties {
    private final LinkedHashSet<Object> keys = new LinkedHashSet<>();

    @Override
    public Enumeration<?> propertyNames() {
        return Collections.enumeration(keys);
    }

    @Override
    public synchronized Enumeration<Object> elements() {
        return Collections.enumeration(keys);
    }

    public LinkedHashSet<Object> orderedKeys() {
        return keys;
    }

    @Override
    public Enumeration<Object> keys() {
        return Collections.enumeration(keys);
    }

    @Override
    public Object put(Object key, Object value) {
        keys.add(key);
        return super.put(key, value);
    }

    @Override
    public synchronized Object remove(Object key) {
        keys.remove(key);
        return super.remove(key);
    }

    @Override
    public synchronized void clear() {
        keys.clear();
        super.clear();
    }
}
