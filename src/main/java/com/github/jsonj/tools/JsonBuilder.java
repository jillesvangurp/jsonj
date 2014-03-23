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
package com.github.jsonj.tools;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.jsonj.JsonArray;
import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonObject;
import com.github.jsonj.JsonPrimitive;
import com.github.jsonj.JsonSet;

/**
 * Builder class for json objects. If you plan to work a lot with jsonj, you will
 * want to import this statically, for example by adding this class to your
 * eclipse Favorites list.
 */
public class JsonBuilder {
    private final JsonObject object;

    private JsonBuilder() {
        // use the static methods
        object = new JsonObject();
    }

    private JsonBuilder(final JsonObject object) {
        this.object = object;
    }

    /**
     * @return constructed JsonObject
     */
    public JsonObject get() {
        return object;
    }

    /**
     * @param key key
     * @param e value
     * @return the builder
     */
    public JsonBuilder put(final String key, final JsonElement e) {
        object.put(key, e);
        return this;
    }

    /**
     * @param key key
     * @param e value
     * @return the builder
     */
    public JsonBuilder put(final String key, final JsonBuilder e) {
        object.put(key, e);
        return this;
    }

    /**
     * Add a string value to the object.
     *
     * @param key key
     * @param s value
     * @return the builder
     */
    public JsonBuilder put(final String key, final String s) {
        object.put(key, primitive(s));
        return this;
    }

    /**
     * Add a boolean value to the object.
     *
     * @param key key
     * @param b value
     * @return the builder
     */
    public JsonBuilder put(final String key, final boolean b) {
        object.put(key, primitive(b));
        return this;
    }

    /**
     * Add a number to the object.
     *
     * @param key key
     * @param n value
     * @return the builder
     */
    public JsonBuilder put(final String key, final Number n) {
        object.put(key, primitive(n));
        return this;
    }

    /**
     * Add a JsonArray to the object with the string values added.
     *
     * @param key key
     * @param values one or more {@link String} values
     *            values that go in the array
     * @return the builder
     */
    public JsonBuilder putArray(final String key, final String... values) {
        JsonArray jjArray = new JsonArray();
        for (String string : values) {
            jjArray.add(primitive(string));
        }
        object.put(key, jjArray);
        return this;
    }

    /**
     * Add a JsonArray to the object with the number values added.
     *
     * @param key key
     * @param values values
     *            values that go in the array
     * @return the builder
     */
    public JsonBuilder putArray(final String key, final Number... values) {
        JsonArray jjArray = new JsonArray();
        for (Number number : values) {
            jjArray.add(primitive(number));
        }
        object.put(key, jjArray);
        return this;
    }

    /**
     * @return JsonBuilder that may be used to construct a json object.
     */
    public static JsonBuilder object() {
        return new JsonBuilder();
    }

    /**
     * Modify an existing JsonObject with a builder.
     *
     * @param object a json object
     * @return the builder
     */
    public static JsonBuilder object(final JsonObject object) {
        return new JsonBuilder(object);
    }

    /**
     * Alternative to using the object() builder that allows you to add {@link Entry} instances.
     * @param fields one or more Entry instances (use the field method to create them).
     * @return the JsonObject with the entries added.
     */
    @SafeVarargs
    public static JsonObject object(Entry<String,JsonElement>...fields) {
        JsonObject object = new JsonObject();
        object.add(fields);
        return object;
    }

    /**
     * Create a new field that can be added to a JsonObject.
     * @param key key
     * @param value value
     * @return field entry implementation that can be added to a JsonObject
     */
    public static Entry<String,JsonElement> field(final String key, final JsonElement value) {
        Entry<String, JsonElement> entry = new Entry<String,JsonElement>() {

            @Override
            public String getKey() {
                return key;
            }

            @Override
            public JsonElement getValue() {
                return value;
            }

            @Override
            public JsonElement setValue(JsonElement value) {
                throw new UnsupportedOperationException("entries are immutable");
            }};
        return entry;
    }

    /**
     * Create a new field with the key and the result of fromObject on the value.
     * @param key key
     * @param value value
     * @return field entry implementation that can be added to a JsonObject
     */
    public static Entry<String,JsonElement> field(final String key, final Object value) {
        return field(key, fromObject(value));
    }

    /**
     * @return an empty JsonArray
     */
    public static JsonArray array() {
        return new JsonArray();
    }

    /**
     * @param elements one or more json elements
     * @return json array with all the elements added
     */
    public static JsonArray array(final JsonElement... elements) {
        JsonArray jjArray = new JsonArray();
        for (JsonElement jjElement : elements) {
            if(jjElement == null) {
                jjArray.add(nullValue());
            } else {
                jjArray.add(jjElement);
            }
        }
        return jjArray;
    }

