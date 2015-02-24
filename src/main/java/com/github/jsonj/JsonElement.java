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

import com.github.jsonj.exceptions.JsonTypeMismatchException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;

/**
 * Super type of all Json elements (object, list, and primitive).
 */
public interface JsonElement extends Cloneable, Serializable {
    /**
     * @return the type of this json element
     */
    JsonType type();

    /**
     * @return a JsonObject
     * @throws JsonTypeMismatchException if the element is not an object
     */
    JsonObject asObject();

    /**
     * @return a JsonArray
     * @throws JsonTypeMismatchException if the element is not an array
     */
    JsonArray asArray();

    /**
     * @return a JsonPrimitive
     * @throws JsonTypeMismatchException if the element is not a primitive type
     */
    JsonPrimitive asPrimitive();

    String asString();

    double asDouble();
    float asFloat();

    int asInt();
    long asLong();

    boolean asBoolean();

    /**
     * @return true if the JsonElement is a JsonObject.
     */
    boolean isObject();

    /**
     * @return true if the JsonElement is a JsonArray
     */
    boolean isArray();

    /**
     * @return true if the JsonElement is a JsonPrimitive
     */
    boolean isPrimitive();

    /**
     * @param <T> an implementation of {@link JsonElement}
     * @return a deep clone of the JsonElement.
     */
    <T extends JsonElement> T deepClone();

    /**
     * @return true if the JsonElement is effectively empty.
     */
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
    String prettyPrint();

    /**
     * @return true if the element is a json number (double or long)
     */
    boolean isNumber();

    /**
     * @return true if the element is a boolean
     */
    boolean isBoolean();

    /**
     * @return true if the element is a json null
     */
    boolean isNull();

    /**
     * @return true if the element is a string
     */
    boolean isString();

    default void serialize(OutputStream out) throws IOException {
        try(OutputStreamWriter w = new OutputStreamWriter(out, "UTF-8")) {
            serialize(w);
        }
    }

    /**
     * Serialize a utf8 encoded representation to the writer.
     * @param w writer
     * @throws IOException when there is a problem with the writer
     */
    void serialize(Writer w) throws IOException;

    JsonSet asSet();

    /**
     * @return a clone of the JsonElement that is immutable. Note, JsonPrimitive instances are already immutable so they return themselves.
     */
    JsonElement immutableClone();

    default Number asNumber() {
        throw new JsonTypeMismatchException("not a primitive");
    }

    boolean isMutable();
}
