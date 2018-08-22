package com.github.jsonj;

import com.github.jsonj.toml.TomlParser;
import com.github.jsonj.toml.TomlSerializer;
import com.moandjiezana.toml.TomlWriter;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.time.ZoneId;
import java.util.TimeZone;

import static com.github.jsonj.tools.JsonBuilder.array;
import static com.github.jsonj.tools.JsonBuilder.field;
import static com.github.jsonj.tools.JsonBuilder.object;

@Test
public class TomlTest {

    public void shouldSerializeToml() {
        JsonObject object = object(
                field("foo", "bar"),
                field("list",array("one","two")),
                field("obj", object(field("name","nested"))),
                field("listOfObjects", array(object(field("id",1)),object(field("id",2))))
        );

        TomlWriter writer = new TomlWriter.Builder()
                .indentTablesBy(2)
                .indentValuesBy(2)
                .padArrayDelimitersBy(1)
                .showFractionalSeconds()
                .timeZone(TimeZone.getTimeZone(ZoneId.of("UTC")))
                .build();


        TomlSerializer jsonJToToml = new TomlSerializer(new TomlWriter());

        String output = jsonJToToml.write(object);
//        System.err.println(output);

        TomlParser tp = new TomlParser();
        JsonObject parsed = tp.parse(output);
        Assertions.assertThat(parsed).isEqualTo(object);
//        System.err.println(parsed);

    }
}
