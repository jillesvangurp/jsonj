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
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.github.jsonj.tools.JsonBuilder.fromObject;
import static com.github.jsonj.tools.JsonBuilder.nullValue;

/**
 * Default implementation for JsonObjects. Uses a memory efficient (for small amounts of keys) map implementation based
 * on two lists with the keys and values. For historic reasons, this class needs to be called JsonObject and have a corresponding constructor.
 * Other implementations need to extend this class for the same reason because we have several methods that return a JsonObject in the API. Most
 * of the common stuff has been pulled up into the IJsonObject interface in the form of default methods.
 */

public class JsonObject implements IJsonObject {
    private static final long serialVersionUID = 497820087656073803L;

    // used only during java object deserialization
    private static JsonParser parser = null;

    private final SimpleStringKeyMap<JsonElement> simpleMap = new SimpleStringKeyMap<>();

    private String idField = null;

    public JsonObject() {
    }

    protected JsonObject createNew() {
        return new JsonObject();
    }

    @SuppressWarnings("rawtypes")
    public JsonObject(@Nonnull Map existing) {
        Iterator iterator = existing.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            put(entry.getKey().toString(), fromObject(entry.getValue()));
        }
    }

    @Override
    public JsonObject asObject() {
        return this;
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
    public JsonElement put(String key, JsonElement value) {
        Validate.notNull(key);
        if (value == null) {
            value = nullValue();
        }
        return simpleMap.put(key, value);
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

    @Override
    public boolean equals(Object o) {
        return defaultEquals(o);
    }

    @Override
    public int hashCode() {
        if (idField != null) {
            JsonElement jsonElement = get(idField);
            if (jsonElement != null) {
                return jsonElement.hashCode();
            }
        }
        return defaultHashCode();
    }

    @Override
    public Object clone() {
        return deepClone();
    }

    @SuppressWarnings("unchecked")
    @Override
    public JsonObject deepClone() {
        JsonObject object = createNew();
        Set<java.util.Map.Entry<String, JsonElement>> es = entrySet();
        for (Entry<String, JsonElement> entry : es) {
            JsonElement e = entry.getValue().deepClone();
            object.put(entry.getKey(), e);
        }
        return object;
    }

    @Override
    public JsonObject immutableClone() {
        JsonObject object = createNew();
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
    public void clear() {
        simpleMap.clear();
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
    public JsonObject flatten(@Nonnull String separator) {
        JsonObject o = createNew();
        flatten(o,"",separator,this);
        return o;
    }

    protected void flatten(@Nonnull JsonObject root, @Nonnull String path, @Nonnull String separator, JsonElement element) {
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

    // used by java serialization
    void writeObject(java.io.ObjectOutputStream out) throws IOException {
        // when using object serialization, write the json bytes
        byte[] bytes = toString().getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    // used by java serialization
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
