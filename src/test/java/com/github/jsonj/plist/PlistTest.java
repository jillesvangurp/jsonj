package com.github.jsonj.plist;

import static com.github.jsonj.tools.JsonBuilder.array;
import static com.github.jsonj.tools.JsonBuilder.field;
import static com.github.jsonj.tools.JsonBuilder.object;
import static com.github.jsonj.tools.JsonBuilder.primitive;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.testng.annotations.Test;

import com.github.jsonj.JsonArray;
import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonObject;

@Test
public class PlistTest {
    public void shouldSerializeAndParseObject() throws IOException {
        JsonObject o = object(
                field("x",42),
                field("o",object(
                        field("x",42),
                        field("a",array(1,2,3))
                )),
                field("a", array(array(0.1,0.2)))
        );

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JsonJPlistSerializer.serialize(o, bos);
        bos.flush();
        byte[] bytes = bos.toByteArray();
        JsonElement e = JsonJPlistParser.parse(bytes);
        assertThat(e.isObject()).isEqualTo(true);
    }

    public void shouldSerializeAndParseArray() throws IOException {
        JsonArray a = array(primitive(42), object(field("x",array(1,2))));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JsonJPlistSerializer.serialize(a, bos);
        bos.flush();
        byte[] bytes = bos.toByteArray();
        JsonElement e = JsonJPlistParser.parse(bytes);
        assertThat(e.isArray()).isEqualTo(true);
    }
}