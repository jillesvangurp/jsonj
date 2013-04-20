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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.LinkedList;

import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.github.jsonj.JsonElement;
import com.github.jsonj.exceptions.JsonParseException;

/**
 * Parser based on json-simple. This class is thread safe so you can safely
 * inject your JsonParser object everywhere.
 *
 * Experimental alternative for JsonParser based on jackson's Stream parser.
 */
public class JsonParserNg {

    JsonFactory jsonFactory = new JsonFactory();

    public JsonParserNg() {
    }

	/**
	 * @param s
	 *            input string with some json
	 * @return JsonElement
	 * @throws JsonParseException
	 *             if the json cannot be parsed
	 */
	public JsonElement parse(final String s) {
	    try {
	        JsonParser parser = jsonFactory.createJsonParser(s);
	        return parseContent(parser);
        } catch (com.fasterxml.jackson.core.JsonParseException e) {
            throw new JsonParseException(e);
        } catch (IOException e) {
            throw new JsonParseException(e);
        } catch (ParseException e) {
            throw new JsonParseException(e);
        }
	}

    public JsonElement parseResource(String resource) throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(resource);
        if(is==null) {
            is = new FileInputStream(resource);
        }
        return parse(is);
	}

    private JsonElement parse(InputStream resourceAsStream) throws IOException {
        return parse(new BufferedReader(new InputStreamReader(resourceAsStream, Charset.forName("UTF-8"))));
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
	    JsonParser parser = jsonFactory.createParser(r);
	    try {
            return parseContent(parser);
        } catch (ParseException e) {
            throw new JsonParseException(e);
        }
	}

    private JsonElement parseContent(JsonParser parser) throws ParseException, IOException, com.fasterxml.jackson.core.JsonParseException {
        JsonHandler handler = new JsonHandler();

        LinkedList<Boolean> stack = new LinkedList<>();
        JsonToken nextToken;
        handler.startJSON();
        while((nextToken = parser.nextToken()) != null) {
            switch (nextToken) {
            case START_OBJECT:
                handler.startObject();
                break;
            case END_OBJECT:
                handler.endObject();
                endObjEntryIfNeeded(handler, stack);
                break;
            case START_ARRAY:
                handler.startArray();
                stack.push(false);
                break;
            case END_ARRAY:
                handler.endArray();
                stack.pop();
                endObjEntryIfNeeded(handler, stack);
                break;
            case FIELD_NAME:
                handler.startObjectEntry(parser.getText());
                stack.push(true);
                break;
            case VALUE_NUMBER_INT:
                handler.primitive(parser.getLongValue());
                endObjEntryIfNeeded(handler, stack);
                break;
            case VALUE_NUMBER_FLOAT:
                handler.primitive(parser.getDoubleValue());
                endObjEntryIfNeeded(handler, stack);
                break;
            case VALUE_STRING:
                handler.primitive(parser.getText());
                endObjEntryIfNeeded(handler, stack);
                break;
            case VALUE_NULL:
                handler.primitive(null);
                endObjEntryIfNeeded(handler, stack);
                break;
            case VALUE_TRUE:
                handler.primitive(true);
                endObjEntryIfNeeded(handler, stack);
                break;
            case VALUE_FALSE:
                handler.primitive(false);
                endObjEntryIfNeeded(handler, stack);
                break;
            default:
                throw new IllegalStateException("unexpected token " + nextToken);
            }
        }
        handler.endJSON();
        return handler.get();
    }

    private void endObjEntryIfNeeded(JsonHandler handler, LinkedList<Boolean> stack) throws ParseException, IOException {
        if(stack.size()>0 && stack.peek()) {
            handler.endObjectEntry();
            stack.pop();
        }
    }
}
