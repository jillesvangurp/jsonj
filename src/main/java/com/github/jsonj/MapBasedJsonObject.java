package com.github.jsonj;

import org.apache.commons.lang3.Validate;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.github.jsonj.tools.JsonBuilder.fromObject;
import static com.github.jsonj.tools.JsonBuilder.nullValue;

/**
 * This implementation of JsonObject uses a LinkedHashMap. This uses more memory but tends to be faster for objects with a large number of keys.
 *
 * Since this is relatively rare, the parser currently automatically uses this implementation if the number of keys exceeds a
 * configurable threshold (default for this is 100).
 *
 */
public class MapBasedJsonObject extends JsonObject {
    private static final long serialVersionUID = 8208686487292876195L;

    private final Map<String, JsonElement> map;

    public MapBasedJsonObject() {
        this(()->new LinkedHashMap<>());
    }

    @Override
    protected JsonObject createNew() {
        return new MapBasedJsonObject();
    }

    public MapBasedJsonObject(Supplier<Map<String,JsonElement>> mapSupplier) {
        map = mapSupplier.get();
    }

    @SuppressWarnings("rawtypes")
    public MapBasedJsonObject(Map existing) {
        this(existing, () -> new LinkedHashMap<>(),false);
    }
    @SuppressWarnings("rawtypes")
    public MapBasedJsonObject(Map existing, Supplier<Map<String,JsonElement>> mapSupplier) {
        this(existing, mapSupplier, false);
    }

    @SuppressWarnings("rawtypes")
    public MapBasedJsonObject(Map existing, Supplier<Map<String,JsonElement>> mapSupplier, boolean immutable) {
        Map<String, JsonElement> newMap = mapSupplier.get();
        Iterator iterator = existing.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            newMap.put(entry.getKey().toString(), fromObject(entry.getValue()));
        }
        if(immutable) {
            map=Collections.unmodifiableMap(newMap);
        } else {
            map=newMap;
        }
    }

    @Override
    public JsonElement put(String key, JsonElement value) {
        Validate.notNull(key);
        if (value == null) {
            value = nullValue();
        }
        return map.put(key, value);
    }

    @Override
    public JsonObject asObject() {
        return this;
    }

    @Override
    public JsonElement get(Object key) {
        if (key != null && key instanceof String) {
            return map.get(key.toString());
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public JsonObject immutableClone() {
        return new MapBasedJsonObject(this, () -> new LinkedHashMap<>(), true);
    }

    @Override
    public boolean isMutable() {
        return !map.getClass().getName().contains("UnmodifiableMap");
    }

    @Override
    public Set<Entry<String, JsonElement>> entrySet() {
        return map.entrySet();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public JsonElement remove(Object key) {
        if (key != null && key instanceof String) {
            return map.remove(key.toString());
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Collection<JsonElement> values() {
        return map.values();
    }

    @Override
    public MapBasedJsonObject toMapBasedJsonObject() {
        return this;
    }
}
