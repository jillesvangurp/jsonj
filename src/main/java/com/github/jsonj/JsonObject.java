
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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.jsonj.exceptions.JsonTypeMismatchException;
import com.github.jsonj.tools.JsonSerializer;

/**
 * Representation of json objects. This class extends LinkedHashMap and may be used as such. In addition a lot of convenience is provided in the form of
 * methods you are likely to need when working with json objects programmatically.
 */
public class JsonObject extends LinkedHashMap<String, JsonElement> implements JsonElement {
	private static final long serialVersionUID = 2183487305816320684L;
	
	private String idField=null;

	@Override
	public JsonType type() {
		return JsonType.object;
	}
	
	/**
	 * By default, the hash code is calculated recursively, which can be rather expensive. Calling this method allows you
	 * to specify a special field that will be used for calculating this object's hashcode. In case the field value is null
	 * it will fall back to recursive behavior.
	 * @param fieldName name of the field value that should be used for calculating the hash code
	 */
	public void useIdHashCodeStrategy(String fieldName) {
	    idField = fieldName.intern();
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

	/**
	 * Variant of put that can take a Object instead of a primitive. The normal put inherited from LinkedHashMap only takes JsonElement instances.
	 * @param key
	 * @param value any object that is accepted by the JsonPrimitive constructor.
	 * @return the JsonElement that was added.
	 * @throws JsonTypeMismatchException if the value cannot be turned into a primitive.
	 */
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
	 * Get a json element at a particular path in an object structure.
	 * @param labels list of field names that describe the location to a particular json node.
	 * @return a json element at a particular path in an object or null if it can't be found.
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

	/**
	 * Get a value at a particular path in an object structure.
	 * @param labels
	 * @return value or null if it doesn't exist at the specified path
	 */
	public String getString(final String...labels) {
		JsonElement jsonElement = get(labels);
		if(jsonElement == null) {
			return null;
		} else {
			return jsonElement.asPrimitive().asString();
		}
	}

	/**
	 * Get a value at a particular path in an object structure.
	 * @param labels
	 * @return value or null if it doesn't exist at the specified path
	 */
	public Boolean getBoolean(final String...labels) {
		JsonElement jsonElement = get(labels);
		if(jsonElement == null) {
			return null;
		} else {
			return jsonElement.asPrimitive().asBoolean();
		}
	}

	/**
	 * Get a value at a particular path in an object structure.
	 * @param labels
	 * @return value or null if it doesn't exist at the specified path
	 */
	public Integer getInt(final String...labels) {
		JsonElement jsonElement = get(labels);
		if(jsonElement == null) {
			return null;
		} else {
			return jsonElement.asPrimitive().asInt();
		}
	}

	/**
	 * Get a value at a particular path in an object structure.
	 * @param labels
	 * @return value or null if it doesn't exist at the specified path
	 */
	public Double getDouble(final String...labels) {
		JsonElement jsonElement = get(labels);
		if(jsonElement == null) {
			return null;
		} else {
			return jsonElement.asPrimitive().asDouble();
		}
	}

	/**
	 * Get a JsonObject at a particular path in an object structure.
	 * @param labels
	 * @return value or null if it doesn't exist at the specified path
	 */
	public JsonObject getObject(final String...labels) {
		JsonElement jsonElement = get(labels);
		if(jsonElement == null) {
			return null;
		} else {
			return jsonElement.asObject();
		}
	}

	/**
	 * Get a JsonArray at a particular path in an object structure.
	 * @param labels
	 * @return value or null if it doesn't exist at the specified path
	 */
	public JsonArray getArray(final String...labels) {
		JsonElement jsonElement = get(labels);
		if(jsonElement == null) {
			return null;
		} else {
			return jsonElement.asArray();
		}
	}

	/**
	 * Get or create a JsonArray at a particular path in an object structure. Any object on the path will be created as well if missing.
	 * @param labels
	 * @return the created JsonArray
	 * @throws JsonTypeMismatchException if an element is present at the path that is not a JsonArray
	 */
	public JsonArray getOrCreateArray(final String...labels) {
		JsonObject parent=this;
		JsonElement decendent;
		int index=0;
		for (String label : labels) {
			decendent=parent.get(label);
			if(decendent == null && index < labels.length-1 && parent.isObject()) {
				decendent = new JsonObject();
				parent.put(label, decendent);
			} else if(index == labels.length-1) {
				if(decendent == null) {
					decendent = new JsonArray();
					parent.put(label, decendent);
					return decendent.asArray();
				} else {
					return decendent.asArray();
				}
			}
			parent = decendent.asObject();
			index++;
		}
		return null;
	}

	/**
	 * Get or create a JsonObject at a particular path in an object structure. Any object on the path will be created as well if missing.
	 * @param labels
	 * @return the created JsonObject
	 * @throws JsonTypeMismatchException if an element is present at the path that is not a JsonObject
	 */
	public JsonObject getOrCreateObject(final String...labels) {
		JsonObject parent=this;
		JsonElement decendent;
		int index=0;
		for (String label : labels) {
			decendent=parent.get(label);
			if(decendent == null && index < labels.length-1 && parent.isObject()) {
				decendent = new JsonObject();
				parent.put(label, decendent);
			} else if(index == labels.length-1) {
				if(decendent == null) {
					decendent = new JsonObject();
					parent.put(label, decendent);
					return decendent.asObject();
				} else {
					return decendent.asObject();
				}
			}
			parent = decendent.asObject();
			index++;
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
	    if(idField != null) {
	        JsonElement jsonElement = get(idField);
	        if(jsonElement != null) {
                return jsonElement.hashCode();
            }
	    }
		int hashCode=23;
		Set<Entry<String, JsonElement>> entrySet = entrySet();
		for (Entry<String, JsonElement> entry : entrySet) {
			hashCode = hashCode * entry.getKey().hashCode() * entry.getValue().hashCode();
		}
		return hashCode;
	}

	@Override
	public Object clone() {
		return deepClone();
	}

	@SuppressWarnings("unchecked")
	@Override
	public JsonObject deepClone() {
		JsonObject object = new JsonObject();
		Set<java.util.Map.Entry<String, JsonElement>> es = entrySet();
		for (Entry<String, JsonElement> entry : es) {
			object.put(entry.getKey(), entry.getValue().deepClone());
		}
		return object;
	}

	/**
	 * Sort objects by key. Note: this method does not recurse on the members.
	 * @return a new JsonObject sorted by key.
	 */
	public JsonObject sort() {
		Set<String> keys = keySet();
		LinkedList<String> list = new LinkedList<String>();
		list.addAll(keys);
		Collections.sort(list);
		JsonObject jsonObject = new JsonObject();
		for (String key : list) {
			jsonObject.put(key, this.get(key).deepClone());
		}
		return jsonObject;
	}

	@Override
	public boolean isEmpty() {
		boolean empty = true;
		if(keySet().size() != 0) {
			for(java.util.Map.Entry<String, JsonElement> entry: entrySet()) {
				empty = empty && entry.getValue().isEmpty();
				if(!empty) {
					return false;
				}
			}
		}
		return empty;
	}

	@Override
	public void removeEmpty() {
		Iterator<java.util.Map.Entry<String, JsonElement>> iterator = entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, JsonElement> entry = iterator.next();
			JsonElement element = entry.getValue();
			if(element.isEmpty()) {
				iterator.remove();
			} else {
				element.removeEmpty();
			}
		}
	}
}
