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

import com.github.jsonj.tools.JsonParser;
import com.github.jsonj.tools.JsonSerializer;
import com.jillesvangurp.efficientstring.EfficientString;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.github.jsonj.tools.JsonBuilder.fromObject;
import static com.github.jsonj.tools.JsonBuilder.nullValue;

/**
 * Representation of json objects. This class extends LinkedHashMap and may be used as such. In addition a lot of
 * convenience is provided in the form of methods you are likely to need when working with json objects
 * programmatically.
 */
public class SimpleIntMapJsonObject extends JsonObject {
    private static final long serialVersionUID = 497820087656073803L;

    // use during object serialization only
    private static JsonParser parser = null;

    // private final LinkedHashMap<EfficientString, JsonElement> map = new LinkedHashMap<EfficientString,
    // JsonElement>();
//    private final Map<EfficientString, JsonElement> map = new SimpleMap<>();
    private final SimpleIntKeyMap<JsonElement> intMap = new SimpleIntKeyMap<>();


    private String idField = null;

    public SimpleIntMapJsonObject() {
    }

    @Override
    protected JsonObject createNew() {
        return new SimpleIntMapJsonObject();
    }

    @SuppressWarnings("rawtypes")
    public SimpleIntMapJsonObject(@Nonnull Map existing) {
        Iterator iterator = existing.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            put(entry.getKey().toString(), fromObject(entry.getValue()));
        }
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
    public JsonElement put(String key, JsonElement value) {
        Validate.notNull(key);
        if (value == null) {
            value = nullValue();
        }
        return intMap.put(EfficientString.fromString(key).index(), value);
    }

    @Override
    public JsonElement get(Object key) {
        if (key != null && key instanceof String) {
            return intMap.get(EfficientString.fromString(key.toString()).index());
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof IJsonObject)) {
            return false;
        }
        IJsonObject object = (IJsonObject) o;
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

    @Override
    public boolean isMutable() {
        return intMap.isMutable();
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
    public @Nonnull Collection<JsonElement> values() {
        return intMap.values();
    }

    @Override
    public JsonObject flatten(@Nonnull String separator) {
        JsonObject o = new SimpleIntMapJsonObject();
        flatten(o,"",separator,this);
        return o;
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
