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
import static com.github.jsonj.tools.JsonBuilder.fromObject;
import static com.github.jsonj.tools.JsonBuilder.object;
import static com.github.jsonj.tools.JsonBuilder.primitive;
import static com.github.jsonj.tools.JsonBuilder.set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonPrimitive;
import com.github.jsonj.JsonSet;

@Test
public class JsonBuilderTest {
    public void shouldConstructInteger() {
        JsonPrimitive p1 = primitive(1234);
        double d1 = p1.asDouble();
        JsonPrimitive p2 = primitive(d1);
        Assert.assertEquals(1234, p2.asInt());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void shouldConvertCollectionToJsonElement() {
        Map m = new HashMap();
        List l = new LinkedList();
        l.add("some string");
        l.add(Integer.valueOf(1));
        m.put("list", l);
        m.put("nr", Double.valueOf(0.2));
        assertThat(fromObject(m).asObject(), is(object().put("nr", 0.2).put("list", array(primitive("some string"), primitive(1))).get()));
    }

    public void shouldConvertPrimitiveProperly() {
        assertThat(primitive(primitive("42")), is(primitive("42")));
    }

    public void shouldConvertPrimitiveProperlyOnFromObject() {
        assertThat(fromObject(primitive("42")), is((JsonElement)primitive("42")));
    }

    public void shouldCreateSet() {
        JsonSet set = set(1,2,3);
        assertThat(set, is(new JsonSet(Arrays.asList(1,2,3))));
        assertThat(set.size(), is(3));
    }
}
