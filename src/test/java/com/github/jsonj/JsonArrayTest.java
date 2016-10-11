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
import static com.github.jsonj.tools.JsonBuilder.field;
import static com.github.jsonj.tools.JsonBuilder.nullValue;
import static com.github.jsonj.tools.JsonBuilder.object;
import static com.github.jsonj.tools.JsonBuilder.primitive;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.github.jsonj.tools.JsonBuilder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test
public class JsonArrayTest {

    public void shouldGetByLabel() {
        JsonElement element = array("foo", "bar").get("bar");
        AssertJUnit.assertNotNull(element);
        AssertJUnit.assertEquals("bar", element.asPrimitive().asString());
    }

    public void shouldGetObjectByIndex() {
        JsonElement element = array(primitive("foo"), object().put("bar", true).get()).get("1");
        AssertJUnit.assertNotNull(element);
        AssertJUnit.assertTrue(element.isObject());
    }

    public void shouldReturnNull() {
        JsonElement element = array(primitive("foo"), object().put("bar", true).get()).get("x");
        AssertJUnit.assertNull(element);
    }

    public void shouldSupportArrayOfOptionals() {
        assertThat(array(Optional.of(array(1,2)),Optional.of(3))).isEqualTo(array(array(1,2),3));
    }

    @DataProvider
    public Object[][] equalPairs() {
        return new Object[][] {
                { new JsonArray(), new JsonArray() },
                { array("bar", "bar"), array("bar", "bar") },
                { array("foo", "bar"), array("foo", "bar") }
        };
    }

    @Test(dataProvider = "equalPairs")
    public void shouldBeEqual(final JsonElement left, final JsonElement right) {
        AssertJUnit.assertTrue(left.equals(left)); // reflexive
        AssertJUnit.assertTrue(right.equals(right)); // reflexive
        AssertJUnit.assertTrue(left.equals(right)); // symmetric
        AssertJUnit.assertTrue(right.equals(left)); // symmetric
    }

    @Test(dataProvider = "equalPairs")
    public void shouldHaveSameHashCode(final JsonElement left, final JsonElement right) {
        AssertJUnit.assertEquals(left.hashCode(), right.hashCode());
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
        AssertJUnit.assertFalse(left.equals(right));
    }

    public void shouldDoDeepClone() {
        JsonArray a = array(1, 2, 3);
        JsonArray cloneOfA = a.deepClone();
        assertThat(a).isEqualTo(cloneOfA);
        a.remove(1);
        assertThat(a).isNotEqualTo(cloneOfA);
        JsonArray b = array();
        b.addAll(cloneOfA);
        assertThat(b).isEqualTo(b.clone());
        assertThat(b).isEqualTo(cloneOfA);
    }

    public void shouldRemoveEmpty() {
        JsonArray array = array(object().get(), array(), nullValue());
        assertThat(array).isEmpty();
        array.removeEmpty();
        AssertJUnit.assertEquals(array.size(), 0);
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
        assertThat(found).isTrue();

        // try a different approach to building the array
        // assume a different library (e.g jdbctemplate) has returned an ArrayList of JsonObjects

        List<JsonObject> results = new ArrayList<JsonObject>();
        results.add(jsonObject);
        jsonArray = new JsonArray();
        // Is there a bug with how addAll is implemented?
        jsonArray.addAll(results);

        found = false;
        for (JsonElement jsonElement : jsonArray) {
            String current = jsonElement.asObject().get("display_name").toString();
            if (current.equals(lookingFor)) {
                found = true;
            }
        }
        assertThat(found).isTrue();
    }

    public void shouldConvertToDoubleArray() {
        assertThat(array(0.1,0.2).asDoubleArray(), is(new double[]{0.1,0.2}));
    }

    public void shouldConvertToIntArray() {
        assertThat(array(1,2).asIntArray(), is(new int[]{1,2}));
    }

    public void shouldConvertToStringArray() {
        assertThat(array("1","2").asStringArray(), is(new String[]{"1","2"}));
    }

    public void shouldMaybeGetThings() {
        assertThat(array("1","2").maybeGet(0).isPresent()).isTrue();
        assertThat(array("1","2").maybeGet(5).isPresent()).isFalse();
        assertThat(array("1","2").maybeGetString(1).get()).isEqualTo("2");
    }

    public void shouldSupportJavaSerialization() throws IOException, ClassNotFoundException {
        JsonArray object = array(object().put("42",42).get(), primitive(42));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.close();
        byte[] bytes = baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object object2 = ois.readObject();
        AssertJUnit.assertTrue(object.equals(object2));
    }

    public void shouldIterateOverObjects() {
        JsonObject obj = object().put("foo", "bar").get();
        int i=0;
        for(JsonObject o: array(obj,obj).objects()) {
            assertThat(o.getString("foo"), is("bar"));
            i++;
        }
        assertThat(i,is(2));
    }

    public void shouldIterateOverArrays() {
        JsonArray twodarray = array();
        twodarray.add(array(42,42));
        for(JsonArray arr: twodarray.arrays()) {
            assertThat(arr.get(0).asInt(), is(42));
        }
    }

