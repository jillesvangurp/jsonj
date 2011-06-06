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

import static org.jsonj.tools.JsonBuilder.array;
import static org.jsonj.tools.JsonBuilder.nullValue;
import static org.jsonj.tools.JsonBuilder.object;
import static org.jsonj.tools.JsonBuilder.primitive;
import static org.jsonj.tools.JsonSerializer.serialize;
import static org.jsonj.tools.JsonSerializer.write;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.jsonj.tools.JsonParser;
import org.testng.annotations.Test;

/**
 * Not really a test but a nice place to show off how to use the framework.
 */
@Test
public class ShowOffTheFramework {

	/** this could be a singleton or a spring injected object, threadsafe of course. */
	private final JsonParser jsonParser = new JsonParser();;

	public void whatCanThisBabyDo() throws IOException {
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
		JsonObject anotherObject = object.getOrCreateObject("1","2","3","4");
		anotherObject.put("5", "xxx");
		assertTrue(object.getString("1","2","3","4","5").equals("xxx"),
			"yep, we just added a string value 5 levels deep");

		JsonArray anotherArray = object.getOrCreateArray("5","4","3","2","1");
		anotherArray.add("xxx");
		assertTrue(object.getArray("5","4","3","2","1").contains("xxx"),
			"naturally it works for arrays too");

		// Lets do some other stuff
		assertTrue(object.equals(object),
			"equals is implemented as a deep equals");
		assertTrue(array("a", "b").equals(array("b", "a")),
			"mostly you shouldn't care about the order of stuff in json");
		assertTrue(
			object().put("a", 1).put("b", 2).get()
			.equals(
				object().put("b", 2).put("a", 1).get()),
			"true for objects as well");

		// Arrays are lists
		JsonArray array = array("foo", "bar");

		assertTrue(array.get(1) == array.get("bar"),
			"returns the same object");
		assertTrue(array.contains(primitive("foo")),
				"obviously this works");
		assertTrue(array.contains("foo"),
			"but this works as well");

		// serialize like this
		String serialized = serialize(object);

		// parse it
		JsonElement json = jsonParser.parse(serialized);

		// and write it straight to some stream
		write(System.out, json, false);

		// or pretty print it like this
		System.out.println("\n" + serialize(json, true));

		assert serialize(object).equals(serialize(jsonParser.parse(serialize(object))));

		assertTrue(object.equals(jsonParser.parse(serialize(object))),
			"input is the same as output");
	}
}
