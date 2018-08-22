package com.github.jsonj.plist;

import com.dd.plist.Base64;
import com.dd.plist.BinaryPropertyListParser;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListFormatException;
import com.github.jsonj.JsonArray;
import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static com.github.jsonj.tools.JsonBuilder.array;
import static com.github.jsonj.tools.JsonBuilder.nullValue;
import static com.github.jsonj.tools.JsonBuilder.primitive;

public class JsonJPlistParser {
    public static JsonElement parse(InputStream is) throws IOException {
        try {
            return parse(BinaryPropertyListParser.parse(is));
        } catch (PropertyListFormatException e) {
            throw new IllegalArgumentException("not a plist");
        }
    }

    public static JsonElement parse(byte[] bytes) {
        try {
            return parse(BinaryPropertyListParser.parse(bytes));
        } catch (IOException | PropertyListFormatException e) {
            throw new IllegalArgumentException("not a plist");
        }
    }

    public static JsonElement parse(NSObject object) {
        return fromObject(object.toJavaObject());
    }

    private static JsonElement fromObject(Object v) {
        Class<?> clazz = v.getClass();
        if(Map.class.isAssignableFrom(clazz)) {
            return convert(convert((Map<?,?>)v));
        } else if(clazz.isAssignableFrom(byte[].class)) {
            return convert((byte[])v);
        } else if(clazz.isAssignableFrom(Object[].class)) {
            return convert((Object[])v);
        } else if(Set.class.isAssignableFrom(clazz)) {
            return convert((Set<?>)v);
        } else if(clazz.isAssignableFrom(Long.class)) {
            return convert((long)v);
        } else if(clazz.isAssignableFrom(Integer.class)) {
            return convert((int)v);
        } else if(clazz.isAssignableFrom(Double.class)) {
            return convert((double)v);
        } else if(clazz.isAssignableFrom(Boolean.class)) {
            return convert((boolean)v);
        } else if(clazz.isAssignableFrom(String.class)) {
            return convert((String)v);
        } else if(clazz.isAssignableFrom(Date.class)) {
            return convert((Date)v);
        } else if(JsonElement.class.isAssignableFrom(clazz)) {
            return (JsonElement) v;
        } else {
            // default to null
            return nullValue();
        }
    }

    private static JsonElement convert(double v) {
        return primitive(v);
    }

    private static JsonArray convert(Object[] os) {
        JsonArray a = array();
        for(Object o:os) {
            a.add(fromObject(o));
        }
        return a;

    }

    private static JsonArray convert(Set<?> v) {
        JsonArray a = array();
        for(Object o:v) {
            a.add(fromObject(o));
        }
        return a;
    }

    private static JsonObject convert(Map<?,?> m) {
        JsonObject object = new JsonObject();
        m.forEach((k,v) -> {
                object.put(k.toString(),fromObject(v));
        });
        return object;
    }

    private static JsonElement convert(Date v) {
        return primitive(Instant.ofEpochMilli(v.getTime()));
    }

    private static JsonElement convert(String v) {
        return primitive(v);
    }

    private static JsonElement convert(boolean v) {
        return primitive(v);
    }

    private static JsonElement convert(long v) {
        return primitive(v);
    }

    private static JsonElement convert(byte[] v) {
        String encoded = Base64.encodeBytes(v);
        return primitive(encoded);
    }
}
