package com.github.jsonj;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import static com.github.jsonj.tools.JsonBuilder.nullValue;

public class JacksonObjectDeserializer extends JsonDeserializer<JsonElement> {

    @Override
    public JsonElement deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return toJsonj(p.readValueAsTree());
    }

    private JsonElement toJsonj(JsonNode t) {
        if(t.isObject()) {
            ObjectNode on = (ObjectNode) t;
            IJsonObject o;
            if(on.size()>100) {
                o = new MapBasedJsonObject();
            } else {
                o = new JsonObject();
            }

            Iterator<Entry<String, JsonNode>> fieldsIterator = on.fields();

            while (fieldsIterator.hasNext()) {
                Entry<String, JsonNode> entry = fieldsIterator.next();
                o.put(entry.getKey(), toJsonj(entry.getValue()));
            }
            return o;
        } else if(t.isArray()) {
            ArrayNode an = (ArrayNode) t;
            JsonArray a = new JsonArray();
            Iterator<JsonNode> elements = an.elements();
            while (elements.hasNext()) {
                JsonNode jsonNode = elements.next();
                a.add(toJsonj(jsonNode));
            }
            return a;
        } else {
            ValueNode vn = (ValueNode) t;
            JsonNodeType nodeType = vn.getNodeType();
            switch (nodeType) {
            case NUMBER:
                return new JsonPrimitive(vn.numberValue());
            case BOOLEAN:
                return new JsonPrimitive(vn.asBoolean());
            case STRING:
                return new JsonPrimitive(vn.textValue());
            case NULL:
                return nullValue();
            default:
                throw new IllegalStateException("unexpected JsonNodeType " + nodeType);
            }
        }
    }
}
