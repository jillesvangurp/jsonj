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
import com.github.jsonj.tools.JsonBuilder;
import com.github.jsonj.tools.JsonSerializer;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static com.github.jsonj.tools.JsonBuilder.fromObject;
import static com.github.jsonj.tools.JsonBuilder.primitive;

/**
 * Representation of json arrays.
 */
public class JsonArray extends ArrayList<JsonElement> implements JsonElement {
    private static final long serialVersionUID = -1269731858619421388L;
    private boolean immutable=false;

    public JsonArray() {
        super();
    }

    public JsonArray(@Nonnull Collection<?> existing) {
        super();
        for(Object o: existing) {
            add(fromObject(o));
        }
    }

    public JsonArray(@Nonnull Stream<Object> s) {
        super();
        s.forEach(o -> this.addObject(o));
    }

    /**
     * Allows you to add any kind of object.
     * @param o the value; will be passed through fromObject()
     * @return true if object was added
     */
    public boolean addObject(Object o) {
        return this.add(fromObject(o));
    }

    /**
     * @param p a predicate
     * @return array of elements matching p
     */
    public @Nonnull JsonArray filter(@Nonnull Predicate<JsonElement> p) {
        return stream().filter(p).collect(JsonjCollectors.array());
    }

    /**
     * @param p a predicate
     * @return an optional of the first element matching p or Optional.empty() if nothing matches
     */
    public Optional<JsonElement> findFirstMatching(@Nonnull Predicate<JsonElement> p) {
        for(JsonElement e: this) {
            if(p.test(e)) {
                return Optional.of(e);
            }
        }
        return Optional.empty();
    }

