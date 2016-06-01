package com.github.jsonj;

import static com.github.jsonj.tools.JsonBuilder.primitive;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import org.testng.annotations.Test;

@Test
public class SimpleIntKeyMapTest {
    public void shouldPutGetStuff() {
        SimpleIntKeyMap<String> map = new SimpleIntKeyMap<>();
        for(int i=100; i< 200; i++) {
            map.put(i, "str"+i);
        }
        for(int i=100; i< 200; i++) {
            assertThat(map.get(i)).isEqualTo("str"+i);
        }
    }

    public void shouldRemoveStuff() {
        SimpleIntKeyMap<Integer> map = new SimpleIntKeyMap<>();
        for(int i=0; i< 200; i++) {
            map.put(i, i);
        }

        for(int i=0; i< 200; i=i+2) {
            map.remove(i);
        }
        assertThat(map.size()).isEqualTo(100);
        for(Entry<Integer, Integer> e: map.entrySet()) {
            assertThat(e.getValue() % 2).as("all even versions were removed").isEqualTo(1);
        }
    }

    public void shouldIterate() {
        SimpleIntKeyMap<JsonElement> m = new SimpleIntKeyMap<>();
        for(int i=0;i<42;i++) {
            m.put(i, primitive(i));
        }
        int i=0;
        for(Entry<Integer, JsonElement> e: m.entrySet()) {
            assertThat(e.getKey(), is(i));
            assertThat(e.getValue(), is((JsonElement)primitive(i)));
            i++;
        }
        assertThat(i, is(m.size()));
    }

    public void shouldRemove() {
        SimpleIntKeyMap<JsonElement> m = new SimpleIntKeyMap<>();
        for(int i=0;i<42;i++) {
            m.put(i, primitive(i));
        }
        Iterator<Entry<Integer, JsonElement>> iterator = m.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Integer, JsonElement> entry = iterator.next();
            Integer key = entry.getKey();
            iterator.remove();
            assertThat(m.get(key), nullValue());
        }
    }

    @Test(expectedExceptions=ConcurrentModificationException.class)
    public void shouldNotAllowConcurrentModification() {
        SimpleIntKeyMap<JsonElement> m = new SimpleIntKeyMap<>();
        for(int i=0;i<42;i++) {
            m.put(i, primitive(i));
        }
        for(@SuppressWarnings("unused") Entry<Integer, JsonElement> e: m.entrySet()) {
            m.put(1234, primitive("y"));
        }
    }

    public void shouldRemoveFromEntrySet() {
        SimpleIntKeyMap<JsonElement> m = new SimpleIntKeyMap<>();
        m.put(42, primitive(42));
        m.put(43, primitive(43));
        Set<Entry<Integer, JsonElement>> set = m.entrySet();
        Iterator<Entry<Integer, JsonElement>> iterator = set.iterator();
        iterator.next();
        iterator.remove();
        assertThat(m.get(42)).isNull();
    }

    public void shouldRemoveElements() {
        SimpleIntKeyMap<JsonElement> m = new SimpleIntKeyMap<>();
        m.put(1, primitive(1));
        m.put(2, primitive(2));
        m.put(3, primitive(3));
        m.put(4, primitive(4));
        m.put(5, primitive(5));
        m.remove(2);
        m.remove(4);
        assertThat(m.getWithIntKey(1)).isEqualTo(primitive(1));
        assertThat(m.getWithIntKey(2)).isNull();
        assertThat(m.getWithIntKey(3)).isEqualTo(primitive(3));
        assertThat(m.getWithIntKey(4)).isNull();
        assertThat(m.getWithIntKey(5)).isEqualTo(primitive(5));
    }
}
