package com.github.jsonj.tools;

import com.github.jsonj.JsonArray;
import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonObject;
import com.github.jsonj.JsonPrimitive;
import com.github.jsonj.MapBasedJsonObject;
import com.github.jsonj.SimpleIntMapJsonObject;
import com.github.jsonj.exceptions.JsonParseException;

import java.util.LinkedList;

final class JsonHandler {
    // use a simple stack mechanism to reconstruct the tree
    LinkedList<JsonElement> stack = new LinkedList<>();
    boolean isObject = false;
    private final JsonjSettings settings;

    public JsonHandler(JsonjSettings settings) {
        this.settings = settings;
    }

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
        if(settings.useEfficientStringBasedJsonObject()) {
            stack.add(new SimpleIntMapJsonObject());
        } else {
            stack.add(new JsonObject());
        }
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
        if (e.isPrimitive()) { // field name
            e = stack.pollLast();
            JsonObject container = stack.peekLast().asObject();
            String key = e.asPrimitive().asString();
            container.put(key, value);
            if(container.size()>settings.upgradeThresholdToMapBasedJsonObject() && !MapBasedJsonObject.class.equals(container.getClass())) {
                JsonElement removed = stack.pollLast();
                MapBasedJsonObject newContainer = new MapBasedJsonObject(removed.asObject());
                stack.add(newContainer);
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