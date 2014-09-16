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

import static com.github.jsonj.tools.JsonBuilder.array;
import static com.github.jsonj.tools.JsonBuilder.fromObject;
import static com.github.jsonj.tools.JsonBuilder.nullValue;
import static com.github.jsonj.tools.JsonBuilder.primitive;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.github.jsonj.tools.JsonBuilder;

/**
 * Representation of json arrays that behaves like a set.
 */
public class JsonSet extends JsonArray implements Set<JsonElement> {
    private static final long serialVersionUID = 753773658521455994L;
    private IdStrategy strategy = null;

    public JsonSet() {
        super();
    }

    @SuppressWarnings("rawtypes")
    public JsonSet(Collection existing) {
        super();
        for (Object o : existing) {
            JsonElement fromObject = fromObject(o);
            if (!contains(fromObject)) {
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
     *
     * @param s
     *            element
     */
    @Override
    public void add(final String s) {
        JsonPrimitive primitive = primitive(s);
        if (!contains(primitive)) {
            add(primitive);
        }
    }

    @Override
    public boolean remove(Object o) {
        if(strategy == null) {
            return super.remove(o);
        } else {
            Iterator<JsonElement> it = iterator();
            while (it.hasNext()) {
                JsonElement jsonElement = it.next();
                if(strategy.equals((JsonElement)o,jsonElement)) {
                    it.remove();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean add(JsonElement e) {
        if (e == null) {
            e = nullValue();
        }
        if (!contains(e)) {
            super.add(e);
            return true;
        } else if(strategy != null){
            remove(e); // remove the old element that is identical according to the strategy
            super.add(e);
        }
        return false;
    }

    @Override
    public JsonArray asArray() {
        JsonArray array = array();
        array.addAll(this);
        return array;
    }

    @Override
    public JsonSet asSet() {
        return this;
    }

    /**
     * Variant of add that adds multiple strings.
     *
     * @param elements
     *            elements
     */
    @Override
    public void add(final String... elements) {
        for (String s : elements) {
            JsonPrimitive primitive = primitive(s);
            if (!contains(primitive)) {
                add(primitive);
            }
        }
    }

    /**
     * Variant of add that adds multiple JsonElements.
     *
     * @param elements
     *            elements
     */
    @Override
    public void add(final JsonElement... elements) {
        for (JsonElement element : elements) {
            add(element);
        }
    }

    @Override
    public void add(final JsonBuilder... elements) {
        for (JsonBuilder element : elements) {
            JsonObject object = element.get();
            add(object);
        }
    }

    @Override
    public boolean addAll(@SuppressWarnings("rawtypes") Collection c) {
        for (Object element : c) {
            if (element instanceof JsonElement) {
                add((JsonElement) element);
            } else {
                JsonPrimitive primitive = primitive(element);
                add(primitive);
            }
        }
        return c.size() != 0;
    }

    /**
     * May be used to change the contains behavior of the set. If set it will use the provided strategy to compare
     * elements in the set instead of JsonElement.equals()
     *
     * @param strategy
     *            an implementation of IdStrategy
     * @return a new set with the elements of the old set, minus the duplicates.
     */
    public JsonSet applyIdStrategy(IdStrategy strategy) {
        JsonSet newSet = new JsonSet();
        newSet.strategy = strategy;
        for (JsonElement e : this) {
            newSet.add(e);
        }
        return newSet;
    }

    /**
     * May be used to change the contains behavior of the set. With a field set, it will assume elements are objects and
     * extact the field with the given name and use that for determining object equality instead of using
     * JsonElement.equals().
     *
     * @param field
     *            name of the field
     * @return a new set with the elements of the old set, minus the duplicates.
     */
    public JsonSet applyIdStrategy(final String field) {
        return applyIdStrategy(new IdStrategy() {

            @Override
            public boolean equals(JsonElement t1, JsonElement t2) {
                JsonElement e1 = t1.asObject().get(field);
                JsonElement e2 = t2.asObject().get(field);
                Validate.notNull(e1);
                Validate.notNull(e2);
                return e1.equals(e2);
            }
        });
    }

    @Override
    public boolean contains(Object o) {
        if (strategy == null) {
            return super.contains(o);
        } else {
            if (o instanceof JsonElement) {
                JsonElement element = (JsonElement) o;
                for (JsonElement e : this) {
                    if (strategy.equals(e, element)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * May be used to override the default equals behavior of JsonElements that are part of the set.
     */
    public interface IdStrategy {
        /**
         * @param t1 first element
         * @param t2 second element
         * @return true if t1 equals t2
         */
        boolean equals(JsonElement t1, JsonElement t2);
    }
}
