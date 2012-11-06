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
package com.github.jsonj.tools;

import static com.github.jsonj.tools.JsonBuilder.array;
import static com.github.jsonj.tools.JsonBuilder.object;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonObject;

@Test
public class JsonSerializerTest {
	private final JsonParser jsonParser = new JsonParser();

	public void shouldDoSerializeParseRoundTrip() {
		JsonObject original = object().put("a", object().get()).put("b", "test").putArray("c", "1","2","3").get();
		String json = JsonSerializer.serialize(original, true);
		String json2 = JsonSerializer.serialize(original, false);
		// there should be a difference (pretty printing)
		Assert.assertNotSame(json, json2);
		// if we parse it back and reprint it they should be identical to each other and the original
		Assert.assertEquals(
				JsonSerializer.serialize(jsonParser.parse(json), false),
				JsonSerializer.serialize(jsonParser.parse(json2), false));
		Assert.assertEquals(
				JsonSerializer.serialize(original, false),
				JsonSerializer.serialize(jsonParser.parse(json2), false));
	}
	
	@DataProvider
	public Object[][] strings() {
	    return new Object[][] {
                {"x"},
                {"xx"},
                {"xxx"},
                {"xxxx"},
                {"fooo\n"},
                {"fooo\nx"},
                {"fooo\nxx"},
                {"fooo\nxxx"},
                {"\tx"},
                {"\txx"},
                {"\txxx"},
                {"\txxxx"},
	            {"e^r is irrational for r\\in\\mathbbQ\\setminus\\0\\"},
	            {"\"value\""},
	            {"'value'"},
	            {"\"'\t\n\r"},
	            {"\\\\\\"}
	    };
	}
	
	@Test(dataProvider="strings")
	public void shouldParseSerializedAndHandleEscapingBothWays(String text) {
        JsonElement e = object().put(text, "value").put("stringval", text).put("array", array(text,text)).get();
	    
	    String json = JsonSerializer.serialize(e);
	    JsonElement parsed = new JsonParser().parse(json);
	    Assert.assertEquals(e, parsed);
	}
	
    @Test(dataProvider="strings")
	public void shouldEscapeAndUnescape(String string) {
        String escaped = JsonSerializer.jsonEscape(string);
        String unEscaped = JsonSerializer.jsonUnescape(escaped);
        assertThat(unEscaped, is(string));
	}
}
