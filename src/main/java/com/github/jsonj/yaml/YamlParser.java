package com.github.jsonj.yaml;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.github.jsonj.JsonElement;
import com.github.jsonj.exceptions.JsonParseException;
import com.github.jsonj.tools.JacksonHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

public class YamlParser {
    private final YAMLFactory factory = new YAMLFactory();

    public YamlParser() {
    }

    public JsonElement parse(InputStream is) {
        return parse(new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8"))));
    }

    public JsonElement parse(Reader r) {
        try {
            YAMLParser parser = factory.createParser(r);
            return JacksonHandler.parseContent(parser);
        } catch (com.fasterxml.jackson.core.JsonParseException e) {
            throw new JsonParseException(e);
        } catch (IOException e) {
            throw new JsonParseException(e);
        }
    }

    public JsonElement parse(String s) {
        try {
            YAMLParser parser = factory.createParser(s);
            return JacksonHandler.parseContent(parser);
        } catch (com.fasterxml.jackson.core.JsonParseException e) {
            throw new JsonParseException(e);
        } catch (IOException e) {
            throw new JsonParseException(e);
        }
    }
}
