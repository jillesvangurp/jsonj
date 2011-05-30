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
package org.jsonj.tools;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map.Entry;

import org.jsonj.JsonElement;
import org.jsonj.JsonType;

/**
 * Utility class to serialize Json.
 */
public class JsonSerializer {

	/**
	 * @param json
	 * @return string representation of the json
	 */
	public static String serialize(final JsonElement json) {
		return serialize(json, false);
	}

	/**
	 * @param json
	 * @param pretty if true, a properly indented version of the json is returned
	 * @return string representation of the json
	 */
	public static String serialize(final JsonElement json, final boolean pretty) {
		StringWriter sw = new StringWriter();
		BufferedWriter bw = new BufferedWriter(sw);
		try {
			write(bw,json,pretty, 0);
		} catch (IOException e) {
			throw new IllegalStateException("cannot serialize json to a string", e);
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				throw new IllegalStateException("cannot serialize json to a string", e);
			}
		}
		return sw.getBuffer().toString();
	}

	/**
	 * Writes the object out as json.
	 * @param out output channel
	 * @param json
	 * @param pretty if true, a properly indented version of the json is written
	 * @throws IOException
	 */
	public static void write(final Writer out, final JsonElement json, final boolean pretty) throws IOException {
		write(new BufferedWriter(out), json, pretty, 0);
	}

	private static void write(final BufferedWriter bw, final JsonElement json, final boolean pretty, final int indent) throws IOException {
		JsonType type = json.type();
		switch (type) {
		case object:
			bw.write('{');
			newline(bw, indent+1, pretty);
			Iterator<Entry<String, JsonElement>> iterator = json.asObject().entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, JsonElement> entry = iterator.next();
				String key = entry.getKey();
				JsonElement value = entry.getValue();
				bw.write('"');
				bw.write(key);
				bw.write("\":");
				write(bw,value,pretty,indent+1);
				if(iterator.hasNext()) {
					bw.write(',');
					newline(bw, indent+1, pretty);
				}
			}
			newline(bw, indent, pretty);
			bw.write('}');
			break;
		case array:
			bw.write('[');
			newline(bw, indent+1, pretty);
			Iterator<JsonElement> arrayIterator = json.asArray().iterator();
			while (arrayIterator.hasNext()) {
				JsonElement value = arrayIterator.next();
				write(bw,value,pretty,indent+1);
				if(arrayIterator.hasNext()) {
					bw.write(',');
					newline(bw, indent+1, pretty);
				}
			}
			newline(bw, indent, pretty);
			bw.write(']');
			break;
		case string:
			bw.write('"');
			bw.write(json.toString());
			bw.write('"');
			break;
		case bool:
			bw.write(json.toString());
			break;
		case number:
			bw.write(json.toString());
			break;
		case nullValue:
			bw.write("null");
			break;

		default:
			throw new IllegalArgumentException("unhandled type " + type);
		}
	}

	private static void newline(final BufferedWriter bw, final int n, final boolean pretty) throws IOException {
		if(pretty) {
			bw.write('\n');
			for(int i=0;i<n;i++) {
				bw.write('\t');
			}
		}
	}
}
