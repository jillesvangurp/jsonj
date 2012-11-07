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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map.Entry;

import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonType;

/**
 * Utility class to serialize Json.
 */
public class JsonSerializer {

	private JsonSerializer() {
		// utility class, don't instantiate
	}

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
		try {
			write(sw,json,pretty);
		} catch (IOException e) {
			throw new IllegalStateException("cannot serialize json to a string", e);
		} finally {
			try {
				sw.close();
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
		BufferedWriter bw = new BufferedWriter(out);
		write(bw, json, pretty, 0);
		if(pretty) {
			out.write('\n');
		}
		bw.flush();
	}

	public static void write(final OutputStream out, final JsonElement json, final boolean pretty) throws IOException {
		write(new OutputStreamWriter(out, Charset.forName("UTF-8")), json, pretty);
	}

	private static void write(final BufferedWriter bw, final JsonElement json, final boolean pretty, final int indent) throws IOException {
		if(json==null) {
            return;
        }
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
				if(value != null) {
    				bw.write('"');
    				bw.write(jsonEscape(key));
    				bw.write("\":");
    				write(bw,value,pretty,indent+1);
    				if(iterator.hasNext()) {
    					bw.write(',');
    					newline(bw, indent+1, pretty);
    				}
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
			bw.write(json.toString());
			break;
		case bool:
            bw.write(json.toString());
			break;
		case number:
            bw.write(json.toString());
			break;
		case nullValue:
            bw.write(json.toString());
			break;

		default:
			throw new IllegalArgumentException("unhandled type " + type);
		}
	}

	public static String jsonEscape(String raw) {
	    StringBuilder buf=new StringBuilder(raw.length());
	    for(char c: raw.toCharArray()) {
	        if('\n' == c) {
	            buf.append("\\n");
	        } else if('"' == c) {
	            buf.append("\\\"");
	        } else if('\\' == c) {
                buf.append("\\\\");
            } else if('\t' == c) {
                buf.append("\\t");
            } else if('\r' == c) {
                buf.append("\\r");
            } else {
	            buf.append(c);
	        }
	    }
        return buf.toString();
    }
	
	public static String jsonUnescape(String escaped) {
        StringBuilder buf=new StringBuilder(escaped.length());
        char[] chars = escaped.toCharArray();
        if(chars.length >= 2) {
            int i=1;
            while(i<chars.length) {
                if(chars[i-1] == '\\') {
                    if(chars[i]=='t') {
                        buf.append('\t');
                        i+=2;
                    } else if(chars[i]=='n') {
                        buf.append('\n');
                        i+=2;
                    } else if(chars[i]=='r') {
                        buf.append('\r');
                        i+=2;
                    } else if(chars[i] == '"') {
                        buf.append('"');
                        i+=2;
                    } else if(chars[i] == '\\') {
                        buf.append('\\');
                        i+=2;
                    } else {
                        buf.append(chars[i-1]);
                        buf.append(chars[i]);
                        i+=2;
                    }
                } else {
                    buf.append(chars[i-1]);
                    i++;                    
                }
            } 
            if(i==chars.length) { 
                // make sure to add the last character
                buf.append(chars[i-1]);
            }
            return buf.toString();
        } else {
            return escaped;
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
