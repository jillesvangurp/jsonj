package com.github.jsonj.toml;

import com.github.jsonj.JsonObject;
import com.github.jsonj.tools.JsonBuilder;
import com.moandjiezana.toml.Toml;

import java.io.InputStream;
import java.io.Reader;

public class TomlParser {
    public TomlParser() {
    }

    public JsonObject parse(Reader input) {
        return tomlToJsonObject(new Toml().read(input));
    }

    public JsonObject parse(InputStream input) {
        return tomlToJsonObject(new Toml().read(input));
    }

    public JsonObject parse(String input) {
        return tomlToJsonObject(new Toml().read(input));
    }

    private JsonObject tomlToJsonObject(Toml read) {
        return JsonBuilder.fromObject(read.toMap()).asObject();
    }
}
