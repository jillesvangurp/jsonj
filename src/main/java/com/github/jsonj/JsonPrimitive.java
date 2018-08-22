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

import com.fasterxml.jackson.annotation.JsonValue;
import com.github.jsonj.exceptions.JsonTypeMismatchException;
import com.github.jsonj.tools.JsonSerializer;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Formattable;
import java.util.Formatter;
import java.util.Locale;

/**
 * Representation of json primitives.
 */
public class JsonPrimitive implements JsonElement, Serializable, Formattable {
    private static final long serialVersionUID = 111536854493507209L;
    private static final Charset UTF8 = Charset.forName("utf-8");

    private final Object value;
    private final @Nonnull JsonType type;

    /** Null object in json, no point creating this over and over again */
    public static final @Nonnull JsonPrimitive JSON_NULL = new JsonPrimitive((String)null);

    private JsonPrimitive(Object value, @Nonnull JsonType type) {
        this.value = value;
        this.type = type;
    }

    public JsonPrimitive(final String s) {
        if(s==null) {
            type = JsonType.nullValue;
            value = null;
        } else {
            type=JsonType.string;
            value = s.getBytes(UTF8);
        }
    }

    public JsonPrimitive(final Boolean b) {
        if(b == null) {
            type = JsonType.nullValue;
            value = null;
        } else {
            type = JsonType.bool;
            value = b;
        }
    }

    public JsonPrimitive(final Number n) {
        if(n == null) {
            type = JsonType.nullValue;
            value = null;
        } else {
            type = JsonType.number;
            if(n instanceof Integer) {
                // make sure to handle Integers and Longs consistently with json simple (always a long)
                value = n.longValue();
            } else {
                value = n;
            }
        }
    }

    public JsonPrimitive(final Object object) {
        if(object == null) {
            type = JsonType.nullValue;
            value = null;
        } else if(object instanceof Number) {
            type = JsonType.number;
            if(object instanceof Integer) {
                // make sure to handle Integers and Longs consistently with json simple (always a long)
                value = ((Number)object).longValue();
            } else {
                value = object;
            }
        } else if(object instanceof Boolean) {
            type = JsonType.bool;
            value = object;
        } else {
            type = JsonType.string;
            value = object.toString().getBytes(UTF8);
        }
    }

    @Override
    public long asLong() {
        if(type == JsonType.number) {
            return ((Number)value).longValue();
        } else {
            try {
                return Long.valueOf(asString());
            } catch (NumberFormatException e) {
                throw new JsonTypeMismatchException("not a long '"+value+"'");
            }
        }
    }

    @Override
    public int asInt() {
        if(type == JsonType.number) {
            return ((Number)value).intValue();
        } else {
            try {
                return Integer.valueOf(asString());
            } catch (NumberFormatException e) {
                throw new JsonTypeMismatchException("not a integer '"+value+"'");
            }
        }
    }

    @Override
    public float asFloat() {
        if(type == JsonType.number) {
            return ((Number)value).floatValue();
        } else {
            try {
                return Float.valueOf(asString());
            } catch (NumberFormatException e) {
                throw new JsonTypeMismatchException("not a float '"+value+"'");
            }
        }
    }

    @Override
    public double asDouble() {
        if(type == JsonType.number) {
            return ((Number)value).doubleValue();
        } else {
            try {
                return Double.valueOf(asString());
            } catch (NumberFormatException e) {
                throw new JsonTypeMismatchException("not a double '"+value+"'");
            }
        }
    }

    @Override
    public Number asNumber() {
        if(type == JsonType.number) {
            return (Number)value;
        } else {
            try {
                // note, this may not work as you want for BigDecimals/BigIntegers.
                return NumberFormat.getInstance(Locale.ENGLISH).parse(asString());
            } catch (ParseException e) {
                throw new JsonTypeMismatchException("not a parsable Number '"+value+"'");
            }
        }
    }

