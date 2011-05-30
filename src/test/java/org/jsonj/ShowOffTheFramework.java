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

import java.io.IOException;

import org.jsonj.tools.JsonParser;
import org.testng.annotations.Test;

/**
 * Not really a test but a nice place to show off some how to use this.
 */
@Test
public class ShowOffTheFramework {

	/** this could be a singleton or a spring injected object, threadsafe of course. */
	private final JsonParser jsonParser = new JsonParser();;

	public void whatCanThisBabyDo() throws IOException {
		JsonObject object = object()
			.put("its", "just a hash map")
			.put("and", array(primitive("adding"), primitive("stuff"), object().put("is", "easy").get(), array("another array")))
			.put("numbers", 42)
			.put("including_this_one", 42.0)
			.put("booleanstoo", true)
			.put("nulls_if_you_insist", nullValue())
			.put("arrays_are_easy", array("1", "2", "etc", "varargs are nice")).get();

		// serialize like this
		String serialized = serialize(object);

		System.out.println(serialized);

		// parse it
		JsonElement json = jsonParser.parse(serialized);

		// and write it straight to some stream
		write(System.out, json, false);

		// or pretty print it like this
		System.out.println("\n" + serialize(json, true));

	}
}
