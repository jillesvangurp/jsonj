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
package org.jsonj;

import static org.jsonj.tools.JsonBuilder.primitive;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.jsonj.exceptions.JsonTypeMismatchException;
import org.jsonj.tools.JsonSerializer;

public class JsonObject extends LinkedHashMap<String, JsonElement> implements JsonElement {
	private static final long serialVersionUID = 2183487305816320684L;

	@Override
	public JsonType type() {
		return JsonType.object;
	}

	@Override
	public JsonObject asObject() {
		return this;
	}

	@Override
	public JsonArray asArray() {
		throw new JsonTypeMismatchException("not an array");
	}

	@Override
	public JsonPrimitive asPrimitive() {
		throw new JsonTypeMismatchException("not a primitive");
	}

	@Override
	public String toString() {
		return JsonSerializer.serialize(this, false);
	}

	public JsonElement put(final String key, final String value) {
		return super.put(key, primitive(value));
	}

	public JsonElement put(final String key, final Boolean value) {
		return super.put(key, primitive(value));
	}

	public JsonElement put(final String key, final Number value) {
		return super.put(key, primitive(value));
	}

	public JsonElement put(final String key, final Object value) {
		return super.put(key, primitive(value));
	}

	@Override
	public boolean isObject() {
		return true;
	}

	@Override
	public boolean isArray() {
		return false;
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	/**
	 * Get a json element at a particular path in an object.
	 * @param labels list of field names that describe the location to a particular json node.
	 * @return a json element at a particular path in an object.
	 */
	public JsonElement get(final String...labels) {
		JsonElement e = this;
		int n = 0;
		for (String label : labels) {
			e = e.asObject().get(label);
			if(e == null) {
				return null;
			}
			if(n == labels.length-1 && e != null) {
				return e;
			}
			if(!e.isObject()) {
				break;
			}
			n++;
		}
		return null;
	}

	@Override
	public boolean equals(final Object o) {
		if(o == null) {
			return false;
		}
		if(!(o instanceof JsonObject)) {
			return false;
		}
		JsonObject object = (JsonObject)o;
		if(object.entrySet().size() != entrySet().size()) {
			return false;
		}
		Set<Entry<String, JsonElement>> es = entrySet();
		for (Entry<String, JsonElement> entry : es) {
			String key = entry.getKey();
			JsonElement value = entry.getValue();
			if(!value.equals(object.get(key))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		// good enough but won't be very helpful if you put lots of json objects in sets
		return 42;
	}

}
