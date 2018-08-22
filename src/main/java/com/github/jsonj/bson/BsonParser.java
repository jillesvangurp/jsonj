package com.github.jsonj.bson;

import com.github.jsonj.JsonElement;
import com.github.jsonj.exceptions.JsonParseException;
import com.github.jsonj.tools.JacksonHandler;
import com.github.jsonj.tools.JsonParser;
import de.undercouch.bson4jackson.BsonFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Parse mongodb style bson.Note, bson is typically slightly bigger than normal json but parser performance tends to be
 * better.
 * So make sure you use it for the right reasons.
 */
public class BsonParser {
    BsonFactory factory = new BsonFactory();

    public BsonParser() {
    }

    public JsonElement parse(InputStream is) {
        try {
            de.undercouch.bson4jackson.BsonParser parser = factory.createParser(is);
            return JacksonHandler.parseContent(parser, JsonParser.DEFAULT_SETTINGS);
        } catch (com.fasterxml.jackson.core.JsonParseException e) {
            throw new JsonParseException(e);
        } catch (IOException e) {
            throw new JsonParseException(e);
        }
    }

    public JsonElement parse(String s) {
        ByteArrayInputStream is = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
        return parse(is);
    }
}
