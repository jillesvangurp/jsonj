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
package org.jsonj.tools;

import static org.jsonj.tools.JsonBuilder.array;
import static org.jsonj.tools.JsonBuilder.object;
import static org.jsonj.tools.JsonBuilder.primitive;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jsonj.JsonArray;
import org.jsonj.JsonElement;
import org.jsonj.JsonObject;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class JsonParserTest {
	private final JsonParser jsonParser = new JsonParser();

	@DataProvider
	public Object[][] goodJson() {
		return new Object[][] {
				{object().put("a", 1).get()},
				{object().put("a", true).get()},
				{object().put("a", "text").get()},
				{object().put("a", "text\n\t").get()},
				{object().put("a", (String)null).get()},
				{object().put("a", array("1")).get()},
				{object().put("a", array("1","2")).get()},
				{object().put("a", array("1","2")).put("b","c").get()},
				{object().put("b","c").put("a", array("1","2")).get()},
				{object().put("b","c").put("a", array("1","2")).put("c", object().get()).get()},
				{object().get()},
				{new JsonArray()},
				{primitive(true)},
				{primitive(false)},
				{primitive(42)},
				{primitive(42.0)},
				{primitive("foo")},
				{array("1")},
				{array("1","2")}
		};
	}

	@Test(dataProvider="goodJson")
	public void shouldParse(final JsonElement element) {
		String input = JsonSerializer.serialize(element, false);
		JsonElement parsed = jsonParser.parse(input);
		Assert.assertEquals(input, JsonSerializer.serialize(parsed, false));
	}

	@Test
	public void shouldParseConcurrently() throws InterruptedException, ExecutionException, TimeoutException {
		JsonObject json = object().put("b","c").put("a", array("1","2")).put("c", object().get()).get();
		final String input = JsonSerializer.serialize(json, false);

		ExecutorService tp = Executors.newFixedThreadPool(100);

		Queue<Callable<Boolean>> tasks = new ConcurrentLinkedQueue<Callable<Boolean>>();
		for(int i = 0; i<100000; i++) {
			tasks.add(new Callable<Boolean>() {

				@Override
				public Boolean call() throws Exception {
					JsonElement output;
					output = jsonParser.parse(input);
					String serialized = JsonSerializer.serialize(output, false);
					return input.equals(serialized);
				}
			});
		}
		List<Future<Boolean>> results = tp.invokeAll(tasks);
		for (Future<Boolean> future : results) {
			Assert.assertTrue(future.get(1, TimeUnit.SECONDS));
		}
		tp.shutdown();
		tp.awaitTermination(10, TimeUnit.SECONDS);
	}
}
