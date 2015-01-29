package com.github.jsonj;

import static com.github.jsonj.tools.JsonBuilder.primitive;

import com.github.jsonj.tools.JsonBuilder;
import com.github.jsonj.tools.JsonSerializer;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Mixin style java interface with a lot of default methods that you can use to easily wrap JsonObject objects with
 * custom functionality. This interface provides proxy methods for the wrapped json object.
 *
 * This interface allows you to create domain classes that wrap around JsonObject without having to use object
 * inheritance.
 *
 * Simply implement getJsonObject() and add any custom methods you want. A good practice is to store the wrapped json
 * object in a final private property that is set from the constructor only.
 *
 * If you need immutable domain objects, simply use JsonObject.immutableClone.
 */
public interface JsonDataObject extends Serializable {
    JsonObject getJsonObject();

    default String prettyPrint() {
        return JsonSerializer.serialize(getJsonObject(), true);
    }

    default JsonElement put(String key, Object value) {
        return getJsonObject().put(key, primitive(value));
    }

    default JsonElement put(String key, JsonElement value) {
        return getJsonObject().put(key, value);
    }

    default JsonElement put(String key, JsonBuilder value) {
        return getJsonObject().put(key, value);
    }

    default void putAll(Map<? extends String, ? extends JsonElement> m) {
        getJsonObject().putAll(m);
    }

    default void add(@SuppressWarnings("unchecked") Entry<String, JsonElement>... es) {
        getJsonObject().add(es);
    }

    default Entry<String, JsonElement> get(int index) {
        return getJsonObject().get(index);
    }

    default Entry<String, JsonElement> first() {
        return getJsonObject().first();
    }

    default JsonElement get(Object key) {
        return getJsonObject().get(key);
    }

    default JsonElement get(final String... labels) {
        return getJsonObject().get(labels);
    }

    default String getString(final String... labels) {
        return getJsonObject().getString(labels);
    }

    default Boolean getBoolean(final String... labels) {
        return getJsonObject().getBoolean(labels);
    }

    default boolean get(final String field, boolean defaultValue) {
        return getJsonObject().get(field, defaultValue);
    }

    default Integer getInt(final String... labels) {
        return getJsonObject().getInt(labels);
    }

    default int get(final String field, int defaultValue) {
        return getJsonObject().get(field, defaultValue);
    }

    default Long getLong(final String... labels) {
        return getJsonObject().getLong(labels);
    }

    default long get(final String field, long defaultValue) {
        return getJsonObject().get(field, defaultValue);
    }

    default Float getFloat(final String... labels) {
        return getJsonObject().getFloat(labels);
    }

    default float get(final String field, float defaultValue) {
        return getJsonObject().get(field, defaultValue);
    }

    default Double getDouble(final String... labels) {
        return getJsonObject().getDouble(labels);
    }

    default double get(final String field, double defaultValue) {
        return getJsonObject().get(field, defaultValue);
    }

    default JsonObject getObject(final String... labels) {
        return getJsonObject().getObject(labels);
    }

    default JsonArray getArray(final String... labels) {
        return getJsonObject().getArray(labels);
    }

    default JsonArray getOrCreateArray(final String... labels) {
        return getJsonObject().getOrCreateArray(labels);
    }

    default JsonSet getOrCreateSet(final String... labels) {
        return getJsonObject().getOrCreateSet(labels);
    }

    default JsonObject getOrCreateObject(final String... labels) {
        return getJsonObject().getOrCreateObject(labels);
    }

    default boolean isEmpty() {
        return getJsonObject().isEmpty();
    }

    default void writeObject(java.io.ObjectOutputStream out) throws IOException {
        getJsonObject().writeObject(out);
    }

    default void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        getJsonObject().readObject(in);
    }
}
