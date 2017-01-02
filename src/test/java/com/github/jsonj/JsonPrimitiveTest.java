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

import static com.github.jsonj.assertions.JsonJAssertions.assertThat;
import static com.github.jsonj.tools.JsonBuilder.array;
import static com.github.jsonj.tools.JsonBuilder.nullValue;
import static com.github.jsonj.tools.JsonBuilder.object;
import static com.github.jsonj.tools.JsonBuilder.primitive;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.github.jsonj.tools.JsonParser;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test
public class JsonPrimitiveTest {
   JsonParser parser = new JsonParser();


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

    @DataProvider
    public Object[][] numbers() {
        return new Object[][]{
            {"123456", Long.class},
            {"666.66", Double.class},
            {"66.666", Double.class},
            {"99999.9", Double.class},
            {"9.99999", Double.class},
            {"99.9999", Double.class},
            {"999.999", Double.class},
            {"9999.99", Double.class},
            {"99999.9", Double.class},
            {"1111111111111111111111111111111111111111111111111111",BigInteger.class},
            {"1111111111111111111111111111111111111111111111111111.000000000000000000000000000000000000000000000000000",BigDecimal.class}
        };
    }


    @Test(dataProvider="numbers")
    public void shouldHandleVeryLargeNumbers(String input, Class<?> expectedType) {
        JsonPrimitive primitive = parser.parse(input).asPrimitive();
        assertThat(primitive.asString()).isEqualTo(input);
        assertThat(primitive.value().getClass()).isEqualTo(expectedType);
    }

    @Test(dataProvider="primitives")
    public void shouldBeEqualWithSelf(final JsonElement element) {
        Assert.assertTrue(element.equals(element));
    }

    @Test(dataProvider="primitives")
    public void shouldHaveSameHashcode(final JsonElement element) {
        Assert.assertEquals(element.hashCode(), element.hashCode());
    }

    @Test
    public void shouldGetNumber() {
        JsonPrimitive primitive = primitive(42);
        assertThat(primitive.asNumber().intValue(), is(42));
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

    public void shouldReturnAsString() {
        assertThat(primitive(1).asString(), is("1"));
    }

    public void shouldReturnJsonString() {
        assertThat(primitive(1).toString(), is("1"));
    }

    public void shouldHandleUtf8Correctly() {
        assertThat(primitive("hello").asString(), is("hello"));
        assertThat(((JsonPrimitive) primitive("hello").clone()).asString(), is("hello"));
        assertThat(primitive("hello"), is(primitive("hello")));
    }

    public void shouldHandleConversions() {
        JsonPrimitive pi = primitive(Math.PI);
        assertThat(pi.asLong(), is(3l));
        assertThat(pi.asInt(), is(3));
        assertThat(pi.asDouble(), is(Math.PI));
        assertThat(pi.asString(), is(""+Math.PI));
    }

    public void shouldConsiderNullEqualToNull() {
        assertThat(nullValue(), is(nullValue()));
    }

    public void shouldUseAssertJ() {
        assertThat(primitive(1)).isEqualTo(1).isNotEqualTo(2);
    }

    public void shouldParseStringWhenNeeded() {
        JsonElement e = new JsonPrimitive("42"); // a string
        assertThat(e.asInt()).isEqualTo(42);
        assertThat(e.asFloat()).isEqualTo(42.0f);
        assertThat(e.asString()).isEqualTo("42");
        assertThat(e.asNumber().toString()).isEqualTo("42");
    }
}
