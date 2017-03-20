package com.github.jsonj;

import static com.github.jsonj.tools.JsonBuilder.field;
import static com.github.jsonj.tools.JsonBuilder.object;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.jsonj.tools.JsonParser;
import org.testng.annotations.Test;

@Test
public class MapBasedJsonObjectTest {
    private final JsonParser parser = new JsonParser();

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
        for(int i=0;i<100000;i++) {
            jsonObject.put("key_"+i, i);
        }
        JsonElement jsonElement = jsonObject.get("key_IDONTEXIST");
        assertThat(jsonElement).isNull();
        JsonElement parsed = parser.parse(jsonObject.toString());
        assertThat(parsed).isEqualTo(jsonObject);
    }

    public void shouldParseAsMapBasedJsonObject() {
        JsonObject jsonObject = new JsonObject();
        for(int i=0;i<300;i++) {
            jsonObject.put("key_"+i, i);
        }
        JsonElement parsed = parser.parse(jsonObject.toString());
        // because > 100 keys
        assertThat(parsed).isInstanceOf(MapBasedJsonObject.class);
    }

    public void shouldPreserveNestedStructureWhenUpgradingToMapBasedJsonObject() {
        JsonObject nested=new JsonObject();
        for(int i=0;i<500;i++) {
            nested.put("key_"+i, i);
        }
        JsonObject o = object(field("nested",nested));
        String input=o.toString();
        JsonObject parsed = parser.parseObject(input);
        assertThat(parsed).isEqualTo(o);
    }

    public void shouldNotRevertToJsonObjectWhenCloning() {
        JsonObject jsonObject = new MapBasedJsonObject();
        for(int i=0;i<1000;i++) {
            jsonObject.put("key_"+i, i);
        }
        assertThat(jsonObject.deepClone()).isInstanceOf(MapBasedJsonObject.class);
        assertThat(jsonObject.immutableClone()).isInstanceOf(MapBasedJsonObject.class);
    }
}
