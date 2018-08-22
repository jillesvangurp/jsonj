package com.github.jsonj.assertions;

import com.github.jsonj.JsonSet;
import org.assertj.core.api.AbstractAssert;

import static com.github.jsonj.tools.JsonBuilder.fromObject;

public class JsonSetAssert extends AbstractAssert<JsonSetAssert, JsonSet> {

    JsonSetAssert(JsonSet actual) {
        super(actual, JsonSetAssert.class);
    }

    public JsonSetAssert shouldContain(Object...elements) {
        isNotNull();
        for(Object e:elements) {
            if(!actual.contains(fromObject(e))) {
                failWithMessage("Expected JsonSet to contain <%s> but was <%s>", e, actual.toString());
            }
        }
        return this;
    }

    public JsonSetAssert hasSize(int i) {
        return array().hasSize(i).set();
    }

    public JsonSetAssert isNotEmpty() {
        return array().isNotEmpty().set();
    }

    public JsonSetAssert isEmpty() {
        return array().isEmpty().set();
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

    public JsonSetAssert shouldNotContain(Object...elements) {
        isNotNull();
        for(Object e:elements) {
            if(actual.contains(fromObject(e))) {
                failWithMessage("Expected JsonSet to not contain <%s> but was <%s>", e, actual.toString());
            }
        }
        return this;
    }

    public JsonArrayAssert array() {
        return new JsonArrayAssert(actual.asArray());
    }
}
