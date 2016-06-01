package com.github.jsonj.hocon;

import com.github.jsonj.JsonElement;
import io.inbot.utils.IOUtils;
import java.io.BufferedReader;
import java.io.IOException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class HoconTest {

    private HoconParser parser;

    @BeforeMethod
    public void before() {
        parser = new HoconParser();

    }

    public void shouldHocon() throws IOException {
        BufferedReader resource = IOUtils.resource("hocon/sample.hjson");
        JsonElement parsed = parser.parse(resource);
        System.out.println(parsed.prettyPrint());
    }
}
