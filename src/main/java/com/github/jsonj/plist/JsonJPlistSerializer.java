package com.github.jsonj.plist;

import com.dd.plist.BinaryPropertyListWriter;
import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.github.jsonj.JsonArray;
import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonObject;
import com.github.jsonj.JsonPrimitive;

import java.io.IOException;
import java.io.OutputStream;

public class JsonJPlistSerializer {

    public static void serialize(JsonElement e, OutputStream out) throws IOException {
        NSObject nsObject = toNsObject(e);
        BinaryPropertyListWriter.write(out, nsObject);
    }

    public static NSObject toNsObject(JsonElement e) {
        if(e.isArray()) {
            return toNsObject(e.asArray());
        } else if(e.isObject()) {
            return toNsObject(e.asObject());
        } else {
            return toNsObject(e.asPrimitive());
        }
    }

    public static NSObject toNsObject(JsonObject o) {
        NSDictionary dict = new NSDictionary();
        o.forEach((k,v) -> {
            if(v.isObject()) {
                dict.put(k, toNsObject(v.asObject()));
            } else if(v.isArray()) {
                dict.put(k, toNsObject(v.asArray()));
            } else {
                dict.put(k, toNsObject(v.asPrimitive()));
            }
        });
        return dict;

    }

    public static NSObject toNsObject(JsonArray a) {
        NSArray array = new NSArray(a.size());
        int index=0;
        for(JsonElement v: a) {
            NSObject value;
            if(v.isObject()) {
                value=toNsObject(v.asObject());
            } else if(v.isArray()) {
                value = toNsObject(v.asArray());
            } else {
                value=toNsObject(v.asPrimitive());
            }
            array.setValue(index++, value);
        }

        return array;
    }

    public static NSObject toNsObject(JsonPrimitive p) {
        NSObject o;
        if(p.isBoolean()) {
            o = new NSNumber(p.asBoolean());
        } else if(p.isNumber()) {
            if(p.asString().contains(".")) {
                o = new NSNumber(p.asDouble());
            } else {
                o = new NSNumber(p.asLong());
            }
        } else if(p.isString()) {
            o = new NSString(p.asString());
        } else {
            o = null;
        }
        return o;
    }
}
