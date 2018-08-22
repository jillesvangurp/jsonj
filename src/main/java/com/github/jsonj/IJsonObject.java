package com.github.jsonj;

import com.github.jsonj.exceptions.JsonTypeMismatchException;
import com.github.jsonj.tools.JsonBuilder;
import com.github.jsonj.tools.JsonSerializer;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.github.jsonj.tools.JsonBuilder.primitive;

public interface IJsonObject extends Map<String,JsonElement>,JsonElement {

    void useIdHashCodeStrategy(String fieldName);

    @Override
    default JsonType type() {
        return JsonType.object;
    }

    @Override
    default JsonArray asArray() {
        throw new JsonTypeMismatchException("not an array");
    }

    @Override
    default JsonSet asSet() {
        throw new JsonTypeMismatchException("not an array");
    }

    @Override
    default JsonPrimitive asPrimitive() {
        throw new JsonTypeMismatchException("not a primitive");
    }

    @Override
    default float asFloat() {
        throw new JsonTypeMismatchException("not a primitive");
    }

    @Override
    default double asDouble() {
        throw new JsonTypeMismatchException("not a primitive");
    }

    @Override
    default int asInt() {
        throw new JsonTypeMismatchException("not a primitive");
    }

    @Override
    default long asLong() {
        throw new JsonTypeMismatchException("not a primitive");
    }

    @Override
    default boolean asBoolean() {
        throw new JsonTypeMismatchException("not a primitive");
    }

    @Override
    default String asString() {
        throw new JsonTypeMismatchException("not a primitive");
    }

    @Override
    default @Nonnull String prettyPrint() {
        return JsonSerializer.serialize(this, true);
    }

    @Override
    default void serialize(Writer w) throws IOException {
        w.append(JsonSerializer.OPEN_BRACE);

        Iterator<Entry<String, JsonElement>> iterator = entrySet().iterator();
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
    default boolean isObject() {
        return true;
    }

    @Override
    default boolean isArray() {
        return false;
    }

    @Override
    default boolean isPrimitive() {
        return false;
    }

    @Override
    default boolean isNumber() {
        return false;
    }

    @Override
    default boolean isBoolean() {
        return false;
    }

    @Override
    default boolean isNull() {
        return false;
    }

    @Override
    default boolean isString() {
        return false;
    }

    default  JsonElement put(@Nonnull String key, JsonBuilder value) {
        return put(key, value.get());
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
    default JsonElement put(@Nonnull String key, Object value) {
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
    default void putAll(Map<? extends String, ? extends JsonElement> m) {
        for (Entry<? extends String, ? extends JsonElement> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    default <T extends JsonDataObject> JsonElement put(@Nonnull String key, @Nonnull T object) {
        return put(key, object.getJsonObject());
    }

    /**
     * Add multiple fields to the object.
     *
     * @param es
     *            field entries
     */
    default void add(@SuppressWarnings("unchecked") @Nonnull Entry<String, JsonElement>... es) {
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
    default Entry<String, JsonElement> get(int index) {
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
    default Entry<String, JsonElement> first() {
        return get(0);
    }

    /**
     * Get a json element at a particular path in an object structure.
     *
     * @param labels
     *            list of field names that describe the location to a particular json node.
     * @return a json element at a particular path in an object or null if it can't be found.
     */
    default JsonElement get(final String... labels) {
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

    default Optional<JsonElement> maybeGet(String...labels) {
        return Optional.ofNullable(get(labels));
    }

    default Optional<String> maybeGetString(String...labels) {
        return maybeGet(labels).map(e -> e.asString());
    }

    default Optional<Integer> maybeGetInt(String...labels) {
        return maybeGet(labels).map(e -> e.asInt());
    }

    default Optional<Long> maybeGetLong(String...labels) {
        return maybeGet(labels).map(e -> e.asLong());
    }

    default Optional<Number> maybeGetNumber(String...labels) {
        return maybeGet(labels).map(e -> e.asNumber());
    }

    default Optional<Boolean> maybeGetBoolean(String...labels) {
        return maybeGet(labels).map(e -> e.asBoolean());
    }

    default Optional<JsonArray> maybeGetArray(String...labels) {
        return maybeGet(labels).map(e -> e.asArray());
    }

    default Optional<JsonSet> maybeGetSet(String...labels) {
        return maybeGet(labels).map(e -> e.asSet());
    }

    default Optional<JsonObject> maybeGetObject(String...labels) {
        return maybeGet(labels).map(e -> e.asObject());
    }

    /**
     * Get a value at a particular path in an object structure.
     *
     * @param labels
     *            one or more text labels
     * @return value or null if it doesn't exist at the specified path
     */
    default String getString(final String... labels) {
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
    default Boolean getBoolean(final String... labels) {
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
    default boolean get(@Nonnull String field, boolean defaultValue) {
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
    default Integer getInt(String... labels) {
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
    default int get(@Nonnull String field, int defaultValue) {
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
    default Long getLong(String... labels) {
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
    default long get(@Nonnull  String field, long defaultValue) {
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
    default Float getFloat(String... labels) {
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
    default float get(@Nonnull  String field, float defaultValue) {
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
    default Double getDouble(String... labels) {
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
    default double get(@Nonnull String field, double defaultValue) {
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
    default JsonObject getObject(String... labels) {
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
    default JsonArray getArray(String... labels) {
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
    default @Nonnull JsonArray getOrCreateArray(@Nonnull String... labels) {
        IJsonObject parent = this;
        JsonElement decendent = null;
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
    default JsonSet getOrCreateSet(String... labels) {
        IJsonObject parent = this;
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
    default JsonObject getOrCreateObject(String... labels) {
        IJsonObject parent = this;
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
            if(decendent == null) {
                throw new IllegalStateException("decendant should not be null here");
            }
            parent = decendent.asObject();
            index++;
        }
        return null;
    }

    @Override
    default boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    default void removeEmpty() {
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
    default boolean isEmpty() {
        return size() == 0;
    }

    default boolean defaultEquals(Object o) {
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

    default int defaultHashCode() {
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

    MapBasedJsonObject toMapBasedJsonObject();

    JsonObject flatten(String separator);

}
