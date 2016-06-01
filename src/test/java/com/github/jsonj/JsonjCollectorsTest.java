package com.github.jsonj;

import static com.github.jsonj.tools.JsonBuilder.array;
import static com.github.jsonj.tools.JsonBuilder.field;
import static com.github.jsonj.tools.JsonBuilder.object;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import org.testng.annotations.Test;

@Test
public class JsonjCollectorsTest {
    public void shouldStreamAndCollectInArray() {
        JsonArray array = array(1,2,3,4,5,6,7,8,9);
        JsonArray array2 = array.stream().collect(JsonjCollectors.array());
        assertThat(array2, is(array2));
    }

    public void shouldStreamAndCollectObjectsInArray() {
        JsonArray array = array(object(field("1",1)), object(field("2",2)));
        JsonArray array2 = array.stream().collect(JsonjCollectors.array());
        assertThat(array, is(array2));
    }

    public void shouldStreamAndCollectObjectsIntoArray() {
        Object[] os = new Object[] {"", 1, null};
        Spliterator<Object> spliterator = Spliterators.spliterator(os, Spliterator.ORDERED);
        JsonArray array = StreamSupport.stream(spliterator, false).collect(JsonjCollectors.array());
        assertThat(array.size(), is(3));
    }

    public void shouldStreamAndCollectInSet() {
        JsonArray array = array(1,2,1,2);
        JsonSet set = array.stream().collect(JsonjCollectors.set());
        assertThat(array, not(is(set)));
        assertThat(set.size(), is(2));
    }

    public void shouldStreamAndCollectObjectsInSet() {
        JsonArray array = array(object(field("1",1)), object(field("1",1)));
        JsonSet set = array.stream().collect(JsonjCollectors.set());
        assertThat(array, not(is(set)));
        assertThat(set.size(), is(1));
    }

    public void shouldStreamAndCollectObjectsIntoSet() {
        Object[] os = new Object[] {"", 1, null,null,1,""};
        Spliterator<Object> spliterator = Spliterators.spliterator(os, Spliterator.ORDERED);
        JsonSet set = StreamSupport.stream(spliterator, false).collect(JsonjCollectors.set());
        assertThat(set.size(), is(3));
    }
}
