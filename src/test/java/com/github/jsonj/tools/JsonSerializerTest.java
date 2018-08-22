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
import static com.github.jsonj.tools.JsonBuilder.field;
import static com.github.jsonj.tools.JsonBuilder.object;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nonnull;
import org.hamcrest.Matchers;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test
public class JsonSerializerTest {
    private final JsonParser jsonParser = new JsonParser();

    public void shouldDoSerializeParseRoundTrip() {
        JsonObject original = object().put("a", object().get()).put("b", "test").putArray("c", "1","2","3").get();
        String json = JsonSerializer.serialize(original, true);
        String json2 = JsonSerializer.serialize(original, false);
        // there should be a difference (pretty printing)
        AssertJUnit.assertNotSame(json, json2);
        // if we parse it back and reprint it they should be identical to each other and the original
        AssertJUnit.assertEquals(
                JsonSerializer.serialize(jsonParser.parse(json), false),
                JsonSerializer.serialize(jsonParser.parse(json2), false));
        AssertJUnit.assertEquals(
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
                {"\\\\\\"},
                {"''''"},
                {"Töölö"}
        };
    }

    @Test(dataProvider="strings")
    public void shouldParseSerializedAndHandleEscapingBothWays(@Nonnull String text) {
        JsonElement e = object().put(text, "value").put("stringval", text).put("array", array(text,text)).get();

        String json = JsonSerializer.serialize(e);
        JsonElement parsed = new JsonParser().parse(json);
        AssertJUnit.assertEquals(e, parsed);
    }

    @Test(dataProvider = "strings")
    public void shouldParseSerializedAndHandleEscapingBothWaysWithOutputStream(@Nonnull String text) throws IOException {
        JsonElement e = object().put(text, "value").put("stringval", text).put("array", array(text, text)).get();
        StringWriter sw = new StringWriter();

        JsonSerializer.serialize(e, sw);
        String string = sw.getBuffer().toString();
        JsonElement parsed = new JsonParser().parse(string);
        AssertJUnit.assertEquals(e, parsed);
    }

    public void shouldUseOutputStream() throws IOException {
        JsonObject object = object(field("hi", "wrld"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        object.serialize(bos);
        byte[] string = bos.toByteArray();
        assertThat(string.length, Matchers.greaterThan(0));
        assertThat(jsonParser.parse(bos.toString(StandardCharsets.UTF_8.name())), is(object));
    }

    @Test(dataProvider="strings")
    public void shouldEscapeAndUnescape(String string) throws IOException {
        String escaped = JsonSerializer.jsonEscape(string);

        String unEscaped = JsonSerializer.jsonUnescape(escaped);
        assertThat(unEscaped, is(string));
    }

    public void shouldEscapeControlCharacters() {
        // use separate test for this because StringEscapeUtils doesn't unescape these the way you would expect
        char controlChar = Character.valueOf((char)27);
        String s = ""+controlChar+"controlChars"+controlChar;
        String escaped = JsonSerializer.jsonEscape(s);
        assertThat(escaped, containsString("001B"));

        String primitive = JsonBuilder.primitive(s).toString();
        assertThat(primitive, containsString("001B"));
        String json = object().put("escapeme", s).get().toString();

        assertThat(json, containsString("001B"));
    }
}
