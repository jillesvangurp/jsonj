package com.github.jsonj;

import static com.github.jsonj.tools.JsonBuilder.primitive;
import static com.jillesvangurp.efficientstring.EfficientString.fromString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map.Entry;

import org.testng.annotations.Test;

import com.jillesvangurp.efficientstring.EfficientString;

@Test
public class SimpleMapTest {
    public void shouldPutGetRemove() {
        SimpleMap<EfficientString,JsonElement> m = new SimpleMap<>();
        assertThat(m.size(), is(0));
        EfficientString key = fromString("42");
        JsonElement value = primitive(42);
        m.put(key, value);
        assertThat(m.size(), is(1));
        assertThat(m.get(key), is(value));
        m.remove(key);
        assertThat(m.size(), is(0));
        assertThat(m.get(key), nullValue());
    }

    public void shouldIterate() {
        SimpleMap<EfficientString,JsonElement> m = new SimpleMap<>();
        for(int i=0;i<42;i++) {
            m.put(fromString(""+i), primitive(i));
        }
        int i=0;
        for(Entry<EfficientString, JsonElement> e: m.entrySet()) {
            assertThat(e.getKey(), is(fromString(""+i)));
            assertThat(e.getValue(), is((JsonElement)primitive(i)));
            i++;
        }
        assertThat(i, is(m.size()));
    }

    public void shouldRemove() {
        SimpleMap<EfficientString,JsonElement> m = new SimpleMap<>();
        for(int i=0;i<42;i++) {
            m.put(fromString(""+i), primitive(i));
        }
        Iterator<Entry<EfficientString, JsonElement>> iterator = m.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<EfficientString, JsonElement> entry = iterator.next();
            iterator.remove();
            assertThat(m.get(entry.getKey()), nullValue());
        }
    }

    @Test(expectedExceptions=ConcurrentModificationException.class)
    public void shouldThrowConcurrentModificationException() {
        SimpleMap<EfficientString,JsonElement> m = new SimpleMap<>();
        for(int i=0;i<42;i++) {
            m.put(fromString(""+i), primitive(i));
        }
        for(@SuppressWarnings("unused") Entry<EfficientString, JsonElement> e: m.entrySet()) {
            m.put(fromString("oops"), primitive(42));
        }
    }
}
