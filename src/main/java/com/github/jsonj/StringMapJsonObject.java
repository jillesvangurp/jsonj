/**
 * Copyright (c) 2011, Jilles van Gurp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.jsonj;

import static com.github.jsonj.tools.JsonBuilder.fromObject;
import static com.github.jsonj.tools.JsonBuilder.nullValue;
import static com.github.jsonj.tools.JsonBuilder.primitive;

import com.github.jsonj.exceptions.JsonTypeMismatchException;
import com.github.jsonj.tools.JsonBuilder;
import com.github.jsonj.tools.JsonParser;
import com.github.jsonj.tools.JsonSerializer;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.Validate;

/**
 * Representation of json objects.
 */
public class StringMapJsonObject extends JsonObject {
    private static final long serialVersionUID = 497820087656073803L;

    // use during serialization
    private static JsonParser parser = null;

    private final SimpleStringKeyMap<JsonElement> simpleMap = new SimpleStringKeyMap<>();

    private String idField = null;

    public StringMapJsonObject() {
    }

    @SuppressWarnings("rawtypes")
    public StringMapJsonObject(@Nonnull Map existing) {
        Iterator iterator = existing.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            put(entry.getKey().toString(), fromObject(entry.getValue()));
        }
    }

    @Override
    public JsonType type() {
        return JsonType.object;
    }

    /**
     * By default, the hash code is calculated recursively, which can be rather expensive. Calling this method allows
     * you to specify a special field that will be used for calculating this object's hashcode. In case the field value
     * is null it will fall back to recursive behavior.
     *
     * @param fieldName
     *            name of the field value that should be used for calculating the hash code
     */
    @Override
    public void useIdHashCodeStrategy(@Nonnull String fieldName) {
        idField = fieldName.intern();
    }

    @Override
    public StringMapJsonObject asObject() {
        return this;
    }

    @Override
    public JsonArray asArray() {
        throw new JsonTypeMismatchException("not an array");
    }

    @Override
    public JsonSet asSet() {
        throw new JsonTypeMismatchException("not an array");
    }

    @Override
    public JsonPrimitive asPrimitive() {
        throw new JsonTypeMismatchException("not a primitive");
    }

    @Override
    public float asFloat() {
        throw new JsonTypeMismatchException("not a primitive");
    }

    @Override
    public double asDouble() {
        throw new JsonTypeMismatchException("not a primitive");
    }

    @Override
    public int asInt() {
        throw new JsonTypeMismatchException("not a primitive");
    }

    @Override
    public long asLong() {
        throw new JsonTypeMismatchException("not a primitive");
    }

    @Override
    public boolean asBoolean() {
        throw new JsonTypeMismatchException("not a primitive");
    }

    @Override
    public String asString() {
        throw new JsonTypeMismatchException("not a primitive");
    }

    @Override
    public String toString() {
        return JsonSerializer.serialize(this, false);
    }

    @Override
    public void serialize(Writer w) throws IOException {
        w.append(JsonSerializer.OPEN_BRACE);

        Iterator<Entry<String, JsonElement>> iterator = simpleMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, JsonElement> entry = iterator.next();
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            w.append(JsonSerializer.QUOTE);
            w.append(JsonSerializer.jsonEscape(key));
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
    public @Nonnull String prettyPrint() {
        return JsonSerializer.serialize(this, true);
    }

    @Override
    public boolean isObject() {
        return true;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public <T extends JsonDataObject> JsonElement put(@Nonnull String key, @Nonnull T object) {
        return put(key, object.getJsonObject());
    }

    @Override
    public JsonElement put(String key, JsonElement value) {
        Validate.notNull(key);
        if (value == null) {
            value = nullValue();
        }
        return simpleMap.put(key, value);
    }

    /**
     * Variant of put that can take a Object instead of a primitive. The normal put inherited from LinkedHashMap only
     * takes JsonElement instances.
     *
     * @param key
     *            label
     * @param value
     *            any object that is accepted by the JsonPrimitive constructor.
     * @return the JsonElement that was added.
     * @throws JsonTypeMismatchException
     *             if the value cannot be turned into a primitive.
     */
    @Override
    public JsonElement put(@Nonnull String key, Object value) {
        if(value instanceof JsonDataObject) {
            return put(key,((JsonDataObject) value).getJsonObject());
        } else if(value instanceof Optional<?>) {
            Optional<?> maybeValue = (Optional<?>)value;
            if(maybeValue.isPresent()) {
                return put(key,maybeValue.get());
            } else {
                return put(key,JsonBuilder.nullValue());
            }
        } else if(value instanceof JsonElement) {
            // can happen when handling cast, non generic objects
            return put(key, (JsonElement)value);
        } else {
            return put(key, primitive(value));
        }
    }

    @Override
    public JsonElement put(@Nonnull String key, JsonBuilder value) {
        return put(key, value.get());
    }

    @Override
    public void putAll(Map<? extends String, ? extends JsonElement> m) {
        for (Entry<? extends String, ? extends JsonElement> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * Add multiple fields to the object.
     *
     * @param es
     *            field entries
     */
    @Override
    public void add(@SuppressWarnings("unchecked") @Nonnull Entry<String, JsonElement>... es) {
        for (Map.Entry<String, JsonElement> e : es) {
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * Allows you to get the nth entry in the JsonObject. Please note that this method iterates over all the entries
     * until it finds the nth, so getting the last element is probably going to be somewhat expensive, depending on the
     * size of the collection. Also note that the entries in JsonObject are ordered by the order of insertion (it is a
     * LinkedHashMap).
     *
     * @param index
     *            index of the entry
     * @return the nth entry in the JsonObject.
     */
    @Override
    public Entry<String, JsonElement> get(int index) {
        if (index >= size()) {
            throw new IllegalArgumentException("index out of range");
        } else {
            int i = 0;
            for (Entry<String, JsonElement> e : entrySet()) {
                if (i++ == index) {
                    return e;
                }
            }
        }
        return null;
    }

    /**
     * @return the first entry in the object.
     */
    @Override
    public Entry<String, JsonElement> first() {
        return get(0);
    }

    @Override
    public JsonElement get(Object key) {
        if (key != null && key instanceof String) {
            return simpleMap.get(key.toString());
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public MapBasedJsonObject toMapBasedJsonObject() {
        return new MapBasedJsonObject(this);
    }

    /**
     * Get a json element at a particular path in an object structure.
     *
     * @param labels
     *            list of field names that describe the location to a particular json node.
     * @return a json element at a particular path in an object or null if it can't be found.
     */
    @Override
    public JsonElement get(final String... labels) {
        JsonElement e = this;
        int n = 0;
        for (String label : labels) {
            e = e.asObject().get(label);
            if (e == null) {
                return null;
            }
            if (n == labels.length - 1) {
                return e;
            }
            if (!e.isObject()) {
                break;
            }
            n++;
        }
        return null;
    }

    @Override
    public Optional<JsonElement> maybeGet(String...labels) {
        return Optional.ofNullable(get(labels));
    }

    @Override
    public Optional<String> maybeGetString(String...labels) {
        return maybeGet(labels).map(e -> e.asString());
    }

    @Override
    public Optional<Integer> maybeGetInt(String...labels) {
        return maybeGet(labels).map(e -> e.asInt());
    }

    @Override
    public Optional<Long> maybeGetLong(String...labels) {
        return maybeGet(labels).map(e -> e.asLong());
    }

    @Override
    public Optional<Number> maybeGetNumber(String...labels) {
        return maybeGet(labels).map(e -> e.asNumber());
    }

    @Override
    public Optional<Boolean> maybeGetBoolean(String...labels) {
        return maybeGet(labels).map(e -> e.asBoolean());
    }

    @Override
    public Optional<JsonArray> maybeGetArray(String...labels) {
        return maybeGet(labels).map(e -> e.asArray());
    }

    @Override
    public Optional<JsonSet> maybeGetSet(String...labels) {
        return maybeGet(labels).map(e -> e.asSet());
    }

    @Override
    public Optional<JsonObject> maybeGetObject(String...labels) {
        return maybeGet(labels).map(e -> e.asObject());
    }

    /**
     * Get a value at a particular path in an object structure.
     *
     * @param labels
     *            one or more text labels
     * @return value or null if it doesn't exist at the specified path
     */
    @Override
    public String getString(final String... labels) {
        JsonElement jsonElement = get(labels);
        if (jsonElement == null || jsonElement.isNull()) {
            return null;
        } else {
            return jsonElement.asString();
        }
    }

    /**
     * Get a value at a particular path in an object structure.
     *
     * @param labels
     *            one or more text labels
     * @return value or null if it doesn't exist at the specified path
     */
    @Override
    public Boolean getBoolean(final String... labels) {
        JsonElement jsonElement = get(labels);
        if (jsonElement == null || jsonElement.isNull()) {
            return null;
        } else {
            if (jsonElement.isBoolean()) {
                return jsonElement.asBoolean();
            } else if (jsonElement.isNumber()) {
                return jsonElement.asInt() > 0;
            } else if (jsonElement.isPrimitive()) {
                return Boolean.valueOf(jsonElement.asString());
            } else {
                throw new JsonTypeMismatchException("expected primitive value but was " + jsonElement.type());
            }
        }
    }

    /**
     * @param field
     *            name of the field
     * @param defaultValue
     *            default value that is returned if the field has no value
     * @return value of the field as a boolean
     */
    @Override
    public boolean get(@Nonnull String field, boolean defaultValue) {
        JsonElement e = get(field);
        if (e == null) {
            return defaultValue;
        } else {
            if (e.isBoolean()) {
                return e.asBoolean();
            } else if (e.isNumber()) {
                return e.asInt() > 0;
            } else if (e.isPrimitive()) {
                return Boolean.valueOf(e.asString());
            } else {
                throw new JsonTypeMismatchException("expected primitive value but was " + e.type());
            }
        }
    }

    /**
     * Get a value at a particular path in an object structure.
     *
     * @param labels
     *            one or more text labels
     * @return value or null if it doesn't exist at the specified path
     */
    @Override
    public Integer getInt(String... labels) {
        JsonElement jsonElement = get(labels);
        if (jsonElement == null || jsonElement.isNull()) {
            return null;
        } else {
            return jsonElement.asInt();
        }
    }

    /**
     * @param field
     *            name of the field
     * @param defaultValue
     *            default value that is returned if the field has no value
     * @return value of the field as an int
     */
    @Override
    public int get(@Nonnull String field, int defaultValue) {
        JsonElement e = get(field);
        if (e == null) {
            return defaultValue;
        } else {
            return e.asInt();
        }
    }

    /**
     * Get a value at a particular path in an object structure.
     *
     * @param labels
     *            one or more text labels
     * @return value or null if it doesn't exist at the specified path
     */
    @Override
    public Long getLong(String... labels) {
        JsonElement jsonElement = get(labels);
        if (jsonElement == null || jsonElement.isNull()) {
            return null;
        } else {
            return jsonElement.asLong();
        }
    }

    /**
     * @param field
     *            name of the field
     * @param defaultValue
     *            default value that is returned if the field has no value
     * @return value of the field as a long
     */
    @Override
    public long get(@Nonnull  String field, long defaultValue) {
        JsonElement e = get(field);
        if (e == null) {
            return defaultValue;
        } else {
            return e.asLong();
        }
    }

    /**
     * Get a value at a particular path in an object structure.
     *
     * @param labels
     *            one or more text labels
     * @return value or null if it doesn't exist at the specified path
     */
    @Override
    public Float getFloat(String... labels) {
        JsonElement jsonElement = get(labels);
        if (jsonElement == null || jsonElement.isNull()) {
            return null;
        } else {
            return jsonElement.asFloat();
        }
    }

    /**
     * @param field
     *            name of the field
     * @param defaultValue
     *            default value that is returned if the field has no value
     * @return value of the field as a float
     */
    @Override
    public float get(@Nonnull  String field, float defaultValue) {
        JsonElement e = get(field);
        if (e == null) {
            return defaultValue;
        } else {
            return e.asFloat();
        }
    }

    /**
     * Get a value at a particular path in an object structure.
     *
     * @param labels
     *            one or more text labels
     * @return value or null if it doesn't exist at the specified path
     */
    @Override
    public Double getDouble(String... labels) {
        JsonElement jsonElement = get(labels);
        if (jsonElement == null || jsonElement.isNull()) {
            return null;
        } else {
            return jsonElement.asDouble();
        }
    }

    /**
     * @param field
     *            name of the field
     * @param defaultValue
     *            default value that is returned if the field has no value
     * @return value of the field as a double
     */
    @Override
    public double get(@Nonnull String field, double defaultValue) {
        JsonElement e = get(field);
        if (e == null) {
            return defaultValue;
        } else {
            return e.asDouble();
        }
    }

    /**
     * Get a JsonObject at a particular path in an object structure.
     *
     * @param labels
     *            one or more text labels
     * @return value or null if it doesn't exist at the specified path
     */
    @Override
    public JsonObject getObject(String... labels) {
        JsonElement jsonElement = get(labels);
        if (jsonElement == null || jsonElement.isNull()) {
            return null;
        } else {
            return jsonElement.asObject();
        }
    }

    /**
     * Get a JsonArray at a particular path in an object structure.
     *
     * @param labels
     *            one or more text labels
     * @return value or null if it doesn't exist at the specified path
     */
    @Override
    public JsonArray getArray(String... labels) {
        JsonElement jsonElement = get(labels);
        if (jsonElement == null || jsonElement.isNull()) {
            return null;
        } else {
            return jsonElement.asArray();
        }
    }

    /**
     * Get or create a JsonArray at a particular path in an object structure. Any object on the path will be created as
     * well if missing.
     *
     * @param labels
     *            one or more text labels
     * @return the created JsonArray
     * @throws JsonTypeMismatchException
     *             if an element is present at the path that is not a JsonArray
     */
    @Override
    public @Nonnull JsonArray getOrCreateArray(@Nonnull String... labels) {
        JsonObject parent = this;
        JsonElement decendent = null;
        int index = 0;
        for (String label : labels) {
            decendent = parent.get(label);
            if (decendent == null && index < labels.length - 1 && parent.isObject()) {
                decendent = new StringMapJsonObject();
                parent.put(label, decendent);
            } else if (index == labels.length - 1) {
                if (decendent == null) {
                    decendent = new JsonArray();
                    parent.put(label, decendent);
                    return decendent.asArray();
                } else {
                    return decendent.asArray();
                }
            }
            if(decendent == null) {
                throw new IllegalStateException("decendant should not be null here");
            }
            parent = decendent.asObject();
            index++;
        }
        throw new IllegalStateException("element not found or created");
    }

    /**
     * Extracts or creates and adds a set at the specied path. Any JsonArrays are converted to sets and updated in the
     * JsonObject as well.
     *
     * @param labels
     *            path to the set in the JsonObject
     * @return the set
     * @throws JsonTypeMismatchException
     *             if an element is present at the path that is not a JsonArray or JsonSet
     */
    @Override
    public JsonSet getOrCreateSet(String... labels) {
        JsonObject parent = this;
        JsonElement decendent;
        int index = 0;
        for (String label : labels) {
            decendent = parent.get(label);
            if (decendent == null && index < labels.length - 1 && parent.isObject()) {
                decendent = new StringMapJsonObject();
                parent.put(label, decendent);
            } else if (index == labels.length - 1) {
                if (decendent == null) {
                    decendent = new JsonSet();
                    parent.put(label, decendent);
                    return decendent.asSet();
                } else {
                    JsonSet set = decendent.asSet();
                    if (!(decendent instanceof JsonSet)) {
                        // if it wasn't a set update it
                        parent.put(label, set);
                    }
                    return set;
                }
            }
            if(decendent == null) {
                throw new IllegalStateException("decendant should not be null here");
            }
            parent = decendent.asObject();
            index++;
        }
        return null;
    }

    /**
     * Get or create a JsonObject at a particular path in an object structure. Any object on the path will be created as
     * well if missing.
     *
     * @param labels
     *            one or more text labels
     * @return the created JsonObject
     * @throws JsonTypeMismatchException
     *             if an element is present at the path that is not a JsonObject
     */
    @Override
    public JsonObject getOrCreateObject(String... labels) {
        JsonObject parent = this;
        JsonElement decendent;
        int index = 0;
        for (String label : labels) {
            decendent = parent.get(label);
            if (decendent == null && index < labels.length - 1 && parent.isObject()) {
                decendent = new StringMapJsonObject();
                parent.put(label, decendent);
            } else if (index == labels.length - 1) {
                if (decendent == null) {
                    decendent = new StringMapJsonObject();
                    parent.put(label, decendent);
                    return decendent.asObject();
                } else {
                    return decendent.asObject();
                }
            }
            if(decendent == null) {
                throw new IllegalStateException("decendant should not be null here");
            }
            parent = decendent.asObject();
            index++;
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof StringMapJsonObject)) {
            return false;
        }
        StringMapJsonObject object = (StringMapJsonObject) o;
        if (object.entrySet().size() != entrySet().size()) {
            return false;
        }
        Set<Entry<String, JsonElement>> es = entrySet();
        for (Entry<String, JsonElement> entry : es) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (!value.equals(object.get(key))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (idField != null) {
            JsonElement jsonElement = get(idField);
            if (jsonElement != null) {
                return jsonElement.hashCode();
            }
        }
        int hashCode = 23;
        Set<Entry<String, JsonElement>> entrySet = entrySet();
        for (Entry<String, JsonElement> entry : entrySet) {
            JsonElement value = entry.getValue();
            if (value != null) { // skip null entries
                hashCode = hashCode * entry.getKey().hashCode() * value.hashCode();
            }
        }
        return hashCode;
    }

    @Override
    public Object clone() {
        return deepClone();
    }

    @SuppressWarnings("unchecked")
    @Override
    public StringMapJsonObject deepClone() {
        StringMapJsonObject object = new StringMapJsonObject();
        Set<java.util.Map.Entry<String, JsonElement>> es = entrySet();
        for (Entry<String, JsonElement> entry : es) {
            JsonElement e = entry.getValue().deepClone();
            object.put(entry.getKey(), e);
        }
        return object;
    }

    @Override
    public StringMapJsonObject immutableClone() {
        StringMapJsonObject object = new StringMapJsonObject();
        Set<java.util.Map.Entry<String, JsonElement>> es = entrySet();
        for (Entry<String, JsonElement> entry : es) {
            JsonElement e = entry.getValue().immutableClone();
            object.put(entry.getKey(), e);
        }
        object.simpleMap.makeImmutable();
        return object;
    }

    @Override
    public boolean isMutable() {
        return simpleMap.isMutable();
    }

    @Override
    public boolean isEmpty() {
        return keySet().size() == 0;
    }

    @Override
    public void removeEmpty() {
        Iterator<java.util.Map.Entry<String, JsonElement>> iterator = entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonElement> entry = iterator.next();
            JsonElement element = entry.getValue();
            if (element.isEmpty() && !element.isObject()) {
                iterator.remove();
            } else {
                element.removeEmpty();
            }
        }
    }

    @Override
    public boolean isNumber() {
        return false;
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean isString() {
        return false;
    }

    @Override
    public void clear() {
        simpleMap.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return simpleMap.containsValue(value);
    }

    @Override
    public Set<Entry<String, JsonElement>> entrySet() {
        final Set<Entry<String, JsonElement>> entrySet = simpleMap.entrySet();
        return new Set<Map.Entry<String, JsonElement>>() {

            @Override
            public boolean add(java.util.Map.Entry<String, JsonElement> e) {
                throw new UnsupportedOperationException("entry set is immutable");
            }

            @Override
            public boolean addAll(Collection<? extends java.util.Map.Entry<String, JsonElement>> c) {
                throw new UnsupportedOperationException("entry set is immutable");
            }

            @Override
            public void clear() {
                throw new UnsupportedOperationException("entry set is immutable");
            }

            @Override
            public boolean contains(Object o) {
                throw new UnsupportedOperationException("not supported");
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                throw new UnsupportedOperationException("not supported");
            }

            @Override
            public boolean isEmpty() {
                return entrySet.isEmpty();
            }

            @Override
            public Iterator<Entry<String, JsonElement>> iterator() {
                return new Iterator<Entry<String, JsonElement>>() {
                    private final Iterator<Entry<String, JsonElement>> it = entrySet.iterator();

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public Entry<String, JsonElement> next() {
                        final Entry<String, JsonElement> next = it.next();
                        return new Entry<String, JsonElement>() {

                            @Override
                            public String getKey() {
                                return next.getKey();
                            }

                            @Override
                            public JsonElement getValue() {
                                return next.getValue();
                            }

                            @Override
                            public JsonElement setValue(JsonElement value) {
                                throw new UnsupportedOperationException("immutable entry");
                            }
                        };
                    }

                    @Override
                    public void remove() {
                        it.remove();
                    }
                };
            }

            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException("entry set is immutable");
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                throw new UnsupportedOperationException("entry set is immutable");
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                throw new UnsupportedOperationException("entry set is immutable");
            }

            @Override
            public int size() {
                return entrySet.size();
            }

            @Override
            public Object[] toArray() {
                @SuppressWarnings("unchecked")
                Entry<String, JsonElement>[] result = new Entry[entrySet.size()];
                int i = 0;
                for (final Entry<String, JsonElement> e : entrySet) {
                    result[i] = new Entry<String, JsonElement>() {

                        @Override
                        public String getKey() {
                            return e.getKey();
                        }

                        @Override
                        public JsonElement getValue() {
                            return e.getValue();
                        }

                        @Override
                        public JsonElement setValue(JsonElement value) {
                            throw new UnsupportedOperationException("immutable");
                        }
                    };
                    i++;
                }
                return result;
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> T[] toArray(T[] a) {
                return (T[]) toArray();
            }
        };
    }

    @Override
    public Set<String> keySet() {
        Set<String> keySet = simpleMap.keySet();
        return keySet;
    }

    @Override
    public JsonElement remove(Object key) {
        if (key != null && key instanceof String) {
            return simpleMap.remove(key.toString());
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public int size() {
        return simpleMap.size();
    }

    @Override
    public @Nonnull Collection<JsonElement> values() {
        return simpleMap.values();
    }

    @Override
    public void map(@Nonnull BiFunction<String, JsonElement, JsonElement> f) {
        entrySet().stream().forEach(e -> {
            put(e.getKey(), f.apply(e.getKey(), e.getValue()));
        });
    }

    @Override
    public void forEachString(@Nonnull BiConsumer<String, String> f) {
        forEach((k,v) -> {f.accept(k, v.asString());});
    }

    @Override
    public void mapPrimitiveFieldsRecursively(@Nonnull BiFunction<String, JsonElement, JsonElement> f) {
        map((k,v) -> {
            if(v.isObject()) {
                v.asObject().mapPrimitiveFieldsRecursively(f);
                return v;
            } else if(v.isArray()) {
                mapPrimitiveFieldsRecursively(v.asArray(), f);
                return v;
            } else {
                return f.apply(k, v);
            }
        });
    }

    private void mapPrimitiveFieldsRecursively(@Nonnull JsonArray arr, @Nonnull BiFunction<String, JsonElement, JsonElement> f) {
        for(JsonElement e: arr) {
            if(e.isObject()) {
                e.asObject().mapPrimitiveFieldsRecursively(f);
            } else if(e.isArray()) {
                mapPrimitiveFieldsRecursively(e.asArray(), f);
            } else {
                // ignore
            }
        }
    }

    @Override
    public void forEachPrimitiveRecursive(@Nonnull BiConsumer<String, JsonPrimitive> f) {
        forEach((k,v) -> {
            if(v.isObject()) {
                v.asObject().forEachPrimitiveRecursive(f);
            } else if(v.isArray()) {
                forEachPrimitiveRecursive(v.asArray(), f);
            } else {
                f.accept(k, v.asPrimitive());
            }
        });
    }

    @Override
    public StringMapJsonObject flatten(@Nonnull String separator) {
        StringMapJsonObject o = new StringMapJsonObject();
        flatten(o,"",separator,this);
        return o;
    }

    private static void flatten(@Nonnull StringMapJsonObject root, @Nonnull String path, @Nonnull String separator, JsonElement element) {
        JsonType type = element.type();
        switch (type) {
        case array:
            JsonArray arr = element.asArray();
            for(int i=0; i<arr.size();i++) {
                if(path.length()>0) {
                    flatten(root,path+separator+i,separator,arr.get(i));
                } else {
                    flatten(root,""+i,separator,arr.get(i));
                }
            }
            break;
        case object:
            if(path.length()>0) {
                element.asObject().forEach((key, value) -> flatten(root,path+separator+key,separator,value));
            } else {
                element.asObject().forEach((key, value) -> flatten(root,key,separator,value));
            }

            break;

        default:
            root.put(path, element);
            break;
        }
    }

    private void forEachPrimitiveRecursive(@Nonnull JsonArray arr, @Nonnull BiConsumer<String, JsonPrimitive> f) {
        for(JsonElement e: arr) {
            if(e.isObject()) {
                e.asObject().forEachPrimitiveRecursive(f);
            } else if(e.isArray()) {
                forEachPrimitiveRecursive(e.asArray(), f);
            } else {
                // ignore
            }
        }
    }

    @Override
    void writeObject(java.io.ObjectOutputStream out) throws IOException {
        // when using object serialization, write the json bytes
        byte[] bytes = toString().getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);

    }

    @Override
    void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        // when deserializing, parse the json string
        try {
            int length = in.readInt();
            byte[] buf = new byte[length];
            in.readFully(buf);
            if (parser == null) {
                // create it lazily, static so won't increase object size
                parser = new JsonParser();
            }
            JsonElement o = parser.parse(new String(buf, StandardCharsets.UTF_8));
            Field f = getClass().getDeclaredField("intMap");
            f.setAccessible(true);
            f.set(this, new SimpleIntKeyMap<>());

            for (Entry<String, JsonElement> e : o.asObject().entrySet()) {
                put(e.getKey(), e.getValue());
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
