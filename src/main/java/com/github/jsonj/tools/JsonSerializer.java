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

import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonType;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;

/**
 * Utility class to serialize Json.
 */
public class JsonSerializer {
    public static final Charset UTF8 = Charset.forName("utf-8");
    public static final String ESCAPED_CARRIAGE_RETURN = "\\r";
    public static final String ESCAPED_TAB = "\\t";
    public static final String ESCAPED_BACKSLASH = "\\\\";
    public static final String ESCAPED_NEWLINE = "\\n";
    public static final String ESCAPED_QUOTE = "\\\"";
    public static final String OPEN_BRACKET = "[";
    public static final String CLOSE_BRACKET = "]";
    public static final String OPEN_BRACE = "{";
    public static final String CLOSE_BRACE = "}";
    public static final String COLON = ":";
    public static final String QUOTE = "\"";
    public static final String COMMA = ",";

    private JsonSerializer() {
        // utility class, don't instantiate
    }

    /**
     * @param json
     *            a {@link JsonElement}
     * @return string representation of the json
     */
    public static String serialize(@Nonnull JsonElement json) {
        return serialize(json, false);
    }

    /**
     * @param json
     *            a {@link JsonElement}
     * @param out
     *            an {@link OutputStream}
     */
    public static void serialize(@Nonnull  JsonElement json, @Nonnull OutputStream out) {
        try {
            json.serialize(out);
        } catch (IOException e) {
            throw new IllegalStateException("cannot serialize json to output stream", e);
        }
    }

    public static void serialize(@Nonnull  JsonElement json, @Nonnull Writer out) {
        try {
            json.serialize(out);
        } catch (IOException e) {
            throw new IllegalStateException("cannot serialize json to output stream", e);
        }
    }

