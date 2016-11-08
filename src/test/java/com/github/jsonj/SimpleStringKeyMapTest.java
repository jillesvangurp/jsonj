package com.github.jsonj;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

@Test
public class SimpleStringKeyMapTest {

    public void shouldStoreAndRetrieveStuff() {
        SimpleStringKeyMap<String> map = new SimpleStringKeyMap<>();
        map.put("1", "one");
        map.put("2", "two");
        map.put("3", "three");
        map.put("4", "four");
        map.remove("1");
        map.remove("3");
        assertThat(map.get("3")).isNull();
        assertThat(map.get("4")).isEqualTo("four");
        assertThat(map.entrySet().iterator().next().getKey()).isEqualTo("2");
    }
}
