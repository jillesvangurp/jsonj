package com.github.jsonj;

import static com.github.jsonj.tools.JsonBuilder.array;
import static com.github.jsonj.tools.JsonBuilder.field;
import static com.github.jsonj.tools.JsonBuilder.object;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jsonj.tools.JsonParser;
import org.testng.annotations.BeforeMethod;
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

    public void shouldSerializeUsingJacksonObjectMapper() throws JsonProcessingException {
        JsonObject o = object(
            field("meaning_of_life",42),
            field("a",42.0),
            field("b",true),
            field("c",array(42,"foo",3.14,true,null)),
            field("d",object(field("a",1)))
        );
        String serialized = objectMapper.writeValueAsString(o);
        assertThat(parser.parse(serialized)).isEqualTo(o);
    }
}
