/**
 * Copyright (c) 2011, Jilles van Gurp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.jsonj.tools;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.github.jsonj.JsonArray;
import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonObject;
import com.github.jsonj.exceptions.JsonParseException;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Parser based on json-simple. This class is thread safe so you can safely
 * inject your JsonParser object everywhere.
 *
 * Experimental alternative for JsonParser based on jackson's Stream parser.
 */
public class JsonParser {
    public static final JsonjSettings DEFAULT_SETTINGS = new JsonjSettings() {
    };

    public static final JsonjSettings EFFICIENT_STRING_MAP_SETTINGS = new JsonjSettings() {
        @Override
        public boolean useEfficientStringBasedJsonObject() {
            return true;
        }
    };

    private final JsonFactory jsonFactory;
    private final JsonjSettings settings;

    public JsonParser() {
        jsonFactory = new JsonFactory();
        settings=DEFAULT_SETTINGS;
    }

    public JsonParser(JsonjSettings settings) {
        this.settings=settings;
        jsonFactory = new JsonFactory();
    }

    /**
     * @param settings settings object
     * @param features
     *            varargs of jackson features to enable, e.g.
     *            com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS
     */
    public JsonParser(JsonjSettings settings, Feature... features) {
        this.settings=settings;
        jsonFactory = new JsonFactory();
        for(Feature f : features) {
            jsonFactory.enable(f);
        }
    }

    /**
     * @param s
     *            input string with some json
     * @return JsonElement
     * @throws JsonParseException
     *             if the json cannot be parsed
     */
    public @Nonnull JsonElement parse(final String s) {
        try {
            com.fasterxml.jackson.core.JsonParser parser = jsonFactory.createParser(s);
            try {
                return JacksonHandler.parseContent(parser, settings);
            } finally {
                parser.close();
            }
        } catch (com.fasterxml.jackson.core.JsonParseException e) {
            throw new JsonParseException(e);
        } catch (IOException e) {
            throw new JsonParseException(e);
        }
    }

    public JsonElement parseResource(String resource) throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(resource);
        if(is == null) {
            is = new FileInputStream(resource);
        }
        return parse(is);
    }

    private JsonElement parse(InputStream is) throws IOException {
        return parse(new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8"))));
    }

    /**
     * @param r
     *            reader with some json input
     * @return JsonElement
     * @throws IOException
     *             if there is some problem reading the input
     * @throws JsonParseException
     *             if the json cannot be parsed
     */
    public JsonElement parse(final Reader r) throws IOException {
        com.fasterxml.jackson.core.JsonParser parser = jsonFactory.createParser(r);
        try {
            return JacksonHandler.parseContent(parser, settings);
        } catch (com.fasterxml.jackson.core.JsonParseException e) {
            throw new JsonParseException(e);
        } finally {
            parser.close();
        }
    }

    public JsonObject parseObject(String json) {
        return parse(json).asObject();
    }

    public JsonObject parseObject(InputStream is) throws IOException {
        return parse(is).asObject();
    }

    public JsonObject parseObject(Reader r) throws IOException {
        return parse(r).asObject();
    }

    public JsonArray parseArray(String json) {
        return parse(json).asArray();
    }

    public JsonArray parseArray(InputStream json) throws IOException {
        return parse(json).asArray();
    }

    public JsonArray parseArray(Reader r) throws IOException {
        return parse(r).asArray();
    }

    private Iterator<JsonObject> jsonLinesIterator(Reader r) {
        BufferedReader br = new BufferedReader(r);
        return new Iterator<JsonObject>() {
            JsonObject next = null;

            @Override
            public boolean hasNext() {
                if(next != null) {
                    return true;
                } else {
                    try {
                        String line = br.readLine();
                        if(line == null) {
                            return false;
                        } else {
                            if(StringUtils.isNotBlank(line) && !line.startsWith("#")) { // lets support comments while we're at it
                                next = parseObject(line);
                            } else {
                                // skip empty lines or comment lines
                                return hasNext();
                            }
                        }
                    } catch (IOException e) {
                        throw new IllegalStateException("error reading", e);
                    }
                    return next != null;
                }
            }

            @Override
            public JsonObject next() {
                if(hasNext()) {
                    JsonObject current = next;
                    next = null;
                    return current;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
    }

    /**
     * Use this to parse jsonlines.org style input. IMPORTANT, you have to close the reader yourself with a try ... finally.
     * @param r a reader
     * @return a Stream of the parsed jsonObjects.
     */
    public Stream<JsonObject> parseJsonLines(Reader r) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(jsonLinesIterator(r), Spliterator.ORDERED), false);
    }
}
