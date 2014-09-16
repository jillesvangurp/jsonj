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

import static com.github.jsonj.tools.JsonBuilder.array;
import static com.github.jsonj.tools.JsonBuilder.object;
import nu.xom.Document;
import nu.xom.Element;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.jsonj.JsonObject;

@Test
public class JsonXmlConverterTest {
    public void shouldConvertToXml() {
        JsonObject object = sampleJson();
        Element element = JsonXmlConverter.getElement(object, "r");
        Document document = new Document(element);
        String xml = document.toXML();
        Assert.assertTrue(xml.contains("<r>"), "should contain the opening tag <r>");
    }

    public void shouldGetW3cDomTree() {
        JsonObject sampleJson = sampleJson();
        org.w3c.dom.Document domNode = JsonXmlConverter.getW3cDocument(sampleJson, "sample");
        Assert.assertNotNull(domNode);
        Assert.assertEquals(domNode.getElementsByTagName("sample").getLength(), 1);
    }

    public void shouldGetW3cDomTreeWithDefaultRoot() {
        JsonObject sampleJson = sampleJson();
        org.w3c.dom.Document domNode = JsonXmlConverter.getW3cDocument(sampleJson, "root");
        Assert.assertNotNull(domNode);
        Assert.assertEquals(domNode.getElementsByTagName("root").getLength(), 1);
    }

    private JsonObject sampleJson() {
        JsonObject object = object().put("foo", "bar").put("bar", array("foo","bar")).put("foobar", object().put("1", 1).put("2", 2).get()).put("escapeme", "><").get();
        return object;
    }
}
