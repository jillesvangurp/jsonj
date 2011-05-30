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
package org.jsonj.tools;

import org.jsonj.JsonArray;
import org.jsonj.JsonElement;
import org.jsonj.JsonObject;
import org.jsonj.JsonPrimitive;

/**
 * Builder class for json objects. If you plan to work a lot with jsonj, you'll
 * want to import this statically, for example by adding this class to your
 * eclipse Favorites list.
 */
public class JsonBuilder {
	final JsonObject object;

	private JsonBuilder() {
		object = new JsonObject();
	}

	/**
	 * @return the constructed object
	 */
	public JsonObject get() {
		return object;
	}

	public JsonBuilder put(final String key, final JsonElement e) {
		object.put(key, e);
		return this;
	}

	public JsonBuilder put(final String key, final String s) {
		object.put(key, primitive(s));
		return this;
	}

	public JsonBuilder put(final String key, final boolean b) {
		object.put(key, primitive(b));
		return this;
	}

	public JsonBuilder put(final String key, final Number n) {
		object.put(key, primitive(n));
		return this;
	}

	public JsonBuilder putArray(final String key, final String...values) {
		JsonArray jjArray = new JsonArray();
		for (String string : values) {
			jjArray.add(primitive(string));
		}
		object.put(key, jjArray);
		return this;
	}

	public JsonBuilder putArray(final String key, final Number...values) {
		JsonArray jjArray = new JsonArray();
		for (Number number : values) {
			jjArray.add(primitive(number));
		}
		object.put(key, jjArray);
		return this;
	}

	public static JsonBuilder object() {
		return new JsonBuilder();
	}

	/**
	 * @param elements
	 * @return json array with all the elements added
	 */
	public static JsonArray array(final JsonElement...elements) {
		JsonArray jjArray = new JsonArray();
		for (JsonElement jjElement : elements) {
			jjArray.add(jjElement);
		}
		return jjArray;
	}

	/**
	 * @param elements
	 * @return json array with all the elements added as JsonPrimitive
	 */
	public static JsonArray array(final String...elements) {
		JsonArray jjArray = new JsonArray();
		for (String s : elements) {
			jjArray.add(primitive(s));
		}
		return jjArray;
	}

	public static JsonPrimitive primitive(final boolean b) {
		return new JsonPrimitive(b);
	}

	public static JsonPrimitive primitive(final String s) {
		return new JsonPrimitive(s);
	}

	public static JsonPrimitive primitive(final Number n) {
		return new JsonPrimitive(n);
	}

	public static JsonPrimitive primitive(final Object n) {
		return new JsonPrimitive(n);
	}

	/**
	 * @return JsonPrimitive with a null representation
	 */
	public static JsonPrimitive nullValue() {
		return JsonPrimitive.JSON_NULL;
	}
}
