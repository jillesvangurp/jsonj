package com.github.jsonj.toml;

import com.github.jsonj.JsonArray;
import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonObject;
import com.moandjiezana.toml.TomlWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TomlSerializer {
    private TomlWriter writer;

    public TomlSerializer(TomlWriter writer) {
        this.writer = writer;
    }

    private Object translate(JsonElement e) {
        if(e.isObject()) {
            JsonObject o = e.asObject();
            Map<String,Object> map = new LinkedHashMap<>();
            o.forEach((key,value) -> {
                map.put(key,translate(value));
            });
            return map;
        } else if(e.isArray()) {
            JsonArray a = e.asArray();
            List<Object> list = new ArrayList<>();
            a.forEach(element -> {
                list.add(translate(element));
            });
            return list;
        } else {
            return e.asPrimitive().value();
        }
    }

    public String write(JsonObject o) {
        return writer.write(translate(o));
    }

    public void write(JsonObject object, Writer writer) {
        try {
            this.writer.write(translate(object), writer);
        } catch (IOException e) {
            throw new IllegalStateException("writing failed", e);
        }
    }

    public void write(JsonObject o, OutputStream out) {
        try {
            writer.write(translate(o), out);
        } catch (IOException e) {
            throw new IllegalStateException("writing failed", e);
        }
    }
}
