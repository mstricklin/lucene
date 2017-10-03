// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class XElement implements Iterable<Map.Entry<String, Object>> {
    protected XElement(final Store s, final String id) {
        this.store = s;
        this.id = id;
    }

    public Set<String> getPropertyKeys() {
        return newHashSet(properties.keySet());
    }
    void setProperty(String key, Object value) throws IOException {
        properties.put(key, value);
        store.index(this);
    }
    <T> T getProperty(String key) {
        return (T)properties.get(key);
    }
    <T> T removeProperty(String key) {
        return (T)properties.remove(key);
    }
    String getId() {
        return id;
    }
    @Override
    public Iterator<Map.Entry<String, Object>> iterator() {
        return properties.entrySet().iterator();
    }
    // =================================
    public final Store store;
    public final String id;
    private final Map<String, Object> properties = newHashMap();


}
