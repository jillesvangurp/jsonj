package com.github.jsonj.assertions;

import com.github.jsonj.JsonArray;
import org.assertj.core.api.AbstractAssert;

public class JsonArrayAssert extends AbstractAssert<JsonArrayAssert, JsonArray> {
    JsonArrayAssert(JsonArray actual) {
        super(actual, JsonArrayAssert.class);
    }

    public JsonSetAssert set() {
        return new JsonSetAssert(actual.asSet());
    }

    public JsonArrayAssert hasSize(int i) {
        isNotNull();
        if(actual.size() != i) {
            failWithMessage("expected size of <%s> but was <%s>", i, actual.size());
        }
        return this;
    }

    public JsonArrayAssert isNotEmpty() {
        if(actual.size() == 0) {
            failWithMessage("expected array is not empty");
        }
        return this;
    }

    public JsonArrayAssert isEmpty() {
        if(actual.size() > 0) {
            failWithMessage("expected array is empty but has size <%s>" + actual.size());
        }
        return this;
    }

    public JsonElementAssert first() {
        isNotEmpty();
        return new JsonElementAssert(actual.first());
    }

    public JsonElementAssert last() {
        isNotEmpty();
        return new JsonElementAssert(actual.first());
    }

    public JsonElementAssert at(int i) {
        isNotEmpty();
        return new JsonElementAssert(actual.get(i));
    }
}
