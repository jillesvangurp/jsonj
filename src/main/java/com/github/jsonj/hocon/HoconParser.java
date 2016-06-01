package com.github.jsonj.hocon;

import com.fasterxml.jackson.core.JsonParser;
import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonObject;
import com.github.jsonj.exceptions.JsonParseException;
import com.github.jsonj.tools.JacksonHandler;
import com.jasonclawson.jackson.dataformat.hocon.HoconFactory;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class HoconParser {
    private final HoconFactory factory = new HoconFactory();

    public HoconParser() {
    }

    // public JsonElement parse(InputStream is) {
    // try {
    // HoconTreeTraversingParser parser = factory.createParser(is);
    // return JacksonHandler.parseContent(parser);
    // } catch (com.fasterxml.jackson.core.JsonParseException e) {
    // throw new JsonParseException(e);
    // } catch (IOException e) {
    // throw new JsonParseException(e);
    // }
    // }

    public JsonElement parse(Reader r) {
        try {
            JsonParser parser = factory.createParser(r);
            return JacksonHandler.parseContent(parser);
        } catch (com.fasterxml.jackson.core.JsonParseException e) {
            throw new JsonParseException(e);
        } catch (IOException e) {
            throw new JsonParseException(e);
        }
    }

    public JsonObject parseObject(Reader r) {
        return parse(r).asObject();
    }

    public JsonElement parse(String s) {
        return parse(new StringReader(s));
    }
    public JsonObject parseObject(String s) {
        return parse(s).asObject();
    }

}
