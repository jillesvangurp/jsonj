package com.github.jsonj;

import static com.github.jsonj.tools.JsonBuilder.array;
import static com.github.jsonj.tools.JsonBuilder.field;
import static com.github.jsonj.tools.JsonBuilder.fromObject;
import static com.github.jsonj.tools.JsonBuilder.object;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jsonj.tools.JsonParser;
import java.io.IOException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test
public class ObjectMapperTest {

    private ObjectMapper objectMapper;
    private JsonParser parser;

    @BeforeMethod
    public void before() {
        objectMapper = new ObjectMapper();
        parser = new JsonParser();
    }

    @DataProvider
    public Object[][] jsonElements() {
        return new Object[][] {
            {object(
                field("meaning_of_life",42),
                field("a",42.0),
                field("b",true),
                field("c",array(42,"foo",3.14,true,null)),
                field("d",object(field("a",1)))
            )},
            {
                array(42,"foo",3.14,true,null)
            },
            {
                fromObject(42)
            },
            {
                fromObject("hello world")
            }
        };
    }

    @Test(dataProvider="jsonElements")
    public void shouldSurviveSerializeDeserializeAndBeEqual(JsonElement element) throws IOException {
        String serialized = objectMapper.writeValueAsString(element);
        assertThat(parser.parse(serialized)).isEqualTo(element);
        JsonElement deSerialized = objectMapper.readValue(serialized, JsonElement.class);
        assertThat(deSerialized).isEqualTo(element);

        if(element.isArray()) {
            JsonArray e = objectMapper.readValue(serialized, JsonArray.class);
            assertThat(e).isEqualTo(element);
        }

        if(element.isObject()) {
            JsonObject e = objectMapper.readValue(serialized, JsonObject.class);
            assertThat(e).isEqualTo(element);
        }

        if(element.isPrimitive()) {
            JsonPrimitive e = objectMapper.readValue(serialized, JsonPrimitive.class);
            assertThat(e).isEqualTo(element);
        }

    }
}
