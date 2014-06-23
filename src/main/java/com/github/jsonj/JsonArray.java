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
import static com.github.jsonj.tools.JsonBuilder.primitive;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.github.jsonj.exceptions.JsonTypeMismatchException;
import com.github.jsonj.tools.JsonBuilder;
import com.github.jsonj.tools.JsonSerializer;

/**
 * Representation of json arrays that extends LinkedList.
 */
public class JsonArray extends ArrayList<JsonElement> implements JsonElement {
	private static final long serialVersionUID = -1269731858619421388L;

	public JsonArray() {
	    super();
	}

    @SuppressWarnings("rawtypes")
    public JsonArray(Collection existing) {
        super();
        for(Object o: existing) {
            add(fromObject(o));
        }
    }


	/**
	 * Variant of add that takes a string instead of a JsonElement. The inherited add only supports JsonElement.
	 * @param s string
	 */
	public void add(final String s) {
		add(primitive(s));
	}

	/**
	 * Variant of add that adds one or more strings.
	 * @param strings values
	 */
	public void add(final String...strings) {
		for (String s : strings) {
			add(primitive(s));
		}
	}

    /**
     * Variant of add that adds one or more numbers (float/int).
     * @param numbers values
     */
   public void add(final Number...numbers) {
        for (Number n : numbers) {
            add(primitive(n));
        }
    }

   /**
    * Variant of add that adds one or more booleans.
    * @param booleans values
    */
   public void add(final Boolean...booleans) {
       for (Boolean b : booleans) {
           add(primitive(b));
       }
   }

	/**
	 * Variant of add that adds one or more JsonElements.
	 * @param elements elements
	 */
	public void add(final JsonElement...elements) {
		for (JsonElement element : elements) {
			add(element);
		}
	}

	/**
     * Variant of add that adds one or more JsonBuilders. This means you don't have to call get() on the builder when adding object builders.
	 * @param elements builders
	 */
	public void add(JsonBuilder...elements) {
        for (JsonBuilder element : elements) {
            add(element.get());
        }
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
	public JsonElement get(final String label) {
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
	public JsonObject asObject() {
		throw new JsonTypeMismatchException("not an object");
	}

	@Override
	public JsonArray asArray() {
		return this;
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
	public void removeEmpty() {
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
	public void serialize(OutputStream out) throws IOException {
        out.write(JsonSerializer.OPEN_BRACKET);
        Iterator<JsonElement> it = iterator();
        while (it.hasNext()) {
            JsonElement jsonElement = it.next();
            jsonElement.serialize(out);
            if(it.hasNext()) {
                out.write(JsonSerializer.COMMA);
            }
        }
        out.write(JsonSerializer.CLOSE_BRACKET);
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
     * Convenience method to prevent casting JsonElement to JsonObject when iterating in the common case that you have
     * an array of JsonObjects.
     *
     * @return iterable that iterates over JsonObjects instead of JsonElements.
     */
    public Iterable<JsonObject> objects() {
        final JsonArray parent=this;
        return new Iterable<JsonObject>() {

            @Override
            public Iterator<JsonObject> iterator() {
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
            }
        };
    }

    /**
     * Convenience method to prevent casting JsonElement to JsonArray when iterating in the common case that you have
     * an array of JsonArrays.
     *
     * @return iterable that iterates over JsonArrays instead of JsonElements.
     */
    public Iterable<JsonArray> arrays() {
        final JsonArray parent=this;
        return new Iterable<JsonArray>() {

            @Override
            public Iterator<JsonArray> iterator() {
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
            }
        };
    }

    /**
     * Convenience method to prevent casting JsonElement to String when iterating in the common case that you have
     * an array of strings.
     *
     * @return iterable that iterates over Strings instead of JsonElements.
     */
    public Iterable<String> strings() {
        final JsonArray parent=this;
        return new Iterable<String>() {

            @Override
            public Iterator<String> iterator() {
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
            }
        };
    }

    /**
     * Convenience method to prevent casting JsonElement to Double when iterating in the common case that you have
     * an array of doubles.
     *
     * @return iterable that iterates over Doubles instead of JsonElements.
     */
    public Iterable<Double> doubles() {
        final JsonArray parent=this;
        return new Iterable<Double>() {

            @Override
            public Iterator<Double> iterator() {
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
            }
        };
    }

    /**
     * Convenience method to prevent casting JsonElement to Long when iterating in the common case that you have
     * an array of longs.
     *
     * @return iterable that iterates over Longs instead of JsonElements.
     */
    public Iterable<Long> longs() {
        final JsonArray parent=this;
        return new Iterable<Long>() {

            @Override
            public Iterator<Long> iterator() {
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
            }
        };
    }
}
