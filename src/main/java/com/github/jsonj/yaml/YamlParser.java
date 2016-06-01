package com.github.jsonj.yaml;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.jsonj.tools.JsonFactoryBasedParser;

public class YamlParser implements JsonFactoryBasedParser {
    private final YAMLFactory factory = new YAMLFactory();

    public YamlParser() {
    }

    @Override
    public JsonFactory factory() {
        return factory;
    }
}
