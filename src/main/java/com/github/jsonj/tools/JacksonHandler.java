package com.github.jsonj.tools;

import com.fasterxml.jackson.core.JsonToken;
import com.github.jsonj.JsonElement;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Internal handler class used for the JsonParser and YamlParser
 */
public class JacksonHandler {
    public static @Nonnull JsonElement parseContent(com.fasterxml.jackson.core.JsonParser parser, JsonjSettings settings) throws IOException, com.fasterxml.jackson.core.JsonParseException {
        JsonHandler handler = new JsonHandler(settings);

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
                if(parser.getTextLength() < 19) { // Long.MAX_VALUE == 20 characters long, so should be fine up until there
                    handler.primitive(parser.getNumberValue());
                } else {
                    handler.primitive(parser.getBigIntegerValue());
                }
                endObjEntryIfNeeded(handler, stack);
                break;
            case VALUE_NUMBER_FLOAT:
                if(parser.getTextLength() < 8) { // beyond this size you may trigger E notation pretty easily e.g. 12345678 becomes 1.2345678E7.
                    handler.primitive(parser.getNumberValue());
                } else {
                    handler.primitive(parser.getDecimalValue());
                }
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
            case NOT_AVAILABLE:
                // non blocking parser may sometimes fail to produce token: ignore
                break;
            case VALUE_EMBEDDED_OBJECT:
                throw new IllegalStateException("unexpected VALUE_EMBEDDED_OBJECT (should not happen) " + nextToken);
            default:
                throw new IllegalStateException("unexpected token " + nextToken);
            }
        }
        handler.endJSON();
        return handler.get();
    }

    private static void endObjEntryIfNeeded(JsonHandler handler, LinkedList<Boolean> stack) {
        if(stack.size() > 0 && stack.peek()) {
            handler.endObjectEntry();
            stack.pop();
        }
    }

}
