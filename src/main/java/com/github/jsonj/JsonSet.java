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

import static com.github.jsonj.tools.JsonBuilder.fromObject;
import static com.github.jsonj.tools.JsonBuilder.nullValue;
import static com.github.jsonj.tools.JsonBuilder.primitive;

import java.util.Collection;
import java.util.Set;

import com.github.jsonj.tools.JsonBuilder;

/**
 * Representation of json arrays that behaves like a set.
 */
@SuppressWarnings("unchecked")
public class JsonSet extends JsonArray implements Set<JsonElement> {
    private static final long serialVersionUID = 753773658521455994L;

    public JsonSet() {
	    super();
	}

    @SuppressWarnings("rawtypes")
    public JsonSet(Collection existing) {
        super();
        for(Object o: existing) {
            JsonElement fromObject = fromObject(o);
            if(!contains(fromObject)) {
                add(fromObject);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public JsonSet(Set existing) {
        super(existing);
    }

    /**
     * Variant of add that takes a string instead of a JsonElement. The inherited add only supports JsonElement.
     * @param s element
     */
    @Override
    public void add(final String s) {
        JsonPrimitive primitive = primitive(s);
        if(!contains(primitive)) {
            add(primitive);
        }
    }

    @Override
    public boolean add(JsonElement e) {
        if(e==null) {
            e=nullValue();
        }
        if(!contains(e)) {
            super.add(e);
            return true;
        }
        return false;
    }

    /**
     * Variant of add that adds multiple strings.
     * @param elements elements
     */
    @Override
    public void add(final String...elements) {
        for (String s : elements) {
            JsonPrimitive primitive = primitive(s);
            if(!contains(primitive)) {
                add(primitive);
            }
        }
    }

    /**
     * Variant of add that adds multiple JsonElements.
     * @param elements elements
     */
    @Override
    public void add(final JsonElement...elements) {
        for (JsonElement element : elements) {
            JsonPrimitive primitive = primitive(element);
            add(primitive);
        }
    }

    @Override
    public void add(final JsonBuilder...elements) {
        for (JsonBuilder element : elements) {
            JsonObject object = element.get();
            add(object);
        }
    }

    @Override
    public boolean addAll(@SuppressWarnings("rawtypes") Collection c) {
        for (Object element : c) {
            if(element instanceof JsonElement) {
                add((JsonElement)element);
            } else {
                JsonPrimitive primitive = primitive(element);
                add(primitive);
            }
        }
        return c.size() != 0;
    }
}
