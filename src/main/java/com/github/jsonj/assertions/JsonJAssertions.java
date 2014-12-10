package com.github.jsonj.assertions;

import com.github.jsonj.JsonArray;
import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonObject;
import com.github.jsonj.JsonPrimitive;
import com.github.jsonj.JsonSet;

public class JsonJAssertions {
    public static JsonPrimitiveAssert assertThat(JsonPrimitive primitive) {
        return new JsonPrimitiveAssert(primitive);
    }

    public static JsonObjectAssert assertThat(JsonObject object) {
        return new JsonObjectAssert(object);
    }

    public static JsonArrayAssert assertThat(JsonArray array) {
        return new JsonArrayAssert(array);
    }

    public static JsonSetAssert assertThat(JsonSet set) {
        return new JsonSetAssert(set);
    }

    public static JsonElementAssert assertThat(JsonElement element) {
        return new JsonElementAssert(element);
    }
}
