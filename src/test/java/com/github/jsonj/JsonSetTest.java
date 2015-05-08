package com.github.jsonj;

import static com.github.jsonj.tools.JsonBuilder.array;
import static com.github.jsonj.tools.JsonBuilder.field;
import static com.github.jsonj.tools.JsonBuilder.nullValue;
import static com.github.jsonj.tools.JsonBuilder.object;
import static com.github.jsonj.tools.JsonBuilder.primitive;
import static com.github.jsonj.tools.JsonBuilder.set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertTrue;

import com.github.jsonj.assertions.JsonJAssertions;
import com.github.jsonj.tools.JsonBuilder;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import org.testng.annotations.Test;

@Test
public class JsonSetTest {
    public void shouldAddNoDuplicateStrings() {
        JsonSet set = new JsonSet();
        set.add("42");
        set.add("42");
        assertThat(set.size(), is(1));
    }

    public void shouldAddNoDuplicateStringsVarargs() {
        JsonSet set = new JsonSet();
        set.add("42", "42", "43");
        assertThat(set.size(), is(2));
    }

    public void shouldAddNoDuplicatePrimitives() {
        JsonSet set = new JsonSet();
        set.add(primitive("42"), primitive("42"), primitive("43"));
        assertThat(set.size(), is(2));
    }

    public void shouldAddNoDuplicatesFromCollection() {
        List<Serializable> collection = Arrays.asList("42",primitive("42"), array(42),array(42));

        JsonSet set = new JsonSet();
        set.addAll(collection);
        assertThat(set.size(), is(2));
    }

    public void shouldAddNoDuplicatesOnConstruction() {
        List<Serializable> collection = Arrays.asList("42",primitive("42"), array(42),array(42));
        JsonSet set = new JsonSet(collection);
        assertThat(set.size(), is(2));
    }

    public void shouldAddAllElementsOfArray() {
        JsonSet set = new JsonSet();
        set.addAll(array(1,2,1));
        assertThat(set.size(), is(2));
    }

    public void shouldIterateOverObjects() {
        int i=0;
        JsonSet set = new JsonSet();
        set.add(object().put("foo", "bar").get());
        set.add(object().put("bar", "foo").get());
        for(@SuppressWarnings("unused") JsonObject o: set.objects()) {
            i++;
        }
        assertThat(i,is(2));
    }

    public void shouldAddJsonBuilderObjects() {
        JsonBuilder builder = object().put("foo", "bar");
        JsonSet set = new JsonSet();
        set.add(builder,builder);
        assertThat(set.size(), is(1));
        assertThat(set.toString(),is("[{\"foo\":\"bar\"}]"));
    }

    public void shouldSupportNulls() {
        JsonSet set = set(null, nullValue());
        assertTrue(set.contains(nullValue()),"should contain null");
        assertThat(set.size(), is(1));
    }

    public void shouldConvertToArray() {
        JsonArray array = set(1,2,3).asArray();
        array.add(1,2,3);
        assertThat(array.size(), is(6));
    }

    public void shouldAddArrayToArray() {
        assertThat(set(set(1,2,3)).size(), is(1));
        assertThat(set(array(1,2,3)).size(), is(1));
    }

    public void shouldAddElementsToArray() {
        assertThat(set(Arrays.asList(1,2,3)).size(), is(3));
        assertThat(set(new Integer[]{1,2,3}).size(), is(3));
        assertThat(set(new int[]{1,2,3}).size(), is(3));
        assertThat(set(new long[]{1,2,3}).size(), is(3));
        assertThat(set(new float[]{1,2,3}).size(), is(3));
        assertThat(set(new double[]{1,2,3}).size(), is(3));
    }

    public void shouldUseIdStrategy() {
        JsonObject object1 = object(field("id",1), field("value", "foo"));
        JsonObject object2 = object(field("id",2), field("value", "bar"));
        JsonObject object3 = object(field("id",1), field("value", "bar"));
        JsonSet set = set();
        set = set.withIdStrategy("id");
        set.add(object1, object2, object3);
        assertThat(set.size(), is(2));
    }

    public void shouldRemoveDuplicatesAfterSettingStrategy() {
        JsonObject object1 = object(field("id",1), field("value", "foo"));
        JsonObject object2 = object(field("id",1), field("value", "bar"));
        JsonSet set = set();
        set.add(object1, object2);
        set = set.withIdStrategy("id");
        assertThat(set.size(), is(1));
    }

    public void shouldReplaceElementAfterSettingStrategy() {
        JsonObject object1 = object(field("id",1), field("value", "foo"));
        JsonObject object2 = object(field("id",1), field("value", "bar"));
        JsonSet set = set();
        set = set.withIdStrategy("id");
        set.add(object1);
        set.add(object2);
        assertThat(set.size(), is(1));
        assertThat(set.get(0).asObject().getString("value"), is("bar"));
    }

    public void shouldUseAssertJAssertion() {
        JsonJAssertions.assertThat(set(1,2)).shouldContain(2,1).shouldNotContain(3);
    }

    public void shouldRemoveStringValue() {
        JsonSet set = set("foo","bar");
        set.remove("foo");
        assertThat(set.contains("foo")).isFalse();
        assertThat(set.toString().contains("foo")).isFalse();
    }

    public void setWithIdStrategyShouldContain() {
        JsonSet set = set();
        set.withIdStrategy("id");
        set.add(object(field("id","1"),field("name","foo")));
        assertThat(set.contains(object(field("id","1"),field("name","bar")))).isTrue();
    }
}
