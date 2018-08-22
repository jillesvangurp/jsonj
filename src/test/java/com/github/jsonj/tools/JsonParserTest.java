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
import static com.github.jsonj.tools.JsonBuilder.nullValue;
import static com.github.jsonj.tools.JsonBuilder.object;
import static com.github.jsonj.tools.JsonBuilder.primitive;
import static com.github.jsonj.tools.JsonBuilder.set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.jsonj.JsonArray;
import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonObject;
import com.github.jsonj.JsonSet;
import com.github.jsonj.JsonjCollectors;
import com.github.jsonj.exceptions.JsonParseException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test
public class JsonParserTest {
    private final JsonParser jsonParser = new JsonParser();

    @DataProvider
    public @Nonnull Object[][] goodJson() {
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
                {array("1","2")},
                {object()
                    .put("its", "just a hash map")
                    .put("and", array(primitive("adding"), primitive("stuff"), object().put("is", "easy").get(), array("another array")))
                    .put("numbers", 42)
                    .put("including_this_one", 42.0)
                    .put("booleanstoo", true)
                    .put("nulls_if_you_insist", nullValue())
                    .put("arrays_are_easy", array("1", "2", "etc", "varargs are nice")).get()},
                    {object().put("o1", object().get()).put("o2", object().put("a1", array()).get()).get()},
        };
    }

    @Test(dataProvider="goodJson")
    public void shouldParse(@Nonnull  JsonElement element) {
        String input = JsonSerializer.serialize(element, false);
        JsonElement parsed = jsonParser.parse(input);
        Assert.assertEquals(JsonSerializer.serialize(parsed, false), input);
        // check with the other parser as well
    }

    @Test
    public void shouldParseConcurrently() throws InterruptedException, ExecutionException, TimeoutException {
        JsonObject json = object().put("b","c").put("a", array("1","2")).put("c", object().get()).get();
        final String input = JsonSerializer.serialize(json, false);

        ExecutorService tp = Executors.newFixedThreadPool(100);

        Queue<Callable<Boolean>> tasks = new ConcurrentLinkedQueue<Callable<Boolean>>();
        for(int i = 0; i<100000; i++) {
            tasks.add(() -> {
                JsonElement output;
                output = jsonParser.parse(input);
                String serialized = JsonSerializer.serialize(output, false);
                return input.equals(serialized);
            });
        }
        List<Future<Boolean>> results = tp.invokeAll(tasks);
        for (Future<Boolean> future : results) {
            Assert.assertTrue(future.get(1, TimeUnit.SECONDS));
        }
        tp.shutdown();
        tp.awaitTermination(10, TimeUnit.SECONDS);
    }

    public void shouldParseLongValue() {
        long l=Long.MAX_VALUE;
        assertThat(jsonParser.parse(""+l).asLong(), is(l));
    }

    @DataProvider
    public Object[][] malformedJson() {
        return new String[][] {
                {"samplejson/test_malformed_1.json"},
                {"samplejson/test_malformed_2.json"},
                {"samplejson/test_malformed_3.json"}
        };
    }

    @Test(expectedExceptions=JsonParseException.class, dataProvider="malformedJson")
    public void shouldNotParseMalformedJson(String resource) throws IOException {
        parseResource(resource);
    }

    private void parseResource(String resource) throws IOException {
        jsonParser.parse(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(resource), StandardCharsets.UTF_8));
    }

    @Test(expectedExceptions=JsonParseException.class)
    public void shouldNotParseEmptyString() {
        jsonParser.parse("");
    }

    public void shouldParseEmoji() {
        String stringWithEmoji = "\"qweqw \\ud83d\\ude00 a\"";
        JsonElement parsed = jsonParser.parse(stringWithEmoji);
        assertThat(parsed.toString().toLowerCase(Locale.ROOT), is(stringWithEmoji));
    }

    public void shouldParseHugeObjects() {
        // test for an obscure pretty printing bug that was caused by using the raw outputstream instead of the buffered writer for newlines
        // only triggered once you go beyond the buffer size for the buffered writer; the newline appears in the wrong place then
        // keep this test around to ensure we don't regress on this
        JsonObject o=new JsonObject();
        // generate a sufficiently large json object
        for(int i=0; i<10; i++) {
            JsonSet s=set();
            for(int j=0; j<100; j++) {
                s.add(UUID.randomUUID().toString());
            }
            o.put(UUID.randomUUID().toString(), s);
        }
        // this should not throw an exception
        jsonParser.parseObject(o.prettyPrint());
    }

    public void shouldStreamJson() {
        String input="";
        for(int i=0;i<10;i++) {
            input+="# this is a comment\n";
            input+=object(field("id",i)) + "\n";
            input+="\t\n"; // a few variations of whitespace
            input+="    \n";
            input+="\n";
        }
        Stream<JsonObject> stream = jsonParser.parseJsonLines(new StringReader(input));
        JsonArray all = stream.collect(JsonjCollectors.array());
        assertThat(all.size()).isEqualTo(10);
    }
}
