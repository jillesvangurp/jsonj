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

import java.io.IOException;
import java.io.Reader;

import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.github.jsonj.JsonArray;
import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonObject;
import com.github.jsonj.JsonPrimitive;
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

	private final ThreadLocal<JsonHandler> handlerTL = new ThreadLocal<JsonParser.JsonHandler>() {
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

	private static final class JsonHandler implements ContentHandler {
		// use a simple stack mechanism to reconstruct the tree
		JsonArray stack = new JsonArray();
		boolean isObject = false;

		public JsonElement get() {
			// the remaining element on the stack is the fully parsed
			// JsonElement
			return stack.getLast();
		}

		@Override
		public boolean startObjectEntry(final String entry)
		throws ParseException, IOException {
			stack.add(JsonBuilder.primitive(entry));
			return true;
		}

		@Override
		public boolean startObject() throws ParseException, IOException {
			isObject = true;
			stack.add(new JsonObject());
			return true;
		}

		@Override
		public void startJSON() throws ParseException, IOException {
			// clean up from previous runs
			stack.clear();
		}

		@Override
		public boolean startArray() throws ParseException, IOException {
			isObject = false;
			stack.add(new JsonArray());
			return true;
		}

		@Override
		public boolean primitive(final Object object) throws ParseException,
		IOException {
			JsonPrimitive primitive = new JsonPrimitive(object);
			if (isObject) {
				stack.add(primitive);
			} else {
				JsonElement peekLast = stack.peekLast();
				if (peekLast instanceof JsonArray) {
					peekLast.asArray().add(primitive);
				} else {
					stack.add(primitive);
				}
			}
			return true;
		}

		@Override
		public boolean endObjectEntry() throws ParseException, IOException {
			JsonElement value = stack.pollLast();
			JsonElement e = stack.peekLast();
			if(e.isPrimitive()) {
				e=stack.pollLast();
				JsonElement last = stack.peekLast();
				if(last.isObject()) {
					JsonObject container = last.asObject();
					container.put(e.toString(), value);
				} else if(last.isArray()) {
					throw new IllegalStateException("shouldn't happen");

				}
			}
			return true;
		}

		@Override
		public boolean endObject() throws ParseException, IOException {
			if(stack.size()>1 && stack.get(stack.size()-2).isArray()) {
				JsonElement object = stack.pollLast();
				stack.peekLast().asArray().add(object);
			}
			return true;
		}

		@Override
		public void endJSON() throws ParseException, IOException {
		}

		@Override
		public boolean endArray() throws ParseException, IOException {
			if(stack.size()>1 && stack.get(stack.size()-2).isArray()) {
				JsonElement value = stack.pollLast();
				stack.peekLast().asArray().add(value);
			}
			return true;
		}
	}
}
