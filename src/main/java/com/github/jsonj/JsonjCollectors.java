package com.github.jsonj;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static com.github.jsonj.tools.JsonBuilder.fromObject;

public class JsonjCollectors {
    public static @Nonnull Collector<Object, JsonSet, JsonSet> set() {
        return new Collector<Object, JsonSet, JsonSet>() {
            @Override
            public Supplier<JsonSet> supplier() {
                return () -> new JsonSet();
            }

            @Override
            public BiConsumer<JsonSet, Object> accumulator() {
                return (a, e) -> {a.add(fromObject(e));};
            }

            @Override
            public BinaryOperator<JsonSet> combiner() {
                return (l,r) -> {l.addAll(r); return l;};
            }

            @Override
            public Function<JsonSet, JsonSet> finisher() {
                return a -> a;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return new HashSet<>();
            }};
    }

    public static @Nonnull Collector<Object, JsonArray, JsonArray> array() {
        return new Collector<Object, JsonArray, JsonArray>() {
            @Override
            public Supplier<JsonArray> supplier() {
                return () -> new JsonArray();
            }

            @Override
            public BiConsumer<JsonArray, Object> accumulator() {
                return (a, e) -> {a.add(fromObject(e));};
            }

            @Override
            public BinaryOperator<JsonArray> combiner() {
                return (l,r) -> {l.addAll(r); return l;};
            }

            @Override
            public Function<JsonArray, JsonArray> finisher() {
                return a -> a;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return new HashSet<>();
            }};
    }
}
