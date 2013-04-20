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

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.github.jsonj.JsonElement;
import com.github.jsonj.exceptions.JsonParseException;

/**
 * Parser based on json-simple. This class is thread safe so you can safely
 * inject your JsonParser object everywhere.
 */
public class JsonParser {
	private final ThreadLocal<JSONParser> parserTL = new ThreadLocal<JSONParser>() {
		// the parser is not thread safe
		@Override
		protected JSONParser initialValue() {
			return new JSONParser();
		};
	};

	private final ThreadLocal<JsonHandler> handlerTL = new ThreadLocal<JsonHandler>() {
		// the handler is stateful and should not be used from more than one
		// thread
		@Override
		public JsonHandler initialValue() {
			return new JsonHandler();
		}
	};

	/**
	 * @param s
	 *            input string with some json
	 * @return JsonElement
	 * @throws JsonParseException
	 *             if the json cannot be parsed
	 */
	public JsonElement parse(final String s) {
		JsonHandler handler = handlerTL.get();
		try {
			parserTL.get().parse(s, handler);
		} catch (ParseException e) {
			throw new JsonParseException(e);
		}
		return handler.get();
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
		JsonHandler handler = handlerTL.get();
		try {
			parserTL.get().parse(r, handler);
		} catch (ParseException e) {
			throw new JsonParseException(e);
		}
		return handler.get();
	}
}
