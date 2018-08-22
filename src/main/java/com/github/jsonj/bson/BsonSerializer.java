package com.github.jsonj.bson;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.github.jsonj.JsonArray;
import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonObject;
import com.github.jsonj.JsonPrimitive;
import de.undercouch.bson4jackson.BsonFactory;
import de.undercouch.bson4jackson.BsonGenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map.Entry;

/**
 * Serialize JsonElements to mongodb style bson. Note, bson is typically slightly bigger than normal json but parser
 * performance tends to be better.
 * So make sure you use it for the right reasons.
 */
public class BsonSerializer {
    public static void serialize(JsonElement element, OutputStream out) throws IOException {
        BsonFactory bsonFactory = new BsonFactory();

        BsonGenerator generator = bsonFactory.createGenerator(out);
        if(element.isObject()) {
            serialize(generator, element.asObject());
        } else if(element.isArray()) {
            serialize(generator, element.asArray());

        } else {
            serialize(generator, element.asPrimitive());
        }
        generator.close();
    }

    private static void serialize(BsonGenerator generator, JsonObject object) throws JsonGenerationException, IOException {
        generator.writeStartObject();
        for(Entry<String, JsonElement> entry: object.entrySet()) {
            generator.writeFieldName(entry.getKey());
            JsonElement element = entry.getValue();

            if(element.isObject()) {
                serialize(generator, element.asObject());
            } else if(element.isArray()) {
                serialize(generator, element.asArray());

            } else {
                serialize(generator, element.asPrimitive());
            }
        }
        generator.writeEndObject();
    }

    private static void serialize(BsonGenerator generator, JsonArray array) throws JsonGenerationException, IOException {
        generator.writeStartArray();
        for(JsonElement element: array) {
            if(element.isObject()) {
                serialize(generator, element.asObject());
            } else if(element.isArray()) {
                serialize(generator, element.asArray());
            } else {
                serialize(generator, element.asPrimitive());
            }
        }

        generator.writeEndArray();
    }

    private static void serialize(BsonGenerator generator, JsonPrimitive p) throws JsonGenerationException, IOException {
        if(p.isBoolean()) {
            generator.writeBoolean(p.asBoolean());
        } else if(p.isNumber()) {
            if(p.asString().contains(".")) {
                generator.writeNumber(p.asDouble());
            } else {
                generator.writeNumber(p.asLong());
            }
        } else if(p.isString()) {
            generator.writeString(p.asString());
        } else {
            generator.writeNull();
        }

    }
}
