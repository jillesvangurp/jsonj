package com.github.jsonj.yaml;

import static com.github.jsonj.tools.JsonBuilder.array;
import static com.github.jsonj.tools.JsonBuilder.field;
import static com.github.jsonj.tools.JsonBuilder.object;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.testng.annotations.Test;

@Test
public class YamlParsingAndSerializingTest {
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
        YamlSerializer yamlSerializer = new YamlSerializer();
        yamlSerializer.serialize(bos, o);
        bos.flush();
        byte[] bytes = bos.toByteArray();
        YamlParser yamlParser = new YamlParser();
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);

        JsonElement e = yamlParser.parse(is);
        assertThat(e.isObject()).isEqualTo(true);
    }

}