    /**
     * Add elements of a collection to a json array.
     * This changes the behavior of array(JsonElement... elements) if you called it with a single JsonArray as an
     * element. Previously you'd get an array with a single array element
     * in it. Because a json array is just another collection, it now inherits the behavior and you get an array of
     * elements in the collection. If the elements are JsonElements, they are added as such. Otherwise it attempts to
     * interpret them as primitives.
     *
     * @param c an existing collection. If the elements are JsonElements, they will be added. Otherwise, primitive will be called on them.
     * @return json array with the collection elements in it
     */
    public static JsonArray array(Collection<?> c) {
        JsonArray jjArray = new JsonArray();
        for (Object o : c) {
            if (o instanceof JsonElement) {
                jjArray.add((JsonElement) o);
            } else {
                jjArray.add(primitive(o));
            }
        }
        return jjArray;
    }

    /**
     * @param elements strings
     * @return json array with all the elements added as JsonPrimitive
     */
    public static JsonArray array(final String... elements) {
        JsonArray jjArray = new JsonArray();
        for (String s : elements) {
            jjArray.add(primitive(s));
        }
        return jjArray;
    }

    /**
     * Allows you to add incomplete object builders without calling get()
     * @param elements json builders
     * @return json array with the builder objects
     */
    public static JsonArray array(final JsonBuilder... elements) {
        JsonArray jjArray = new JsonArray();
        for (JsonBuilder b : elements) {
            jjArray.add(b);
        }
        return jjArray;
    }


    /**
     * @param elements numbers
     * @return an array
     */
    public static JsonArray array(final Number... elements) {
        JsonArray jjArray = new JsonArray();
        for (Number n : elements) {
            jjArray.add(primitive(n));
        }
        return jjArray;
    }

    /**
     * @return an empty JsonSet
     */
    public static JsonSet set() {
        return new JsonSet();
    }

    /**
     * @param elements elements
     * @return json set with all the elements added
     */
    public static JsonSet set(final JsonElement... elements) {
        JsonSet jjArray = new JsonSet();
        for (JsonElement jjElement : elements) {
            if(jjElement==null) {
                jjArray.add(nullValue());
            } else {
                jjArray.add(jjElement);
            }
        }
        return jjArray;
    }

    /**
     * Add elements of a collection to a json array.
     * This changes the behavior of array(JsonElement... elements) if you called it with a single JsonArray as an
     * element. Previously you'd get an array with a single array element
     * in it. Because a json array is just another collection, it now inherits the behavior and you get an array of
     * elements in the collection. If the elements are JsonElements, they are added as such. Otherwise it attempts to
     * interpret them as primitives.
     *
     * @param c an existing collection. If the elements are JsonElements, they will be added. Otherwise, primitive will be called on them.
     * @return json array with the collection elements in it
     */
    public static JsonSet set(Collection<?> c) {
        JsonSet jjArray = new JsonSet();
        for (Object o : c) {
            if (o instanceof JsonElement) {
                jjArray.add((JsonElement) o);
            } else {
                jjArray.add(primitive(o));
            }
        }
        return jjArray;
    }

    /**
     * @param elements strings
     * @return json array with all the elements added as JsonPrimitive
     */
    public static JsonSet set(final String... elements) {
        JsonSet jjSet = new JsonSet();
        for (String s : elements) {
            jjSet.add(primitive(s));
        }
        return jjSet;
    }

    /**
     * Allows you to add incomplete object builders without calling get()
     * @param elements json builders
     * @return json array with the builder objects
     */
    public static JsonSet set(final JsonBuilder... elements) {
        JsonSet jjArray = new JsonSet();
        for (JsonBuilder b : elements) {
            jjArray.add(b);
        }
        return jjArray;
    }


    /**
     * @param elements {@link Number} instances
     * @return json array
     */
    public static JsonSet set(final Number... elements) {
        JsonSet jjArray = new JsonSet();
        for (Number n : elements) {
            jjArray.add(primitive(n));
        }
        return jjArray;
    }


    /**
     * @param value a boolean
     * @return a JsonPrimitive with the value
     */
    public static JsonPrimitive primitive(final boolean value) {
        return new JsonPrimitive(value);
    }

    /**
     * @param value a string
     * @return a JsonPrimitive with the value
     */
    public static JsonPrimitive primitive(final String value) {
        return new JsonPrimitive(value);
    }

    /**
     * @param value a {@link Number} instance
     * @return a JsonPrimitive with the value
     */
    public static JsonPrimitive primitive(final Number value) {
        return new JsonPrimitive(value);
    }

    /**
     * @param value any object that the JsonPrimitive constructor would accept. If it is a JsonPrimitive, the immutable value is returned.
     * @return a JsonPrimitive with the value
     */
    public static JsonPrimitive primitive(final Object value) {
        if(value instanceof JsonPrimitive) {
            return ((JsonPrimitive) value);
        }
        return new JsonPrimitive(value);
    }

    /**
     * @return JsonPrimitive with a null representation
     */
    public static JsonPrimitive nullValue() {
        return JsonPrimitive.JSON_NULL;
    }

    @SuppressWarnings("rawtypes")
    public static JsonElement fromObject(Object o) {
        if(o instanceof JsonBuilder) {
            return ((JsonBuilder) o).get();
        } else if(o instanceof Map) {
            return new JsonObject((Map)o);
        } else if(o instanceof List) {
            return new JsonArray((List)o);
        }
        return primitive(o);
    }
}
