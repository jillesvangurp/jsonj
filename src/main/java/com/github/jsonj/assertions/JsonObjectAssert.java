package com.github.jsonj.assertions;

import org.assertj.core.api.AbstractAssert;

import com.github.jsonj.JsonObject;

public class JsonObjectAssert extends AbstractAssert<JsonObjectAssert, JsonObject>{
    JsonObjectAssert(JsonObject object) {
        super(object, JsonObjectAssert.class);
    }
}
