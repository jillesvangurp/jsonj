package com.github.jsonj.yaml;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.github.jsonj.JsonElement;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

public class YamlSerializer {
    private final YAMLFactory factory = new YAMLFactory();

    public void serialize(OutputStream out, JsonElement e) {
        serialize(new OutputStreamWriter(out, Charset.forName("UTF8")), e);
    }

    public void serialize(Writer w, JsonElement e) {
        try {
            YAMLGenerator yaml = factory.createGenerator(w);
            serialize(yaml, e);
            yaml.flush();
        } catch (IOException e1) {
            throw new IllegalStateException(e1);
        }
    }

    private void serialize(YAMLGenerator yaml, JsonElement e) throws IOException {
        switch (e.type()) {
        case object:
            yaml.writeStartObject();
            e.asObject().forEach((k,v) -> {
               try {
                    yaml.writeFieldName(k);
                    serialize(yaml, v);
                } catch (IOException e1) {
                    throw new IllegalStateException(e1);
                }
            });

            yaml.writeEndObject();

            break;
        case array:
            yaml.writeStartArray();
            e.asArray().forEach(element -> {
                try {
                    serialize(yaml, element);
                } catch (IOException e1) {
                    throw new IllegalStateException(e1);
                }
            });
            yaml.writeEndArray();
            break;
        case string:
            yaml.writeString(e.asString());
            break;
        case bool:
            yaml.writeBoolean(e.asBoolean());
            break;
        case number:
            Number number = e.asNumber();
            Class<? extends Number> clazz = number.getClass();
            if(clazz.isAssignableFrom(Double.class) || clazz.isAssignableFrom(Float.class)) {
                yaml.writeNumber(number.doubleValue());
            } else {
                yaml.writeNumber(number.longValue());
            }
            break;
        case nullValue:
            yaml.writeNull();
            break;
        default:
            break;
        }
    }
}
