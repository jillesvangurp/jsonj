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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.jsonj.exceptions.JsonTypeMismatchException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;

/**
 * Super type of all Json elements (object, list, and primitive).
 */
@JsonDeserialize(using=JacksonObjectDeserializer.class)
public interface JsonElement extends Cloneable, Serializable {

    /**
     * @return the type of this json element
     */
    @JsonIgnore
    @Nonnull JsonType type();

    /**
     * @return a JsonObject
     * @throws JsonTypeMismatchException if the element is not an object
     */
    @JsonIgnore
    @Nonnull JsonObject asObject();

    /**
     * @return a JsonArray
     * @throws JsonTypeMismatchException if the element is not an array
     */
    @JsonIgnore
    @Nonnull JsonArray asArray();

    /**
     * @return a JsonPrimitive
     * @throws JsonTypeMismatchException if the element is not a primitive type
     */
    @Nonnull JsonPrimitive asPrimitive();

    @Nonnull String asString();

    double asDouble();
    float asFloat();

    int asInt();
    long asLong();

    boolean asBoolean();

    /**
     * @return true if the JsonElement is a JsonObject.
     */
    @JsonIgnore
    boolean isObject();

    /**
     * @return true if the JsonElement is a JsonArray
     */
    @JsonIgnore
    boolean isArray();

    /**
     * @return true if the JsonElement is a JsonPrimitive
     */
    @JsonIgnore
    boolean isPrimitive();

    /**
     * @param <T> an implementation of {@link JsonElement}
     * @return a deep clone of the JsonElement.
     */
    @Nonnull <T extends JsonElement> T deepClone();

    /**
     * @return true if the JsonElement is effectively empty.
     */
    @JsonIgnore
    boolean isEmpty();

    /**
     * Removes empty elements from a json tree. An object is empty if it
     * contains no elements or if all the entries in it are empty. Likewise a
     * list is empty if it has no elements or only empty elements. Finally,
     * primitives are considered empty if the value is null or if the string
     * value is equal to "".
     */
    void removeEmpty();

    /**
     * @return pretty printed serialized version of this element. Use toString to get the non pretty printed version.
     */
    @Nonnull String prettyPrint();

    /**
     * @return true if the element is a json number (double or long)
     */
    @JsonIgnore
    boolean isNumber();

    /**
     * @return true if the element is a boolean
     */
    @JsonIgnore
    boolean isBoolean();

    /**
     * @return true if the element is a json null
     */
    @JsonIgnore
    boolean isNull();

    /**
     * @return true if the element is a string
     */
    @JsonIgnore
    boolean isString();

    default void serialize(@Nonnull OutputStream out) throws IOException {
        try(OutputStreamWriter w = new OutputStreamWriter(out, "UTF-8")) {
            serialize(w);
        }
    }

    /**
     * Serialize a utf8 encoded representation to the writer.
     * @param w writer
     * @throws IOException when there is a problem with the writer
     */
    void serialize(@Nonnull Writer w) throws IOException;

    @Nonnull JsonSet asSet();

    /**
     * @return a clone of the JsonElement that is immutable. Note, JsonPrimitive instances are already immutable so they return themselves.
     */
    @Nonnull JsonElement immutableClone();

    default @Nonnull Number asNumber() {
        throw new JsonTypeMismatchException("not a primitive");
    }

    @JsonIgnore
    boolean isMutable();
}
