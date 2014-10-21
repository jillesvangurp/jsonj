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

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.apache.commons.lang.Validate;

import com.github.jsonj.exceptions.JsonTypeMismatchException;
import com.github.jsonj.tools.JsonBuilder;
import com.github.jsonj.tools.JsonParser;
import com.github.jsonj.tools.JsonSerializer;
import com.jillesvangurp.efficientstring.EfficientString;

/**
 * Representation of json objects. This class extends LinkedHashMap and may be used as such. In addition a lot of
 * convenience is provided in the form of methods you are likely to need when working with json objects
 * programmatically.
 */
public class JsonObject implements Map<String, JsonElement>, JsonElement {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final long serialVersionUID = 497820087656073803L;

    // use during serialization
    private static JsonParser parser = null;

    // private final LinkedHashMap<EfficientString, JsonElement> map = new LinkedHashMap<EfficientString,
    // JsonElement>();
//    private final Map<EfficientString, JsonElement> map = new SimpleMap<>();
    private final SimpleIntKeyMap<JsonElement> intMap = new SimpleIntKeyMap<>();

    private String idField = null;

    public JsonObject() {
    }

    @SuppressWarnings("rawtypes")
    public JsonObject(Map existing) {
        super();
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
    public void useIdHashCodeStrategy(String fieldName) {
        idField = fieldName.intern();
    }

    @Override
    public JsonObject asObject() {
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

        Iterator<Entry<Integer, JsonElement>> iterator = intMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Integer, JsonElement> entry = iterator.next();
            EfficientString key = EfficientString.get(entry.getKey());
            JsonElement value = entry.getValue();
            w.append(JsonSerializer.QUOTE);
            w.append(JsonSerializer.jsonEscape(key.toString()));
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
    public String prettyPrint() {
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
    public JsonElement put(String key, Object value) {
        return put(key, primitive(value));
    }

    @Override
    public JsonElement put(String key, JsonElement value) {
        Validate.notNull(key);
        if (value == null) {
            value = nullValue();
        }
        return intMap.put(EfficientString.fromString(key).index(), value);
    }

    public JsonElement put(String key, JsonBuilder value) {
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
    public void add(@SuppressWarnings("unchecked") Entry<String, JsonElement>... es) {
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
    public Entry<String, JsonElement> first() {
        return get(0);
    }

    @Override
    public JsonElement get(Object key) {
        if (key != null && key instanceof String) {
            return intMap.get(EfficientString.fromString(key.toString()).index());
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Get a json element at a particular path in an object structure.
     *
     * @param labels
     *            list of field names that describe the location to a particular json node.
     * @return a json element at a particular path in an object or null if it can't be found.
     */
    public JsonElement get(final String... labels) {
        JsonElement e = this;
        int n = 0;
        for (String label : labels) {
            e = e.asObject().get(label);
            if (e == null) {
                return null;
            }
            if (n == labels.length - 1 && e != null) {
                return e;
            }
            if (!e.isObject()) {
                break;
            }
            n++;
        }
        return null;
    }

    /**
     * Get a value at a particular path in an object structure.
     *
     * @param labels
     *            one or more text labels
     * @return value or null if it doesn't exist at the specified path
     */
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
    public boolean get(final String field, boolean defaultValue) {
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
    public Integer getInt(final String... labels) {
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
    public int get(final String field, int defaultValue) {
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
    public Long getLong(final String... labels) {
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
    public long get(final String field, long defaultValue) {
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
    public Float getFloat(final String... labels) {
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
    public float get(final String field, float defaultValue) {
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
    public Double getDouble(final String... labels) {
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
    public double get(final String field, double defaultValue) {
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
    public JsonObject getObject(final String... labels) {
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
    public JsonArray getArray(final String... labels) {
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
    public JsonArray getOrCreateArray(final String... labels) {
        JsonObject parent = this;
        JsonElement decendent;
        int index = 0;
        for (String label : labels) {
            decendent = parent.get(label);
            if (decendent == null && index < labels.length - 1 && parent.isObject()) {
                decendent = new JsonObject();
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
            parent = decendent.asObject();
            index++;
        }
        return null;
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
    public JsonSet getOrCreateSet(final String... labels) {
        JsonObject parent = this;
        JsonElement decendent;
        int index = 0;
        for (String label : labels) {
            decendent = parent.get(label);
            if (decendent == null && index < labels.length - 1 && parent.isObject()) {
                decendent = new JsonObject();
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
    public JsonObject getOrCreateObject(final String... labels) {
        JsonObject parent = this;
        JsonElement decendent;
        int index = 0;
        for (String label : labels) {
            decendent = parent.get(label);
            if (decendent == null && index < labels.length - 1 && parent.isObject()) {
                decendent = new JsonObject();
                parent.put(label, decendent);
            } else if (index == labels.length - 1) {
                if (decendent == null) {
                    decendent = new JsonObject();
                    parent.put(label, decendent);
                    return decendent.asObject();
                } else {
                    return decendent.asObject();
                }
            }
            parent = decendent.asObject();
            index++;
        }
        return null;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof JsonObject)) {
            return false;
        }
        JsonObject object = (JsonObject) o;
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
    public JsonObject deepClone() {
        JsonObject object = new JsonObject();
        Set<java.util.Map.Entry<String, JsonElement>> es = entrySet();
        for (Entry<String, JsonElement> entry : es) {
            JsonElement e = entry.getValue().deepClone();
            object.put(entry.getKey(), e);
        }
        return object;
    }

    @Override
    public JsonObject immutableClone() {
        JsonObject object = new JsonObject();
        Set<java.util.Map.Entry<String, JsonElement>> es = entrySet();
        for (Entry<String, JsonElement> entry : es) {
            JsonElement e = entry.getValue().immutableClone();
            object.put(entry.getKey(), e);
        }
        object.intMap.makeImmutable();
        return object;
    }


    @Override
    public boolean isEmpty() {
        boolean empty = true;
        if (keySet().size() != 0) {
            for (java.util.Map.Entry<String, JsonElement> entry : entrySet()) {
                empty = empty && entry.getValue().isEmpty();
                if (!empty) {
                    return false;
                }
            }
        }
        return empty;
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
        intMap.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return intMap.containsValue(value);
    }

    @Override
    public Set<Entry<String, JsonElement>> entrySet() {
        final Set<Entry<Integer, JsonElement>> entrySet = intMap.entrySet();
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
                    private final Iterator<Entry<Integer, JsonElement>> it = entrySet.iterator();

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public Entry<String, JsonElement> next() {
                        final Entry<Integer, JsonElement> next = it.next();
                        return new Entry<String, JsonElement>() {

                            @Override
                            public String getKey() {
                                EfficientString es = EfficientString.get(next.getKey());
                                return es.toString();
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
                for (final Entry<Integer, JsonElement> e : entrySet) {
                    result[i] = new Entry<String, JsonElement>() {

                        @Override
                        public String getKey() {
                            EfficientString es = EfficientString.get(e.getKey());
                            return es.toString();
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
        Set<Integer> keySet = intMap.keySet();
        Set<String> keys = new HashSet<String>();
        for (Integer idx : keySet) {
            keys.add(EfficientString.get(idx).toString());
        }
        return keys;
    }

    @Override
    public JsonElement remove(Object key) {
        if (key != null && key instanceof String) {
            return intMap.remove(EfficientString.fromString(key.toString()).index());
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public int size() {
        return intMap.size();
    }

    @Override
    public Collection<JsonElement> values() {
        return intMap.values();
    }

    public Stream<JsonElement> map(BiFunction<String, JsonElement, JsonElement> f) {
        return entrySet().stream().map(e -> f.apply(e.getKey(), e.getValue()));
    }

    public void forEachString(BiConsumer<String, String> f) {
        forEach((k,v) -> {f.accept(k, v.asString());});
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        // when using object serialization, write the json bytes
        byte[] bytes = toString().getBytes(UTF8);
        out.writeInt(bytes.length);
        out.write(bytes);

    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        // when deserializing, parse the json string
        try {
            int length = in.readInt();
            byte[] buf = new byte[length];
            in.readFully(buf);
            if (parser == null) {
                // create it lazily, static so won't increase object size
                parser = new JsonParser();
            }
            JsonElement o = parser.parse(new String(buf, UTF8));
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
