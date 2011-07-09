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
import static com.github.jsonj.tools.JsonBuilder.object;
import static com.github.jsonj.tools.JsonBuilder.primitive;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonPrimitive;

public class JsonPrimitiveTest {

	@DataProvider
	public JsonPrimitive[][] primitives() {
		return new JsonPrimitive[][] {
				{primitive(true)}, 
				{primitive(false)},
				{primitive(42)},
				{primitive(42.0)},
				{primitive("abc")}, 
				{primitive((Object)null)}
		};
	}

	@Test(dataProvider="primitives")
	public void shouldBeEqualWithSelf(final JsonElement element) {
		Assert.assertTrue(element.equals(element));
	}

	@Test(dataProvider="primitives")
	public void shouldHaveSameHashcode(final JsonElement element) {
		Assert.assertEquals(element.hashCode(), element.hashCode());
	}

	@DataProvider
	public Object[][] notEqual() {
		return new Object[][] {
				{primitive(true), primitive(false)},
				{primitive(true), primitive("true")},
				{primitive("true"), primitive(true)},
				{primitive(42), primitive(41)},
				{primitive("abc"), primitive(" abc")},
				{primitive("x"), array("x")},
				{primitive("x"), object().put("x", "x").get()},
				{primitive("x"), null}
		};
	}

	@Test(dataProvider="notEqual")
	public void shouldNotBeEqual(final JsonPrimitive primitive, final Object o) {
		Assert.assertNotSame(primitive, o);
	}

	
	@Test(dataProvider="primitives")
	public void shouldBeEqualWithClone(JsonPrimitive p) {
		Assert.assertTrue(p.equals(p.clone()));
	}
}
