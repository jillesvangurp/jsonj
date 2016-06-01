package com.github.jsonj.hocon;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.jsonj.JsonObject;
import io.inbot.utils.IOUtils;
import java.io.BufferedReader;
import java.io.IOException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test // most examples here are grabbed from https://github.com/typesafehub/config
public class HoconTest {

    private HoconParser parser;

    @BeforeMethod
    public void before() {
        parser = new HoconParser();

    }

    public void shouldParseAndSubstitute() {
        String hocon="path : \"/bin\"\n" +
            "path : ${path}\":/usr/bin\"";
        JsonObject parsed = parser.parseObject(hocon);
        assertThat(parsed.getString("path")).isEqualTo("/bin:/usr/bin");
    }

    public void shouldConcatenate() {
        String hocon="path : [ \"/bin\" ]\n" +
            "path : ${path} [ \"/usr/bin\" ]";
        JsonObject parsed = parser.parseObject(hocon);
        assertThat(parsed.getArray("path").get(0).asString()).isEqualTo("/bin");
        assertThat(parsed.getArray("path").get(1).asString()).isEqualTo("/usr/bin");
    }

    public void shouldConcatenateShortHand() {
        String hocon="path : [ \"/bin\" ]\n" +
            "path += \"/usr/bin\"";
        JsonObject parsed = parser.parseObject(hocon);
        assertThat(parsed.getArray("path").get(0).asString()).isEqualTo("/bin");
        assertThat(parsed.getArray("path").get(1).asString()).isEqualTo("/usr/bin");
    }

    public void shouldHocon() throws IOException {
        BufferedReader resource = IOUtils.resource("hocon/sample.hjson");
        JsonObject parsed = parser.parseObject(resource);
        assertThat(parsed.getInt("foo","baz")).isEqualTo(12);
        assertThat(parsed.getInt("foo","bar")).isEqualTo(10);
    }

    public void shouldDoKVproperties() {
        String hocon="foo.bar=10\n" +
        "foo.baz=12";
        JsonObject parsed = parser.parseObject(hocon);
        assertThat(parsed.getInt("foo","baz")).isEqualTo(12);
        assertThat(parsed.getInt("foo","bar")).isEqualTo(10);
    }

    public void shouldDoKVpropertiesCommaSeparated() {
        String hocon="foo.bar=10, foo.baz=12";
        JsonObject parsed = parser.parseObject(hocon);
        assertThat(parsed.getInt("foo","baz")).isEqualTo(12);
        assertThat(parsed.getInt("foo","bar")).isEqualTo(10);
    }

    public void shouldDoKVpropertiesCommaFree() {
        String hocon="foo {\n" +
            "    bar = 10\n" +
            "    baz = 12\n" +
            "}";
        JsonObject parsed = parser.parseObject(hocon);
        assertThat(parsed.getInt("foo","baz")).isEqualTo(12);
        assertThat(parsed.getInt("foo","bar")).isEqualTo(10);
    }

    public void shouldHandleComments() {
        String hocon="// I'm a comment#&{}\npath : [ \"/bin\" ]\n" +
            "path += \"/usr/bin\"";
        JsonObject parsed = parser.parseObject(hocon);
        assertThat(parsed.size()).isEqualTo(1); // comment should be ignored

    }
}
