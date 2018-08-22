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

import com.github.jsonj.JsonArray;
import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonObject;
import com.github.jsonj.JsonPrimitive;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.IllegalNameException;
import nu.xom.converters.DOMConverter;
import org.w3c.dom.DOMImplementation;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Map.Entry;

/**
 * Helper class to get a DOM representation of a json element. This may be
 * useful for example to apply xpath or xsl to a json document. The algorithm
 * for converting is very straight forward:
 * <ol>
 * <li>You have to pick a root element name because json doesn't name its elements (except dictionary keys).</li>
 * <li>Dictionary keys become tags. If needed the names are prepended with an underscore to produce valid xml tag names.
 * E.g. {"1":"one"} becomes &lt;_1&gt;one&lt;/_1&gt;
 * <li>Arrays become ordered lists with an outer ol tag and li tags for each element. Just like in HTML</li>
 * <li>Primitives are rendered as TEXT. Where needed XML escaping is used.</li>
 * </ol>
 * 
 * This class uses an optional maven dependency xom. If you want to use the
 * functionality in this class, you will need to add this dependency to your own
 * project.
 * 
 */
public class JsonXmlConverter {

    private static void append(Element e, JsonPrimitive p) {
        e.appendChild(p.asString());
    }

    private static void append(Element e, JsonObject o) {
        for(Entry<String, JsonElement> entry:o.entrySet()) {
            Element child;
            try {
                child = new Element(entry.getKey());
            } catch (IllegalNameException exc1) {
                child = new Element("_" + entry.getKey());
            }
            JsonElement value = entry.getValue();
            if(value.isArray()) {
                append(child, value.asArray());
            } else if(value.isObject()) {
                append(child, value.asObject());
            } else {
                append(child, value.asPrimitive());
            }

            e.appendChild(child);
        }
    }

    private static void append(Element e, JsonArray p) {
        Element list = new Element("ol");
        for(JsonElement value: p) {
            Element li = new Element("li");
            if(value.isArray()) {
                append(li, value.asArray());
            } else if(value.isObject()) {
                append(li, value.asObject());
            } else {
                append(li, value.asPrimitive());
            }
            list.appendChild(li);

        }
        e.appendChild(list);
    }

    /**
     * Convert any JsonElement into an w3c DOM tree with a default root tag of &lt;root&gt;.
     * 
     * @param value
     *            a json element
     * @return a Document with a default root tag of &lt;root&gt;
     */
    public static org.w3c.dom.Document getW3cDocument(JsonElement value) {
        return getW3cDocument(value, "root");
    }

    /**
     * Convert any JsonElement into an w3c DOM tree.
     * 
     * @param value
     *            a json element
     * @param rootName
     *            the root name of the xml
     * @return a Document
     */
    public static org.w3c.dom.Document getW3cDocument(JsonElement value, String rootName) {
        Element root = getElement(value, rootName);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            DOMImplementation impl = builder.getDOMImplementation();

            return DOMConverter.convert(new Document(root), impl);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    static Element getElement(JsonElement value, String rootName) {
        Element root = new Element(rootName);
        if(value.isArray()) {
            append(root, value.asArray());
        } else if(value.isObject()) {
            append(root, value.asObject());
        } else {
            append(root, value.asPrimitive());
        }
        return root;
    }
}
