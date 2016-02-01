package com.github.jsonj;

import static com.github.jsonj.tools.JsonBuilder.fromObject;
import static com.github.jsonj.tools.JsonBuilder.nullValue;

import com.github.jsonj.tools.JsonSerializer;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.apache.commons.lang3.Validate;

/**
 * More traditional Map based implementation of JsonObject
 */
public class MapBasedJsonObject extends JsonObject {
    private static final long serialVersionUID = 8208686487292876195L;

    private final Map<String, JsonElement> map;

    public MapBasedJsonObject() {
        this(()->new LinkedHashMap<>());
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
    public void serialize(Writer w) throws IOException {
        w.append(JsonSerializer.OPEN_BRACE);

        Iterator<Entry<String, JsonElement>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, JsonElement> entry = iterator.next();
            JsonElement value = entry.getValue();
            w.append(JsonSerializer.QUOTE);
            w.append(JsonSerializer.jsonEscape(entry.getKey()));
            w.append(JsonSerializer.QUOTE);
            w.append(JsonSerializer.COLON);
            value.serialize(w);
            if (iterator.hasNext()) {
                w.append(JsonSerializer.COMMA);
            }
        }
        w.append(JsonSerializer.CLOSE_BRACE);
    }

    @Override
    public MapBasedJsonObject toMapBasedJsonObject() {
        return this;
    }
}
