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

import static com.github.jsonj.tools.JsonBuilder.primitive;

import java.util.Iterator;
import java.util.LinkedList;

import com.github.jsonj.exceptions.JsonTypeMismatchException;

/**
 * Representation of json arrays that extends LinkedList.
 */
public class JsonArray extends LinkedList<JsonElement> implements JsonElement {
	private static final long serialVersionUID = -1269731858619421388L;

	/**
	 * Variant of add that takes a string instead of a JsonElement. The inherited add only supports JsonElement.
	 * @param s
	 */
	public void add(final String s) {
		add(primitive(s));
	}

	/**
	 * Variant of add that adds multiple strings.
	 * @param strings
	 */
	public void add(final String...strings) {
		for (String s : strings) {
			add(primitive(s));
		}
	}

	/**
	 * Variant of add that adds multiple JsonElements.
	 * @param elements
	 */	public void add(final JsonElement...elements) {
		for (JsonElement element : elements) {
			add(primitive(element));
		}
	}

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

	/**
	 * Variant of contains that checks if the array contains something that can be extracted with JsonElement get(final String label).
	 * @param label
	 * @return true if the array contains the element
	 */
	public boolean contains(final String label) {
		return get(label) != null;
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
	
	@Override
	public Object clone() {
		return deepClone();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JsonArray deepClone() {
		JsonArray array = new JsonArray();
		for (JsonElement jsonElement : this) {
			array.add(jsonElement.deepClone());
		}
		return array;
	}
	
	@Override
	public boolean isEmpty() {
		boolean empty = true;
		if(size() > 0) {
			for (JsonElement element : this) {
				empty = empty && element.isEmpty();
				if(!empty) {
					return false;
				}
			}
		}
		return empty;
	}
	
	@Override
	public void removeEmpty() {
		Iterator<JsonElement> iterator = iterator();
		while (iterator.hasNext()) {
			JsonElement jsonElement = (JsonElement) iterator.next();
			if(jsonElement.isEmpty()) {
				iterator.remove();
			} else {
				jsonElement.removeEmpty();
			}			
		}
	}
}
