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

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.jsonj.tools.JsonSerializer;

@Test
public class JsonArrayTest {

    public void shouldGetByLabel() {
        JsonElement element = array("foo", "bar").get("bar");
        Assert.assertNotNull(element);
        Assert.assertEquals("bar", element.asPrimitive().asString());
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
        return new Object[][] { { new JsonArray(), new JsonArray() }, { array("foo", "bar"), array("foo", "bar") }, // same
                                                                                                                    // because
                                                                                                                    // we
                                                                                                                    // don't
                                                                                                                    // care
                                                                                                                    // about
                                                                                                                    // the
                                                                                                                    // order
                { array("foo", "bar"), array("bar", "foo") } // same because we don't care about the order
        };
    }

    @Test(dataProvider = "equalPairs")
    public void shouldBeEqual(final JsonElement left, final JsonElement right) {
        Assert.assertTrue(left.equals(left)); // reflexive
        Assert.assertTrue(right.equals(right)); // reflexive
        Assert.assertTrue(left.equals(right)); // symmetric
        Assert.assertTrue(right.equals(left)); // symmetric
    }

    @Test(dataProvider = "equalPairs")
    public void shouldHaveSameHashCode(final JsonElement left, final JsonElement right) {
        Assert.assertEquals(left.hashCode(), right.hashCode());
    }

    @DataProvider
    public Object[][] unEqualPairs() {
        return new Object[][] { { array("foo"), new JsonArray() }, { array("foo"), array("foo", "foo") }, // different
                                                                                                          // because
                                                                                                          // second
                                                                                                          // array has
                                                                                                          // more
                                                                                                          // elements
                { array("foo"), array("bar") }, // element is different
                { array("foo", "bar"), array("foo", "bbbbbar") } // not same
        };
    }

    @Test(dataProvider = "unEqualPairs")
    public void shouldNotBeEqual(final JsonArray left, final JsonArray right) {
        Assert.assertFalse(left.equals(right));
    }

    public void shouldDoDeepClone() {
        JsonArray a = array(1, 2, 3);
        JsonArray cloneOfA = a.deepClone();
        Assert.assertTrue(a.equals(cloneOfA), "a's clone should be equal");
        a.remove(1);
        Assert.assertFalse(a.equals(cloneOfA), "a was modified so clone is different");
        JsonArray b = array(cloneOfA);
        Assert.assertTrue(b.equals(b.clone()), "b's clone should be equal");
        Assert.assertTrue(b.equals(cloneOfA), "b's clone should to clone of A");
    }

    public void shouldRemoveEmpty() {
        JsonArray array = array(object().get(), array(), nullValue());
        Assert.assertTrue(array.isEmpty(), "array should be empty");
        array.removeEmpty();
        Assert.assertEquals(array.size(), 0);
    }

    @Test
    public void testJsonArrayUsage() {
        JsonObject jsonObject = object().put("display_name", "to becreated").put("first_name", "to").put("last_name", "becreated")
                .put("email", "tobecreated@surroundly.com").put("description", "UserDAOTest to be created").get();

        JsonArray jsonArray = new JsonArray();
        jsonArray.add(jsonObject);

        String lookingFor = jsonObject.get("display_name").toString();
        boolean found = false;
        for (JsonElement jsonElement : jsonArray) {
            String current = jsonElement.asObject().get("display_name").toString();
            if (current.equals(lookingFor)) {
                found = true;
            }
        }
        Assert.assertTrue(found, "jsonObject with display name is found in jsonArray");

        // try a different approach to building the array
        // assume a different library (e.g jdbctemplate) has returned an ArrayList of JsonObjects

        List<JsonObject> results = new ArrayList<JsonObject>();
        results.add(jsonObject);
        jsonArray = new JsonArray();
        // Is there a bug with how addAll is implemented?
        jsonArray.addAll(results);

        found = false;
        System.out.println(JsonSerializer.serialize(jsonArray));
        for (JsonElement jsonElement : jsonArray) {
            String current = jsonElement.asObject().get("display_name").toString();
            if (current.equals(lookingFor)) {
                found = true;
            }
        }
        Assert.assertTrue(found, "jsonObject with display name is found in jsonArray created from ArrayList ");

    }
}
