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

/**
 * Representation of json primitives.
 */
public class JsonPrimitive implements JsonElement {
	private static final long serialVersionUID = 111536854493507209L;

	private final Object value;
	private final JsonType type;

	/** Null object in json, no point creating this over and over again */
	public static final JsonPrimitive JSON_NULL = new JsonPrimitive((String)null);

	private JsonPrimitive(Object value, JsonType type) {
		this.value = value;
		this.type = type;		
	}
	
	public JsonPrimitive(final String s) {
		if(s==null) {
			type = JsonType.nullValue;
			value = null;
		} else {
			type = JsonType.string;
			value = s;
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
			value = object;
		}
	}

	public int asInt() {
		if(type == JsonType.number) {
			return ((Number)value).intValue();
		} else {
			throw new JsonTypeMismatchException("not a number");
		}
	}

	public double asDouble() {
		if(type == JsonType.number) {
			return ((Number)value).doubleValue();
		} else {
			throw new JsonTypeMismatchException("not a number");
		}
	}

	public boolean asBoolean() {
		if(type == JsonType.bool) {
			return ((Boolean)value).booleanValue();
		} else{
			throw new JsonTypeMismatchException("not a boolean");
		}
	}

	public String asString() {
		return value.toString();
	}

	@Override
	public JsonType type() {
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
	public JsonPrimitive asPrimitive() {
		return this;
	}

	@Override
	public String toString() {
		if(value != null)
			return value.toString();
		else
			return "null";
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
		if(type == primitive.type && value.equals(primitive.value)) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {

		int hashCode = 49*type.hashCode();
		if(value != null) {
			hashCode = hashCode * value.hashCode();
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
}