    /**
     * Allows you to lookup objects from an array by e.g. their id.
     * @param fieldName field to match on
     * @param value value of the field
     * @return the first object where field == value, or null
     */
    public Optional<JsonObject> findFirstWithFieldValue(@Nonnull String fieldName, String value) {
        JsonElement result = findFirstMatching(e -> {
            if(!e.isObject()) {
                return false;
            }
            JsonObject object = e.asObject();
            String fieldValue = object.getString(fieldName);
            if(fieldValue !=null) {
                return fieldValue.equals(value);
            } else {
                return false;
            }
        }).orElse(null);
        if(result != null) {
            return Optional.of(result.asObject());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Variant of add that takes a string instead of a JsonElement. The inherited add only supports JsonElement.
     * @param s string
     * @return true if element was added successfully
     */
    public boolean add(String s) {
        return add(primitive(s));
    }

    /**
     * Variant of add that adds one or more strings.
     * @param strings values
     */
    public void add(String...strings) {
        for (String s : strings) {
            add(primitive(s));
        }
    }

    /**
     * Variant of add that adds one or more numbers (float/int).
     * @param numbers values
     */
    public void add(Number...numbers) {
        for (Number n : numbers) {
            add(primitive(n));
        }
    }

    public void add(Optional<?>...maybeObjects) {
        for (Optional<?> mo : maybeObjects) {
            if(mo.isPresent()) {
                addObject(mo.get());
            }
        }
    }


    /**
     * Variant of add that adds one or more booleans.
     * @param booleans values
     */
    public void add(@Nonnull  Boolean...booleans) {
        for (Boolean b : booleans) {
            add(primitive(b));
        }
    }

    /**
     * Variant of add that adds one or more JsonElements.
     * @param elements elements
     */
    public void add(@Nonnull  JsonElement...elements) {
        for (JsonElement element : elements) {
            add(element);
        }
    }

    /**
     * Variant of add that adds one or more JsonBuilders. This means you don't have to call get() on the builder when adding object builders.
     * @param elements builders
     */
    public void add(@Nonnull JsonBuilder...elements) {
        for (JsonBuilder element : elements) {
            add(element.get());
        }
    }

    public void add(@Nonnull JsonDataObject...elements) {
        for (JsonDataObject element : elements) {
            add(element.getJsonObject());
        }
    }

    @Override
    public boolean add(JsonElement e) {
        if(immutable) {
            throw new IllegalStateException("object is immutable");
        }
        return super.add(e);
    }

    @Override
    public void add(int index, JsonElement element) {
        if(immutable) {
            throw new IllegalStateException("object is immutable");
        }
        super.add(index, element);
    }

    @Override
    public boolean addAll(@SuppressWarnings("rawtypes") Collection c) {
        for (Object element : c) {
            if(element instanceof JsonElement) {
                add((JsonElement)element);
            } else {
                add(primitive(element));
            }
        }
        return c.size() != 0;
    }

    /**
     * Convenient method providing a few alternate ways of extracting elements
     * from a JsonArray.
     *
     * @param label label
     * @return the first element in the array matching the label or the n-th
     *         element if the label is an integer and the element an object or
     *         an array.
     */
    public JsonElement get(String label) {
        int i = 0;
        try{
            for (JsonElement e : this) {
                if(e.isPrimitive() && e.asPrimitive().asString().equals(label)) {
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

    public Optional<JsonElement> maybeGet(int index) {
        if(index>=size()) {
            // prevent index out of bounds exception
            return Optional.empty();
        }
        return Optional.ofNullable(get(index));
    }

    public Optional<String> maybeGetString(int index) {
        return maybeGet(index).map(e -> e.asString());
    }

    public Optional<Integer> maybeGetInt(int index) {
        return maybeGet(index).map(e -> e.asInt());
    }

    public Optional<Long> maybeGetLong(int index) {
        return maybeGet(index).map(e -> e.asLong());
    }

    public Optional<Number> maybeGetNumber(int index) {
        return maybeGet(index).map(e -> e.asNumber());
    }

    public Optional<Boolean> maybeGetBoolean(int index) {
        return maybeGet(index).map(e -> e.asBoolean());
    }

    public Optional<JsonArray> maybeGetArray(int index) {
        return maybeGet(index).map(e -> e.asArray());
    }

    public Optional<JsonSet> maybeGetSet(int index) {
        return maybeGet(index).map(e -> e.asSet());
    }

    public Optional<JsonObject> maybeGetObject(int index) {
        return maybeGet(index).map(e -> e.asObject());
    }

    public JsonElement first() {
        return get(0);
    }

    public JsonElement last() {
        return get(size()-1);
    }

    /**
     * Variant of contains that checks if the array contains something that can be extracted with JsonElement get(final String label).
     * @param label label
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
    public @Nonnull JsonObject asObject() {
        throw new JsonTypeMismatchException("not an object");
    }

    @Override
    public @Nonnull JsonArray asArray() {
        return this;
    }

    @Override
    public @Nonnull JsonSet asSet() {
        JsonSet set = JsonBuilder.set();
        set.addAll(this);
        return set;
    }

    public double[] asDoubleArray() {
        double[] result = new double[size()];
        int i=0;
        for(JsonElement e: this) {
            result[i++] = e.asPrimitive().asDouble();
        }
        return result;
    }

    public int[] asIntArray() {
        int[] result = new int[size()];
        int i=0;
        for(JsonElement e: this) {
            result[i++] = e.asPrimitive().asInt();
        }
        return result;
    }

    public String[] asStringArray() {
        String[] result = new String[size()];
        int i=0;
        for(JsonElement e: this) {
            result[i++] = e.asPrimitive().asString();
        }
        return result;
    }

    @Override
    public JsonPrimitive asPrimitive() {
        throw new JsonTypeMismatchException("not a primitive");
    }

    @Override
    public float asFloat() {
        throw new JsonTypeMismatchException("not a primitive");
    }

    @Override
    public double asDouble() {
        throw new JsonTypeMismatchException("not a primitive");
    }

    @Override
    public int asInt() {
        throw new JsonTypeMismatchException("not a primitive");
    }

    @Override
    public long asLong() {
        throw new JsonTypeMismatchException("not a primitive");
    }

    @Override
    public boolean asBoolean() {
        throw new JsonTypeMismatchException("not a primitive");
    }

    @Override
    public String asString() {
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
        for(int i=0; i<size();i++) {
            JsonElement e1 = get(i);
            JsonElement e2 = array.get(i);
            if(!e1.equals(e2)) {
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
    public @Nonnull Object clone() {
        return deepClone();
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nonnull JsonArray deepClone() {
        JsonArray array = new JsonArray();
        for (JsonElement jsonElement : this) {
            JsonElement e = jsonElement.deepClone();
            array.add(e);
        }
        return array;
    }

    @Override
    public @Nonnull JsonArray immutableClone() {
        JsonArray array = new JsonArray();
        for (JsonElement jsonElement : this) {
            JsonElement e = jsonElement.immutableClone();
            array.add(e);
        }
        array.immutable=true;
        return array;
    }

    @Override
    public boolean isMutable() {
        return !immutable;
    }

    public boolean isNotEmpty() {
        return !isEmpty();
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
    public boolean remove(Object o) {
        if(immutable) {
            throw new IllegalStateException("object is immutable");
        }

        if(o instanceof JsonElement) {
            return super.remove(o);
        } else {
            // try remove it as a primitive.
            return super.remove(primitive(o));
        }
    }

    @Override
    public void removeEmpty() {
        if(immutable) {
            throw new IllegalStateException("object is immutable");
        }

        Iterator<JsonElement> iterator = iterator();
        while (iterator.hasNext()) {
            JsonElement jsonElement = iterator.next();
            if(jsonElement.isEmpty()) {
                iterator.remove();
            } else {
                jsonElement.removeEmpty();
            }
        }
    }

    @Override
    public String toString() {
        return JsonSerializer.serialize(this,false);
    }

    @Override
    public void serialize(Writer w) throws IOException {
        w.append(JsonSerializer.OPEN_BRACKET);
        Iterator<JsonElement> it = iterator();
        while (it.hasNext()) {
            JsonElement jsonElement = it.next();
            jsonElement.serialize(w);
            if(it.hasNext()) {
                w.append(JsonSerializer.COMMA);
            }
        }
        w.append(JsonSerializer.CLOSE_BRACKET);
    }

    @Override
    public String prettyPrint() {
        return JsonSerializer.serialize(this, true);
    }

    @Override
    public boolean isNumber() {
        return false;
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean isString() {
        return false;
    }

    /**
     * Replaces the first matching element.
     * @param e1 original
     * @param e2 replacement
     * @return true if the element was replaced.
     */
    public boolean replace(JsonElement e1, JsonElement e2) {
        int index = indexOf(e1);
        if(index>=0) {
            set(index, e2);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Replaces the element.
     * @param e1 original
     * @param e2 replacement
     * @return true if element was replaced
     */
    public boolean replace(Object e1, Object e2) {
        return replace(fromObject(e1),fromObject(e2));
    }

    /**
     * Convenient replace method that allows you to replace an object based on field equality for a specified field.
     * Useful if you have an id field in your objects. Note, the array may contain non objects as well or objects
     * without the specified field. Those elements won't be replaced of course.
     *
     * @param e1 object you want replaced; must have a value at the specified path
     * @param e2 replacement
     * @param path path
     * @return true if something was replaced.
     */
    public boolean replaceObject(JsonObject e1, JsonObject e2, String...path) {
        JsonElement compareElement = e1.get(path);
        if(compareElement == null) {
            throw new IllegalArgumentException("specified path may not be null in object " + StringUtils.join(path));
        }
        int i=0;
        for(JsonElement e: this) {
            if(e.isObject()) {
                JsonElement fieldValue = e.asObject().get(path);
                if(compareElement.equals(fieldValue)) {
                    set(i,e2);
                    return true;
                }
            }
            i++;
        }
        return false;
    }

    @Override
    public JsonElement set(int index, JsonElement element) {
        if(immutable) {
            throw new IllegalStateException("object is immutable");
        }
        return super.set(index, element);
    }

    @Override
    public boolean addAll(int index, Collection<? extends JsonElement> c) {
        if(immutable) {
            throw new IllegalStateException("object is immutable");
        }
        return super.addAll(index, c);
    }

    @Override
    public void ensureCapacity(int minCapacity) {
        if(immutable) {
            throw new IllegalStateException("object is immutable");
        }
        super.ensureCapacity(minCapacity);
    }

    @Override
    public @Nonnull Iterator<JsonElement> iterator() {
        if(immutable) {
            Iterator<JsonElement> it = super.iterator();
            return new Iterator<JsonElement>() {

                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public JsonElement next() {
                    return it.next();
                }

                @Override
                public void remove() {
                    throw new IllegalStateException("object is immutable");
                }
            };
        } else {
            return super.iterator();
        }
    }

    @Override
    public JsonElement remove(int index) {
        if(immutable) {
            throw new IllegalStateException("object is immutable");
        }
        return super.remove(index);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if(immutable) {
            throw new IllegalStateException("object is immutable");
        }
        return super.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super JsonElement> filter) {
        if(immutable) {
            throw new IllegalStateException("object is immutable");
        }
        return super.removeIf(filter);
    }

    @Override
    public ListIterator<JsonElement> listIterator() {
        if(immutable) {
            throw new IllegalStateException("object is immutable");
        }
        return super.listIterator();
    }

    @Override
    public ListIterator<JsonElement> listIterator(int index) {
        if(immutable) {
            throw new IllegalStateException("object is immutable");
        }
        return super.listIterator(index);
    }

    @Override
    public void replaceAll(UnaryOperator<JsonElement> operator) {
        if(immutable) {
            throw new IllegalStateException("object is immutable");
        }
        super.replaceAll(operator);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        if(immutable) {
            throw new IllegalStateException("object is immutable");
        }
        return super.retainAll(c);
    }

    /**
     * Convenience method to prevent casting JsonElement to JsonObject when iterating in the common case that you have
     * an array of JsonObjects.
     *
     * @return iterable that iterates over JsonObjects instead of JsonElements.
     */
    public @Nonnull Iterable<JsonObject> objects() {
        final JsonArray parent=this;
        return () -> {
            final Iterator<JsonElement> iterator = parent.iterator();
            return new Iterator<JsonObject>() {

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public JsonObject next() {
                    return iterator.next().asObject();
                }

                @Override
                public void remove() {
                    iterator.remove();
                }
            };
        };
    }

    public @Nonnull Stream<JsonObject> streamObjects() {
        return stream().map(e -> e.asObject());
    }

    public @Nonnull Stream<JsonArray> streamArrays() {
        return stream().map(e -> e.asArray());
    }

    public @Nonnull Stream<String> streamStrings() {
        return stream().map(e -> e.asString());
    }

    public @Nonnull Stream<JsonElement> map(Function<JsonElement,JsonElement> f) {
        return stream().map(f);
    }

    public @Nonnull Stream<JsonObject> mapObjects(Function<JsonObject, JsonObject> f) {
        return streamObjects().map(f);
    }

    public void forEachObject(@Nonnull Consumer<? super JsonObject> action) {
        for (JsonElement e : this) {
            action.accept(e.asObject());
        }
    }

    /**
     * Convenience method to prevent casting JsonElement to JsonArray when iterating in the common case that you have
     * an array of JsonArrays.
     *
     * @return iterable that iterates over JsonArrays instead of JsonElements.
     */
    public @Nonnull Iterable<JsonArray> arrays() {
        final JsonArray parent=this;
        return () -> {
            final Iterator<JsonElement> iterator = parent.iterator();
            return new Iterator<JsonArray>() {

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public JsonArray next() {
                    return iterator.next().asArray();
                }

                @Override
                public void remove() {
                    iterator.remove();
                }
            };
        };
    }

    /**
     * Convenience method to prevent casting JsonElement to String when iterating in the common case that you have
     * an array of strings.
     *
     * @return iterable that iterates over Strings instead of JsonElements.
     */
    public @Nonnull Iterable<String> strings() {
        final JsonArray parent=this;
        return () -> {
            final Iterator<JsonElement> iterator = parent.iterator();
            return new Iterator<String>() {

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public String next() {
                    return iterator.next().asString();
                }

                @Override
                public void remove() {
                    iterator.remove();
                }
            };
        };
    }

    /**
     * Convenience method to prevent casting JsonElement to Double when iterating in the common case that you have
     * an array of doubles.
     *
     * @return iterable that iterates over Doubles instead of JsonElements.
     */
    public @Nonnull Iterable<Double> doubles() {
        final JsonArray parent=this;
        return () -> {
            final Iterator<JsonElement> iterator = parent.iterator();
            return new Iterator<Double>() {

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Double next() {
                    return iterator.next().asDouble();
                }

                @Override
                public void remove() {
                    iterator.remove();
                }
            };
        };
    }

    /**
     * Convenience method to prevent casting JsonElement to Long when iterating in the common case that you have
     * an array of longs.
     *
     * @return iterable that iterates over Longs instead of JsonElements.
     */
    public @Nonnull Iterable<Long> longs() {
        final JsonArray parent=this;
        return () -> {
            final Iterator<JsonElement> iterator = parent.iterator();
            return new Iterator<Long>() {

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Long next() {
                    return iterator.next().asLong();
                }

                @Override
                public void remove() {
                    iterator.remove();
                }
            };
        };
    }

    /**
     * Convenience method to prevent casting JsonElement to Long when iterating in the common case that you have
     * an array of longs.
     *
     * @return iterable that iterates over Longs instead of JsonElements.
     */
    public @Nonnull Iterable<Integer> ints() {
        final JsonArray parent=this;
        return () -> {
            final Iterator<JsonElement> iterator = parent.iterator();
            return new Iterator<Integer>() {

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Integer next() {
                    return iterator.next().asInt();
                }

                @Override
                public void remove() {
                    iterator.remove();
                }
            };
        };
    }
}
