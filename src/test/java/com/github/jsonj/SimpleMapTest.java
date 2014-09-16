package com.github.jsonj;

import static com.github.jsonj.tools.JsonBuilder.primitive;
import static com.jillesvangurp.efficientstring.EfficientString.fromString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map.Entry;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.jillesvangurp.efficientstring.EfficientString;

@Test
public class SimpleMapTest {
    private SimpleMap<EfficientString,JsonElement> m;

    @BeforeMethod
    public void before() {
        m = new SimpleMap<>();
    }

    public void shouldPutGetRemove() {
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
        for(int i=0;i<42;i++) {
            m.put(fromString(""+i), primitive(i));
        }
        for(@SuppressWarnings("unused") Entry<EfficientString, JsonElement> e: m.entrySet()) {
            m.put(fromString("oops"), primitive(42));
        }
    }

    //    public void shouldCalculateSize() throws IOException {
    //        SimpleMap<Integer , JsonElement> m1 = new SimpleMap<>();
    //        LinkedHashMap<Integer , JsonElement> m2 = new LinkedHashMap<>();
    //        for(int i=0;i<42;i++) {
    //            m1.put(i, primitive(i));
    //            m2.put(i, primitive(i));
    //        }
    //
    //        System.out.println(getSerializedSize(m));
    //        System.out.println(getSerializedSize(m2));
    //    }

    //    private int getSerializedSize(Object o) throws IOException {
    //        ByteArrayOutputStream baos = new ByteArrayOutputStream();
    //        ObjectOutputStream oos = new ObjectOutputStream(baos);
    //        oos.writeObject(o);
    //        oos.close();
    //        int size = baos.size();
    //        return size;
    //    }

}
