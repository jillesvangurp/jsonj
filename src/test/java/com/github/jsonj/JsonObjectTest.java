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

import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.jsonj.exceptions.JsonTypeMismatchException;

@Test
public class JsonObjectTest {
	@DataProvider
	public Object[][] equalPairs() {
		return new Object[][] {
			{ object().put("a", object().put("b", object().put("c", "d").get()).get()).get(),
				object().put("a", object().put("b", object().put("c", "d").get()).get()).get() },
			{ object().get(), object().get() },
			{ object().put("a", "a").put("b", 42).put("c", true).put("d", array("foo", "bar")).put("e", primitive((String) null)).get(),
				object().put("b", 42).put("a", "a").put("c", true).put("d", array("foo", "bar")).put("e", primitive((String) null)).get() } };
	}

	@Test(dataProvider = "equalPairs")
	public void shouldBeEqualToSelf(final JsonObject left, final JsonObject right) {
		Assert.assertTrue(left.equals(left)); // reflexive
		Assert.assertTrue(right.equals(right)); // reflexive
		Assert.assertTrue(left.equals(right)); // symmetric
		Assert.assertTrue(right.equals(left)); // symmetric
	}

	@Test(dataProvider="equalPairs")
	public void shouldHaveSameHashCode(final JsonObject left, final JsonObject right) {
		Assert.assertEquals(left.hashCode(), right.hashCode());
	}

	@DataProvider
	public Object[][] unEqualPairs() {
		return new Object[][] {
				{ object().put("a", "b").get(),
					object().put("a", "b").put("b", 42).get() },
					{ object().put("a", "b").get(), null },
					{ object().put("a", "b").get(), primitive(42) },
					{ object().put("a", 42).get(), object().put("a", 41).get() } };
	}

	@Test(dataProvider = "unEqualPairs")
	public void shouldNotBeEqual(final JsonObject o, final JsonElement e) {
		Assert.assertNotSame(o, e);
	}

	public void shouldExtractValue() {
		JsonObject o = object().put("a",
				object().put("b", object().put("c", "d").get()).get()).get();
		Assert.assertEquals("d", o.get("a", "b", "c").asPrimitive().asString());
	}

	public void shouldCreateArray() {
		JsonObject object = new JsonObject();
		JsonArray createdArray = object.getOrCreateArray("a","b","c");
		createdArray.add("1");
		Assert.assertTrue(object.getArray("a","b","c").contains("1"), "array should have been added to the object");
	}

	public void shouldReturnExistingArray() {
		JsonObject object = object().put("a", object().put("b", array("foo")).get()).get();
		Assert.assertTrue(object.getOrCreateArray("a","b").contains("foo"));
	}

	@Test(expectedExceptions=JsonTypeMismatchException.class)
	public void shouldThrowExceptionOnElementThatIsNotAnArray() {
		JsonObject object = object().put("a", object().put("b", 42).get()).get();
		object.getOrCreateArray("a","b");
	}

	public void shouldCreateObject() {
		JsonObject object = new JsonObject();
		JsonObject createdObject = object.getOrCreateObject("a","b","c");
		createdObject.put("foo", "bar");
		Assert.assertTrue(object.getString("a","b","c", "foo").equals("bar"), "object should have been added");
	}

	public void shouldReturnExistingObject() {
		JsonObject object = object().put("a", object().put("b", object().put("foo","bar").get()).get()).get();
		JsonObject orCreateObject = object.getOrCreateObject("a","b");
		Assert.assertTrue(orCreateObject.getString("foo").equals("bar"), "return the object with foo=bar");
	}

	@Test(expectedExceptions=JsonTypeMismatchException.class)
	public void shouldThrowExceptionOnElementThatIsNotAnObject() {
		JsonObject object = object().put("a", object().put("b", 42).get()).get();
		object.getOrCreateObject("a","b");
	}

	public void shouldDoDeepClone() {
		JsonObject o = object().put("1", 42).put("2", "Hello world").get();
		JsonObject cloneOfO = o.deepClone();
		Assert.assertTrue(o.equals(cloneOfO));
		o.remove("1");
		Assert.assertFalse(o.equals(cloneOfO));
		o.put("1", cloneOfO);
		Object clone = o.clone();
		Assert.assertTrue(o.equals(clone));
		cloneOfO.remove("2");
		Assert.assertFalse(o.equals(clone));
	}

	public void shouldSortOnKey() {
		JsonObject unsorted = object().put("c", "c").put("a", "a").put("b", "b").get();
		JsonObject sorted = unsorted.sort();
		Set<String> ks = sorted.keySet();
		int i = 0;
		String[] keys = new String[] {"a","b","c"};
		for (String k : ks) {
			Assert.assertTrue(k.equals(keys[i++]));
		}
	}

	public void shouldRemoveEmptyElements() {
		JsonObject jsonObject = object().put("empty", object().get()).put("empty2", nullValue()).put("empty3", new JsonArray()).get();
		Assert.assertTrue(jsonObject.isEmpty(), "object should be empty");
		jsonObject.removeEmpty();
		Assert.assertEquals(jsonObject.getString("empty"), null);
		Assert.assertEquals(jsonObject.getString("empty2"), null);
		Assert.assertEquals(jsonObject.getString("empty3"), null);
	}

}