    /**
     * @param json
     *            a {@link JsonElement}
     * @param pretty
     *            if true, a properly indented version of the json is returned
     * @return string representation of the json
     */
    @SuppressWarnings("null")
    public static @Nonnull String serialize(@Nonnull JsonElement json, boolean pretty) {
        StringWriter sw = new StringWriter();
        if(pretty) {
            try {
                serialize(sw, json, pretty);
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
        } else {
            try {
                json.serialize(sw);
                return sw.getBuffer().toString();
            } catch (IOException e) {
                throw new IllegalStateException("cannot serialize json to a string", e);
            }
        }
    }

    /**
     * Writes the object out as json.
     *
     * @param out
     *            output writer
     * @param json
     *            a {@link JsonElement}
     * @param pretty
     *            if true, a properly indented version of the json is written
     * @throws IOException
     *             if there is a problem writing to the writer
     */
    public static void serialize(@Nonnull  Writer out, @Nonnull  JsonElement json, boolean pretty) throws IOException {
        BufferedWriter bw = new BufferedWriter(out);
        serialize(bw, json, pretty, 0);
        if(pretty) {
            bw.write('\n');
        }
        bw.flush();
    }

    /**
     * Writes the object out as json.
     *
     * @param out
     *            output writer
     * @param json
     *            a {@link JsonElement}
     * @param pretty
     *            if true, a properly indented version of the json is written
     * @throws IOException
     *             if there is a problem writing to the stream
     */
    public static void serialize(OutputStream out, @Nonnull  JsonElement json, boolean pretty) throws IOException {
        Validate.notNull(out);
        BufferedOutputStream bufferedOut = new BufferedOutputStream(out);
        OutputStreamWriter w = new OutputStreamWriter(bufferedOut, UTF8);
        if(pretty) {
            serialize(w, json, pretty);
        } else {
            json.serialize(w);
            // subtle bug where not flushing this results in empty string when serializing to a ByteArrayOutputStream
            w.flush();
            bufferedOut.flush();
        }
    }

    private static void serialize(@Nonnull  BufferedWriter bw,  JsonElement json, boolean pretty, int indent) throws IOException {
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
                    serialize(bw,value,pretty,indent+1);
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
                boolean nestedPretty=false;
                if(value.isObject()) {
                    nestedPretty=true;
                }
                serialize(bw,value,nestedPretty,indent+1);
                if(arrayIterator.hasNext()) {
                    bw.write(',');
                    newline(bw, indent+1, nestedPretty);
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

    /**
     * The xml specification defines these character hex codes as allowed: #x9 | #xA | #xD | [#x20-#xD7FF] |
     * [#xE000-#xFFFD] | [#x10000-#x10FFFF] Characters outside this range will cause parsers to reject the xml as not
     * well formed. Probably should not allow these in Json either.
     *
     * @param c
     *            a character
     * @return true if character is allowed in an XML document
     */
    public static boolean isAllowedInXml(final int c) {
        boolean ok = false;
        if(c >= 0x10000 && c <= 0x10FFFF) {
            ok = true;
        } else if(c >= 0xE000 && c <= 0xFFFD) {
            ok = true;
        } else if(c >= 0x20 && c <= 0xD7FF) {
            ok = true;
        } else if(c == 0x9 || c == 0xA || c == 0xD) {
            ok = true;
        }
        return ok;
    }

    /**
     * This method escapes strings so that parsers don't break when encountering certain characters. Note, this method
     * is designed to be robust against corrupted input and will simply silently drop illegal characters rather than
     * trying
     * to escape them. E.g. escape control characters other than the common ones are simply dropped from the input.
     * Unlike {@link StringEscapeUtils}, this method does not convert non ascii characters to their unicode escaped
     * notation. Since {@link JsonSerializer} always uses UTF-8 this is not required.
     *
     * @param raw
     *            any string
     * @return the json escaped string
     */
    public static String jsonEscape(String raw) {
        // can't use StringEscapeUtils here because it escapes all non ascii characters and doesn't unescape them.
        // this is unacceptable for most utf8 content where in fact you only want to escape if you really have to

        StringBuilder buf = new StringBuilder(raw.length());
        for (char c : raw.toCharArray()) {
            // escape control characters
            if (c < 32) {
                switch (c) {
                case '\b':
                    buf.append('\\');
                    buf.append('b');
                    break;
                case '\n':
                    buf.append('\\');
                    buf.append('n');
                    break;
                case '\t':
                    buf.append('\\');
                    buf.append('t');
                    break;
                case '\f':
                    buf.append('\\');
                    buf.append('f');
                    break;
                case '\r':
                    buf.append('\\');
                    buf.append('r');
                    break;
                default:
                    // note, these characters are not unescaped.
                    if (c > 0xf) {
                        buf.append("\\u00" + hex(c));
                    } else {
                        buf.append("\\u000" + hex(c));
                    }
                    break;
                }
            } else if (isAllowedInXml(c)) {
                // note, this silently drops characters that would not be allowed in XML anyway.
                switch (c) {
                case '"':
                    buf.append('\\');
                    buf.append('"');
                    break;
                case '\\':
                    buf.append('\\');
                    buf.append('\\');
                    break;
                default:
                    buf.append(c);
                    break;
                }
            } else {
                // simply escape; emojis are apparently outside the xml friendly range
                buf.append("\\u" + hex(c));
            }
        }
        return buf.toString();
    }

    private static String hex(char ch) {
        return Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
    }

    /**
     * @param escaped
     *            a json string that may contain escaped characters
     * @return the unescaped String
     */
    public static String jsonUnescape(String escaped) {
        StringBuilder buf = new StringBuilder(escaped.length());
        char[] chars = escaped.toCharArray();
        if(chars.length >= 2) {
            int i = 1;
            while (i < chars.length) {
                if(chars[i - 1] == '\\') {
                    if(chars[i] == 't') {
                        buf.append('\t');
                        i += 2;
                    } else if(chars[i] == 'n') {
                        buf.append('\n');
                        i += 2;
                    } else if(chars[i] == 'r') {
                        buf.append('\r');
                        i += 2;
                    } else if(chars[i] == '"') {
                        buf.append('"');
                        i += 2;
                    } else if(chars[i] == '\\') {
                        buf.append('\\');
                        i += 2;
                    } else {
                        buf.append(chars[i - 1]);
                        buf.append(chars[i]);
                        i += 2;
                    }
                } else {
                    buf.append(chars[i - 1]);
                    i++;
                }
            }
            if(i == chars.length) {
                // make sure to add the last character
                buf.append(chars[i - 1]);
            }
            return buf.toString();
        } else {
            return escaped;
        }
    }

    private static void newline(final BufferedWriter bw, final int n, final boolean pretty) throws IOException {
        if(pretty) {
            bw.write('\n');
            for(int i = 0; i < n; i++) {
                bw.write('\t');
            }
        }
    }
}
