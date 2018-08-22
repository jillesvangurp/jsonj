package com.github.jsonj.assertions;

import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonPrimitive;
import org.assertj.core.api.AbstractAssert;

import static com.github.jsonj.tools.JsonBuilder.fromObject;

public class JsonPrimitiveAssert extends AbstractAssert<JsonPrimitiveAssert, JsonPrimitive> {
    JsonPrimitiveAssert(JsonPrimitive actual) {
        super(actual, JsonPrimitiveAssert.class);
    }

    @Override
    public JsonPrimitiveAssert isEqualTo(Object o) {
        isNotNull();
        JsonElement fromObject = fromObject(o);
        if(!fromObject.isPrimitive()) {
            failWithMessage("<%s> is not a json primitive", o.toString());
        }
        if(!actual.equals(fromObject)) {
            failWithMessage("expected <%s> to be equal to <%s>", o.toString(), actual);
        }
        return this;
    }

    @Override
    public JsonPrimitiveAssert isNotEqualTo(Object o) {
        isNotNull();
        JsonElement fromObject = fromObject(o);
        if(!fromObject.isPrimitive()) {
            failWithMessage("<%s> is not a json primitive", o.toString());
        }
        if(actual.equals(fromObject)) {
            failWithMessage("expected <%s> to be not equal to <%s>", o.toString(), actual);
        }
        return this;
    }
}
