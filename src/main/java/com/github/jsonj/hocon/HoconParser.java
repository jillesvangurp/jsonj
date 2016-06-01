package com.github.jsonj.hocon;

import com.fasterxml.jackson.core.JsonFactory;
import com.github.jsonj.tools.JsonFactoryBasedParser;
import com.jasonclawson.jackson.dataformat.hocon.HoconFactory;

public class HoconParser implements JsonFactoryBasedParser {
    private final HoconFactory factory = new HoconFactory();

    public HoconParser() {
    }

    @Override
    public JsonFactory factory() {
        return factory;
    }
}
