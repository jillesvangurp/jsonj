package com.github.jsonj.tools;

import com.github.jsonj.JsonArray;
import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonObject;
import com.github.jsonj.JsonPrimitive;
import com.github.jsonj.MapBasedJsonObject;
import com.github.jsonj.exceptions.JsonParseException;
import java.util.LinkedList;

final class JsonHandler {
    // use a simple stack mechanism to reconstruct the tree
    LinkedList<JsonElement> stack = new LinkedList<>();
    boolean isObject = false;

    public JsonElement get() {
        // the remaining element on the stack is the fully parsed
        // JsonElement
        if(stack.size() == 0) {
            // happens when parsing empty string or just whitespace
            throw new JsonParseException("no elements parsed");
        }
        return stack.getLast();
    }

    public boolean startObjectEntry(final String entry) {
        stack.add(JsonBuilder.primitive(entry));
        return true;
    }

    public boolean startObject() {
        isObject = true;
        stack.add(new JsonObject());
        return true;
    }

    public void startJSON() {
        // clean up from previous runs
        stack.clear();
    }

    public boolean startArray() {
        isObject = false;
        stack.add(new JsonArray());
        return true;
    }

    public boolean primitive(final Object object) {
        JsonPrimitive primitive;
        primitive = new JsonPrimitive(object);
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

    public boolean endObjectEntry() {
        JsonElement value = stack.pollLast();
        JsonElement e = stack.peekLast();
        if (e.isPrimitive()) {
            e = stack.pollLast();
            JsonElement last = stack.peekLast();
            if (last.isObject()) {
                JsonObject container = last.asObject();
                String key = e.asPrimitive().asString();
                // automatically switch to linked hashmap based implementations if more than 100 keys
                // at this point inserts will just keep on getting more expensive than it is worth
                if(container instanceof JsonObject && container.size()>100) {
                    // replace with more efficient implementation
                    stack.poll();
                    container = new MapBasedJsonObject(container);
                    stack.add(container);
                }
                container.put(key, value);
            } else if (last.isArray()) {
                throw new IllegalStateException("shouldn't happen");
            }
        }
        return true;
    }

    public boolean endObject() {
        if (stack.size() > 1 && stack.get(stack.size() - 2).isArray()) {
            JsonElement object = stack.pollLast();
            stack.peekLast().asArray().add(object);
        }
        return true;
    }

    public void endJSON() {
    }

    public boolean endArray() {
        if (stack.size() > 1 && stack.get(stack.size() - 2).isArray()) {
            JsonElement value = stack.pollLast();
            stack.peekLast().asArray().add(value);
        }
        return true;
    }
}