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
import static org.jsonj.tools.JsonBuilder.object;
import static org.jsonj.tools.JsonBuilder.primitive;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test
public class JsonArrayTest {

	public void shouldGetByLabel() {
		JsonElement element = array("foo", "bar").get("bar");
		Assert.assertNotNull(element);
		Assert.assertEquals("bar", element.toString());
	}

	public void shouldGetObjectByIndex() {
		JsonElement element = array(primitive("foo"), object().put("bar", true).get()).get("1");
		Assert.assertNotNull(element);
		Assert.assertTrue(element.isObject());
	}

	public void shouldReturnNull() {
		JsonElement element = array(primitive("foo"), object().put("bar", true).get()).get("x");
		Assert.assertNull(element);
	}

	@DataProvider
	public Object[][] equalPairs() {
		return new Object[][] {
				{new JsonArray(), new JsonArray()},
				{array("foo","bar"), array("foo", "bar")}, // same because we don't care about the order
				{array("foo","bar"), array("bar", "foo")} // same because we don't care about the order
		};
	}

	@Test(dataProvider="equalPairs")
	public void shouldBeEqual(final JsonArray left, final JsonArray right) {
		Assert.assertTrue(left.equals(left)); // reflexive
		Assert.assertTrue(right.equals(right)); // reflexive
		Assert.assertTrue(left.equals(right)); // symmetric
		Assert.assertTrue(right.equals(left)); // symmetric
	}

	@Test(dataProvider="equalPairs")
	public void shouldHaveSameHashCode(final JsonArray left, final JsonArray right) {
		Assert.assertEquals(left.hashCode(), right.hashCode());
	}

	@DataProvider
	public Object[][] unEqualPairs() {
		return new Object[][] {
				{array("foo"), new JsonArray()},
				{array("foo"), array("foo", "foo")}, // different because second array has more elements
				{array("foo"), array("bar")}, // element is different
				{array("foo","bar"), array("foo", "bbbbbar")} // not same
		};
	}
	@Test(dataProvider="unEqualPairs")
	public void shouldNotBeEqual(final JsonArray left, final JsonArray right) {
		Assert.assertFalse(left.equals(right));
	}
}