    @Override
    public boolean asBoolean() {
        if(type == JsonType.bool) {
            return ((Boolean)value).booleanValue();
        } else{
            throw new JsonTypeMismatchException("not a boolean '"+value+"'");
        }
    }

    @Override
    public String asString() {
        if( null == value ) {
            return "";
        } else if(type==JsonType.string) {
            return new String((byte[]) value, UTF8);
        } else {
            return value.toString();
        }
    }

    @Override
    public @Nonnull JsonType type() {
        return type;
    }
    @Override
    public JsonObject asObject() {
        throw new JsonTypeMismatchException("not an object");
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
        return this;
    }

    /**
     * @return the raw value as an Object.
     */
    @JsonValue
    public Object value() {
        if(isString()) {
            return asString();
        } else {
            return value;
        }
    }



    @Override
    public String toString() {
        switch (type) {
        case string:
            String raw;
            raw = new String((byte[]) value, UTF8);
            return '"' + JsonSerializer.jsonEscape(raw) + '"';
        case bool:
            return value.toString();
        case number:
            return value.toString();
        case nullValue:
            return "null";
        default:
            throw new IllegalArgumentException("value has to be a primitive");
        }
    }

    @Override
    public void serialize(Writer w) throws IOException {
        switch (type) {
        case string:
            w.append(JsonSerializer.QUOTE);
            byte[] bytes = (byte[]) value;
            w.append(JsonSerializer.jsonEscape(new String(bytes, UTF8)));
            w.append(JsonSerializer.QUOTE);
            return;
        case bool:
            w.append(value.toString());
            return;
        case number:
            w.append(value.toString());
            return;
        case nullValue:
            w.append("null");
            return;
        default:
            throw new IllegalArgumentException("value has to be a primitive");
        }
    }

    @Override
    public String prettyPrint() {
        return JsonSerializer.serialize(this, true);
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public boolean isNumber() {
        return JsonType.number.equals(type);
    }

    @Override
    public boolean isBoolean() {
        return JsonType.bool.equals(type);
    }

    @Override
    public boolean isNull() {
        return JsonType.nullValue.equals(type);
    }

    @Override
    public boolean isString() {
        return JsonType.string.equals(type);
    }

    @Override
    public boolean equals(final Object o) {
        if(o == null) {
            return false;
        }
        if(!(o instanceof JsonPrimitive)) {
            return false;
        }
        JsonPrimitive primitive = (JsonPrimitive) o;
        if(type == primitive.type && value == null && primitive.value == null) {
            return true;
        }
        if(type == primitive.type && type != JsonType.string && value.equals(primitive.value)) {
            return true;
        } else if(type == primitive.type && type == JsonType.string) {
            return Arrays.equals((byte[]) value, (byte[])primitive.value);
        }

        return false;
    }

    @Override
    public int hashCode() {

        int hashCode = 49*type.hashCode();
        if(value != null && type != JsonType.string) {
            hashCode = hashCode * value.hashCode();
        } else if (value != null) {
            hashCode = hashCode * Arrays.hashCode((byte[]) value);
        }
        return hashCode;
    }

    @Override
    public Object clone() {
        return deepClone();
    }

    @SuppressWarnings("unchecked")
    @Override
    public JsonPrimitive deepClone() {
        // all supported value types are immutable so no need to clone those.
        return new JsonPrimitive(value,type);
    }

    @Override
    public JsonElement immutableClone() {
        return this;
    }

    @Override
    public boolean isMutable() {
        return false;
    };

    @Override
    public boolean isEmpty() {
        if(value == null) {
            return true;
        } else {
            return StringUtils.isEmpty(asString());
        }
    }

    @Override
    public void removeEmpty() {
    }

    @Override
    public void formatTo(Formatter formatter, int flags, int width, int precision) {
        // TODO actually support more stuff here for int/double formatting
        // this is useful already since you can shove the value into a formatted string
        formatter.format(Locale.ENGLISH, "%s", asString());
    }
}
