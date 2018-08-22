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

import com.github.jsonj.tools.JsonBuilder;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import static com.github.jsonj.tools.JsonBuilder.array;
import static com.github.jsonj.tools.JsonBuilder.fromObject;
import static com.github.jsonj.tools.JsonBuilder.nullValue;
import static com.github.jsonj.tools.JsonBuilder.primitive;

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

    @Override
    @SuppressWarnings("unchecked")
    public JsonSet deepClone() {
        // TODO Auto-generated method stub
        return super.deepClone().asSet();
    }

    @SuppressWarnings("rawtypes")
    public JsonSet(@Nonnull Set existing) {
        super(existing);
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
     * Variant of add that takes a string instead of a JsonElement. The inherited add only supports JsonElement.
     *
     * @param s
     *            element
     */
    @Override
    public boolean add(final String s) {
        JsonPrimitive primitive = primitive(s);
        if (!contains(primitive)) {
            return add(primitive);
        } else {
            return false;
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
     * May be used to change the contains behavior of the set. If set it will use the provided strategy to compare
     * elements in the set instead of JsonElement.equals().
     *
     * Important: this creates a new set in order to remove duplicates.
     *
     * @param strategy
     *            an implementation of IdStrategy
     * @return a new set with the elements of the old set, minus the duplicates.
     */
    @Deprecated // use withIdStrategy
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
     * Important: this creates a new set in order to remove duplicates.
     *
     * @param field
     *            name of the field
     * @return a new set with the elements of the old set, minus the duplicates.
     */
    @Deprecated // use withIdStrategy
    public JsonSet applyIdStrategy(final String field) {
        return applyIdStrategy(new FieldIdStrategy(field));
    }

    /**
     * Changes the strategy on the current set.
     * @param strategy id strategy
     * @return the current set.
     */
    public JsonSet withIdStrategy(IdStrategy strategy) {
        this.strategy = strategy;
        if(size()>0) {
            JsonSet seen=new JsonSet().withIdStrategy(strategy);
            Iterator<JsonElement> iterator = this.iterator();
            while (iterator.hasNext()) {
                JsonElement e = iterator.next();
                if(seen.contains(e)) {
                    iterator.remove();
                } else {
                    seen.add(e);
                }
            }
        }
        return this;
    }

    /**
     * Changes the strategy on the current set.
     * @param field id field
     * @return the current set.
     */
    public JsonSet withIdStrategy(String... field) {
        return withIdStrategy(new FieldIdStrategy(field));
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
            if(o instanceof JsonDataObject) {
                JsonElement element = ((JsonDataObject) o).getJsonObject();
                for (JsonElement e : this) {
                    if (strategy.equals(e, element)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private static final class FieldIdStrategy implements IdStrategy {
        private final String[] fields;

        private FieldIdStrategy(String... fields) {
            this.fields = fields;
        }

        @Override
        public boolean equals(JsonElement t1, JsonElement t2) {
            JsonObject left = t1.asObject();
            JsonObject right = t2.asObject();

            for(String field: fields) {
                JsonElement lv = left.get(field);
                JsonElement rv = right.get(field);
                if(lv != null && !lv.equals(rv)) {
                    return false;
                } else if(rv!=null && !rv.equals(lv)) {
                    return false;
                }
            }
            return true;
//            Map<String, JsonElement> e1 = new HashMap<>();
//            for (String f: fields) {
//                Validate.notNull(t1.asObject().get(f));
//                e1.put(f, t1.asObject().get(f));
//            }
//            Map<String, JsonElement> e2 = new HashMap<>();
//            for (String f: fields) {
//                Validate.notNull(t2.asObject().get(f));
//                e2.put(f, t2.asObject().get(f));
//            }
//            return e1.equals(e2);
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
