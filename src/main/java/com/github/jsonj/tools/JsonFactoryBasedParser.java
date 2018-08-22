package com.github.jsonj.tools;

import com.fasterxml.jackson.core.JsonFactory;
import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonObject;
import com.github.jsonj.exceptions.JsonParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

/**
 * Extend this to quickly parse something using the jsonj handler for the jackson streaming parser with any jackson dataformat implementation.
 *
 * HoconParser for example implementation.
 */
public interface JsonFactoryBasedParser {

    JsonFactory factory();

    default JsonElement parse(InputStream is) {
        return parse(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    default JsonElement parse(Reader r) {
        try {
            com.fasterxml.jackson.core.JsonParser parser = factory().createParser(r);
            return JacksonHandler.parseContent(parser, JsonParser.DEFAULT_SETTINGS);
        } catch (com.fasterxml.jackson.core.JsonParseException e) {
            throw new JsonParseException(e);
        } catch (IOException e) {
            throw new JsonParseException(e);
        }
    }

    default JsonObject parseObject(Reader r) {
        return parse(r).asObject();
    }

    default JsonElement parse(String s) {
        return parse(new StringReader(s));
    }
    default JsonObject parseObject(String s) {
        return parse(s).asObject();
    }

}
