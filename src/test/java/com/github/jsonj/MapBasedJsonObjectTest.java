package com.github.jsonj;

import static com.github.jsonj.tools.JsonBuilder.field;
import static com.github.jsonj.tools.JsonBuilder.object;
import static org.assertj.core.api.StrictAssertions.assertThat;

import org.testng.annotations.Test;

@Test
public class MapBasedJsonObjectTest {

    public void shouldBeImmutable() {
        JsonObject o = object(field("x", 42));
        MapBasedJsonObject mo = o.toMapBasedJsonObject();
        assertThat(o.isMutable()).isTrue();
        assertThat(mo.isMutable()).isTrue();
        JsonObject imo = mo.immutableClone();
        assertThat(imo.isMutable()).isFalse();
    }

    public void shouldScaleNumberOfKeys() {
        // this test proves two points:
        // 1) inserting 1000000 elements is fast o(1)
        // 2) establishing a non existent key is not there is fast-ish
        // if it is not, both will take ages
        JsonObject jsonObject = new MapBasedJsonObject();
        for(int i=0;i<1000000;i++) {
            jsonObject.put("key_"+i, i);
        }
        JsonElement jsonElement = jsonObject.get("key_IDONTEXIST");
        assertThat(jsonElement).isNull();
    }
}
