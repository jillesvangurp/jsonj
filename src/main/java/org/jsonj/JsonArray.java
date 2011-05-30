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

import java.util.LinkedList;

import org.jsonj.exceptions.JsonTypeMismatchException;

public class JsonArray extends LinkedList<JsonElement> implements JsonElement {
	private static final long serialVersionUID = -1269731858619421388L;

	/**
	 * Convenient method providing a few alternate ways of extracting elements
	 * from a JsonArray.
	 *
	 * @param label
	 * @return the first element in the array matching the label or the n-th
	 *         element if the label is an integer and the element an object or
	 *         an array.
	 */
	public JsonElement get(final String label) {
		int i = 0;
		try{
			for (JsonElement e : this) {
				if(e.isPrimitive() && e.toString().equals(label)) {
					return e;
				} else if((e.isObject() || e.isArray())  && Integer.valueOf(label).equals(i)) {
					return e;
				}
				i++;
			}
		} catch(NumberFormatException e) {
			// fail gracefully
			return null;
		}
		// the element was not found
		return null;
	}

	@Override
	public JsonType type() {
		return JsonType.array;
	}

	@Override
	public JsonObject asObject() {
		throw new JsonTypeMismatchException("not an object");
	}

	@Override
	public JsonArray asArray() {
		return this;
	}

	@Override
	public JsonPrimitive asPrimitive() {
		throw new JsonTypeMismatchException("not a primitive");
	}

	@Override
	public boolean isObject() {
		return false;
	}

	@Override
	public boolean isArray() {
		return true;
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof JsonArray)) {
			return false;
		}
		JsonArray array = (JsonArray) o;
		if (size() != array.size()) {
			return false;
		}
		for (JsonElement jsonElement : array) {
			if (!contains(jsonElement)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int code = 7;
		for (JsonElement e : this) {
			code += e.hashCode();
		}
		return code;
	}
}
