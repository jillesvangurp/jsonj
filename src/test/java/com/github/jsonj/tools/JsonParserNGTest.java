package com.github.jsonj.tools;

import static com.github.jsonj.tools.JsonBuilder.array;
import static com.github.jsonj.tools.JsonBuilder.object;

import org.testng.annotations.Test;

@Test
public class JsonParserNGTest {
    JsonParserNg parser = new JsonParserNg();

    public void shouldParse() {
        parser.parse(object()
                .put("42", 42)
                .put("obj", object().put("hi", "wrld").get())
                .put("arr", array(1,2,3,4))
                .get().toString());
    }
}
