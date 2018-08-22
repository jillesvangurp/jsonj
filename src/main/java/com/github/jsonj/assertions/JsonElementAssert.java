package com.github.jsonj.assertions;

import com.github.jsonj.JsonElement;
import org.assertj.core.api.AbstractAssert;

public class JsonElementAssert extends AbstractAssert<JsonElementAssert, JsonElement> {

    JsonElementAssert(JsonElement actual) {
        super(actual, JsonElementAssert.class);
    }

    public JsonElementAssert isObject() {
        isNotNull();
        if(!actual.isObject()) {
            failWithMessage("should be object but was <%s>", actual.type());
        }
        return this;
    }

    public JsonElementAssert isArray() {
        isNotNull();
        if(!actual.isArray()) {
            failWithMessage("should be array but was <%s>", actual.type());
        }
        return this;
    }

    public JsonElementAssert isPrimitive() {
        isNotNull();
        if(!actual.isPrimitive()) {
            failWithMessage("should be primitive but was <%s>", actual.type());
        }
        return this;
    }

    public JsonPrimitiveAssert primitive() {
        isPrimitive();
        return new JsonPrimitiveAssert(actual.asPrimitive());
    }

    public JsonSetAssert set() {
        isArray();
        return new JsonSetAssert(actual.asSet());
    }

    public JsonArrayAssert array() {
        isArray();
        return new JsonArrayAssert(actual.asArray());
    }

    public JsonObjectAssert object() {
        isObject();
        return new JsonObjectAssert(actual.asObject());
    }
}
