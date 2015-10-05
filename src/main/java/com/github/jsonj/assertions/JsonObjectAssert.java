package com.github.jsonj.assertions;

import com.github.jsonj.JsonObject;
import org.assertj.core.api.AbstractAssert;

public class JsonObjectAssert extends AbstractAssert<JsonObjectAssert, JsonObject>{
    JsonObjectAssert(JsonObject object) {
        super(object, JsonObjectAssert.class);
    }

    public JsonObjectAssert hasFields(String...fields) {
        for(String field: fields) {
            if(!actual.keySet().contains(field)) {
                failWithMessage("expected actual to have field <%s>", field);
            }
        }
        return this;
    }

    public JsonObjectAssert hasNoFields(String...fields) {
        for(String field: fields) {
            if(actual.keySet().contains(field)) {
                failWithMessage("expected actual to not have field <%s>", field);
            }
        }
        return this;
    }
}