    public void shouldIterateOverStrings() {
        for(String s: array("hello").strings()) {
            assertThat(s, is("hello"));
        }
    }

    public void shouldIterateOverLongs() {
        for(Long l: array(42).longs()) {
            assertThat(l, is(42l));
        }
    }

    public void shouldIterateOverDoubles() {
        for(Double d: array(42).doubles()) {
            assertThat(d, is(42d));
        }
    }

    public void shouldAddJsonBuilderObjects() {
        JsonBuilder builder = object().put("foo", "bar");
        JsonArray array1 = array(builder,builder);
        JsonArray array2 = array();
        array2.add(builder,builder);
        assertThat(array1, is(array2));
        assertThat(array1.toString(),is("[{\"foo\":\"bar\"},{\"foo\":\"bar\"}]"));
    }

    public void shouldAllowJsonNullValues() {
        JsonArray arr = array(null, nullValue());
        assertThat(arr.get(0), is((JsonElement)nullValue()));
    }

    public void shouldAddNumbers() {
        JsonArray arr = new JsonArray();
        arr.add(1,2,3,4);
        assertThat(arr, is(array(1,2,3,4)));
    }

    public void shouldConvertToSet() {
        assertThat(array(1,2,3,1,2,3).asSet().size(), is(3));
    }

    public void shouldReplaceElement() {
        JsonArray array = array("bar","foo");
        assertThat(array.replace("bar", "rab"), is(true));
        assertThat(array.replace("bar", "rab"), is(false));
        assertThat(array.contains("rab"), is(true));
        assertThat(array.contains("foo"), is(true));
        assertThat(array.contains("bar"), is(false));
    }

    public void shouldReplaceObject() {
        JsonArray array = array(
                object(field("id", 1)),
                object(field("id", 2)),
                object(field("id", 3)),
                object(field("id", 4))
                );
        assertThat(array.replaceObject(object(field("id", 2)), object(field("id", 22)), "id"), is(true));
        assertThat(array.replaceObject(object(field("id", 2)), object(field("id", 22)), "id"), is(false));
        assertThat(array.contains(object(field("id", 22))), is(true));
        assertThat(array.contains(object(field("id", 2))), is(false));
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void shouldThrowExceptionWhenSpecifiedFieldIsMissing() {
        array().replaceObject(object(field("id", 1)), object(field("id", 1)), "Idontexist");
    }

    public void shouldAddJsonElements() {
        JsonArray arr = new JsonArray();
        arr.add(primitive(1),object(field("1", "2")),array(42));
        assertThat(arr.size(), is(3));
        assertThat(arr.get(0).isPrimitive(), is(true));
        assertThat(arr.get(1).isObject(), is(true));
        assertThat(arr.get(2).isArray(), is(true));
    }

    public void shouldAddArrayToArray() {
        assertThat(array(array(1,2,3)).size(), is(1));
    }

    public void shouldAddElementsToArray() {
        assertThat(array(Arrays.asList(1,2,3)).size(), is(3));
        assertThat(array(new Integer[]{1,2,3}).size(), is(3));
        assertThat(array(new int[]{1,2,3}).size(), is(3));
        assertThat(array(new long[]{1,2,3}).size(), is(3));
        assertThat(array(new float[]{1,2,3}).size(), is(3));
        assertThat(array(new double[]{1,2,3}).size(), is(3));
    }

    public void shouldRemovePrimitive() {
        JsonArray array = array("1","2","3");
        array.remove("2");
        assertThat("should be removed as primitive", !array.contains(primitive("2")));
        assertThat("should be removed as primitive", !array.contains("2"));
        array.remove(primitive("3"));
        assertThat("should be removed", !array.contains(primitive("3")));
        assertThat("should be removed", !array.contains("3"));
    }

    @Test(expectedExceptions=IllegalStateException.class)
    public void shouldNotAllowMutations() {
        JsonArray list = array(1).immutableClone();
        list.add(2);
    }

    @Test(expectedExceptions=IllegalStateException.class)
    public void shouldNotAllowMutationsOnElement() {
        JsonArray list = array(array(1)).immutableClone();
        JsonArray l2=list.first().asArray();
        l2.add(2);
    }

    public void shouldFindObjectInArray() {
        JsonArray arr = array(
                null,
                primitive(42),
                object(field("id","1"), field("name", "1")),
                object(field("id","1"), field("name", "2")),
                object(field("id","2"), field("name", "3")),
                object(field("id","2"), field("name", "4")),
                object(field("id","3"), field("name", "5")),
                object(field("id","3"), field("name", "6"))
        );
        assertThat(arr.findFirstWithFieldValue("id", "1").get().getString("name")).isEqualTo("1");
        assertThat(arr.findFirstWithFieldValue("id", "2").get().getString("name")).isEqualTo("3");
        assertThat(arr.findFirstWithFieldValue("id", "3").get().getString("name")).isEqualTo("5");
        assertThat(arr.findFirstWithFieldValue("id", "4").isPresent()).isEqualTo(false);
    }
}
