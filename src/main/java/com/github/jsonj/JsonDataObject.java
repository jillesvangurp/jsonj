package com.github.jsonj;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.jsonj.tools.JsonBuilder;
import com.github.jsonj.tools.JsonSerializer;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

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
    @JsonAnyGetter
    @Nonnull JsonObject getJsonObject();

    @JsonIgnore
    default boolean isMutable() {
        return getJsonObject().isMutable();
    }

    default void verifyMutable() {
        if(!isMutable()) {
            throw new IllegalStateException("object is immutable");
        }
    }
    default @Nonnull String prettyPrint() {
        return JsonSerializer.serialize(getJsonObject(), true);
    }

    @JsonAnySetter
    default JsonElement put(@Nonnull String key, Object value) {
        verifyMutable();
        return getJsonObject().put(key, value);
    }

    default JsonElement put(@Nonnull String key, JsonElement value) {
        verifyMutable();
        return getJsonObject().put(key, value);
    }

    default JsonElement put(@Nonnull String key, JsonBuilder value) {
        verifyMutable();
        return getJsonObject().put(key, value);
    }

    default void putAll(Map<? extends String, ? extends JsonElement> m) {
        verifyMutable();
        getJsonObject().putAll(m);
    }

    default void add(@SuppressWarnings("unchecked") @Nonnull  Entry<String, JsonElement>... es) {
        verifyMutable();
        getJsonObject().add(es);
    }

    default JsonElement remove(String key) {
        verifyMutable();
        return getJsonObject().remove(key);
    }

    default boolean containsKey(String key) {
        return getJsonObject().containsKey(key);
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

    default boolean get(@Nonnull  String field, boolean defaultValue) {
        return getJsonObject().get(field, defaultValue);
    }

    default Integer getInt(final String... labels) {
        return getJsonObject().getInt(labels);
    }

    default int get(@Nonnull  String field, int defaultValue) {
        return getJsonObject().get(field, defaultValue);
    }

    default Long getLong(@Nonnull  String... labels) {
        return getJsonObject().getLong(labels);
    }

    default long get(@Nonnull  String field, long defaultValue) {
        return getJsonObject().get(field, defaultValue);
    }

    default Float getFloat(@Nonnull  String... labels) {
        return getJsonObject().getFloat(labels);
    }

    default float get(@Nonnull  String field, float defaultValue) {
        return getJsonObject().get(field, defaultValue);
    }

    default Double getDouble(@Nonnull  String... labels) {
        return getJsonObject().getDouble(labels);
    }

    default double get(@Nonnull  String field, double defaultValue) {
        return getJsonObject().get(field, defaultValue);
    }

    default JsonObject getObject(@Nonnull  String... labels) {
        return getJsonObject().getObject(labels);
    }

    default JsonArray getArray(@Nonnull  String... labels) {
        return getJsonObject().getArray(labels);
    }

    default @Nonnull JsonArray getOrCreateArray(@Nonnull  String... labels) {
        return getJsonObject().getOrCreateArray(labels);
    }

    default JsonSet getOrCreateSet(final String... labels) {
        return getJsonObject().getOrCreateSet(labels);
    }

    default JsonObject getOrCreateObject(final String... labels) {
        return getJsonObject().getOrCreateObject(labels);
    }
    @JsonIgnore
    default boolean isEmpty() {
        return getJsonObject().isEmpty();
    }

    default void writeObject(java.io.ObjectOutputStream out) throws IOException {
        getJsonObject().writeObject(out);
    }

    default void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        getJsonObject().readObject(in);
    }

    default Optional<JsonElement> maybeGet(String...labels) {
        return Optional.ofNullable(get(labels));
    }

    default Optional<String> maybeGetString(String...labels) {
        return getJsonObject().maybeGet(labels).map(e -> e.asString());
    }

    default Optional<Integer> maybeGetInt(String...labels) {
        return getJsonObject().maybeGet(labels).map(e -> e.asInt());
    }

    default Optional<Long> maybeGetLong(String...labels) {
        return getJsonObject().maybeGet(labels).map(e -> e.asLong());
    }

    default Optional<Number> maybeGetNumber(String...labels) {
        return getJsonObject().maybeGet(labels).map(e -> e.asNumber());
    }

    default Optional<Boolean> maybeGetBoolean(String...labels) {
        return getJsonObject().maybeGet(labels).map(e -> e.asBoolean());
    }

    default Optional<JsonArray> maybeGetArray(String...labels) {
        return getJsonObject().maybeGet(labels).map(e -> e.asArray());
    }

    default Optional<JsonSet> maybeGetSet(String...labels) {
        return getJsonObject().maybeGet(labels).map(e -> e.asSet());
    }

    default Optional<JsonObject> maybeGetObject(String...labels) {
        return getJsonObject().maybeGet(labels).map(e -> e.asObject());
    }
}
