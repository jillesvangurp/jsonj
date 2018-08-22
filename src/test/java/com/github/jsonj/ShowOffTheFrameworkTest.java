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
import static com.github.jsonj.tools.JsonBuilder.nullValue;
import static com.github.jsonj.tools.JsonBuilder.object;
import static com.github.jsonj.tools.JsonBuilder.primitive;
import static com.github.jsonj.tools.JsonSerializer.serialize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertTrue;

import com.github.jsonj.tools.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.testng.annotations.Test;

/**
 * Not really a test but a nice place to show off how to use the framework.
 */
@Test
public class ShowOffTheFrameworkTest {

    /** this could be a singleton or a spring injected object, threadsafe of course. */
    private final JsonParser jsonParser = new JsonParser();;

    public void whatCanThisBabyDo() throws IOException {
        // First there are the json primitives which are based on well known Java classes
        // Everything is a JsonElement: JsonObject, JsonArray, JsonPrimitive (that's it, all you need to know)
        // json dictionaries and arrays are generic Maps and Lists in Java:
        // JsonObject extends LinkedHashMap<String, JsonElement> implements JsonElement
        // JsonArray extends LinkedList<JsonElement> implements JsonElement
        // JsonPrimitive implements JsonElement

        // new is tedious so we have a nice builder for constructing json structures:
        JsonObject object = object()
                .put("its", "just a hash map")
                .put("and", array(
                        primitive("adding"),
                        primitive("stuff"),
                        object().put("is", "easy").get(),
                        array("another array")))
                        .put("numbers", 42)
                        .put("including_this_one", 42.0)
                        .put("booleanstoo", true)
                        .put("nulls_if_you_insist", nullValue())
                        .put("a", object()
                                .put("b", object()
                                        .put("c", true)
                                        .put("d", 42)
                                        .put("e", "hi!")
                                        .get())
                                        .get())
                                        .put("array",
                                                array("1", "2", "etc", "varargs are nice"))
                                                .get();

        // get with varargs, a natural evolution for Map
        assertTrue(object.get("a","b","c").asPrimitive().asBoolean(),
                "extract stuff from a nested object");
        assertTrue(object.getBoolean("a","b","c"),
                "or like this");
        assertTrue(object.getInt("a","b","d") == 42,
                "or an integer");
        assertTrue(object.getString("a","b","e").equals("hi!"),
                "or a string");

        assertTrue(object.getArray("array").isArray(),
                "works for arrays as well");
        assertTrue(object.getObject("a","b").isObject(),
                "and objects");

        // builders are nice, but still feels kind of repetitive
        // getOrCreate will assume you want the object and will create it and everything on its path for you.
        object.getOrCreateObject("1","2","3","4").put("5", "xxx");
        assertTrue(object.getString("1","2","3","4","5").equals("xxx"),
                "yep, we just added a string value 5 levels deep that did not exist so far");
        object.getOrCreateArray("5","4","3","2","1").add("xxx");
        assertTrue(object.getArray("5","4","3","2","1").contains("xxx"),
                "naturally it works for arrays too");

        // Lets do some other stuff
        assertTrue(object.equals(object),
                "equals is implemented as a deep equals");
        assertTrue(
                object().put("a", 1).put("b", 2).get()
                .equals(
                        object().put("b", 2).put("a", 1).get()),
                "true for objects as well");

        // arrays are a bit more flexible than ordinary lists
        JsonArray array = array("foo", "bar");
        assertTrue(array.get(1) == array.get("bar"),
                "returns the same object");
        assertTrue(array.contains(primitive("foo")),
                "obviously this works");
        assertTrue(array.contains("foo"),
                "but this works as well");

        // cloning is supported too
        JsonObject deepClone = object.deepClone();
        assertTrue(object.equals(deepClone));
        // deepClone is just a more convenient API than clone but of course Cloneable is implemented on all json elements
        assertTrue(object.equals(deepClone.clone()));

        // serialize like this
        String serialized = serialize(object);

        // parse it
        JsonElement json = jsonParser.parse(serialized);

        // and write it straight to some stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        serialize(bos, json, false);
        // or pretty print it like this

        String pretty=serialize(json, true);

        assertThat( pretty).isEqualTo(serialize(jsonParser.parse(pretty),true));
        assertThat(new String(bos.toByteArray(),"UTF-8")).isEqualTo(serialize(jsonParser.parse(bos.toString("UTF-8"))));
        assertThat(serialize(object)).isEqualTo(serialize(jsonParser.parse(serialize(object))));

        assertTrue(object.equals(jsonParser.parse(serialize(object))),
                "input is the same as output");

        assertTrue(serialize(object).equals(serialize(jsonParser.parse((serialize(object))))),
                "the same as in string equals");
    }
}
