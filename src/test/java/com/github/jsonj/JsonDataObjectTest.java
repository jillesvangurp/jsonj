package com.github.jsonj;

import static com.github.jsonj.tools.JsonBuilder.field;
import static com.github.jsonj.tools.JsonBuilder.object;
import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Nonnull;
import org.testng.annotations.Test;

@Test
public class JsonDataObjectTest {

    class MyPoint implements JsonDataObject {
        private static final long serialVersionUID = 1857812728597159873L;

        private final @Nonnull JsonObject wrapped;
        public MyPoint(double x, double y) {
            wrapped=object(field("x",x),field("y",y));
        }

        @Override
        public JsonObject getJsonObject() {
            return wrapped;
        }

        @Override
        public boolean isMutable() {
            return true;
        }

        public double x() {
            return getDouble("x");
        }

        public double y() {
            return getDouble("y");
        }

        @Override
        public boolean equals(Object obj) {
            return wrapped.equals(obj);
        }

        @Override
        public int hashCode() {
            return wrapped.hashCode();
        }

        @Override
        public String toString() {
            return wrapped.toString();
        }
    }
    public void shouldHaveDomainObjects() {
        MyPoint p = new MyPoint(666, 42);
        assertThat(p.x()).isEqualTo(666);
        assertThat(p.y()).isEqualTo(42);
    }

    public void shouldALlowPutsWithPolymorphicTypes() {
        MyPoint p1 = new MyPoint(1, 1);
        MyPoint p2 = new MyPoint(2, 2);
        p2.put("nestedp", p1);
        assertThat(p2.getObject("nestedp")).isEqualTo(p1.getJsonObject());
        p2.put("nestedp", p1.getJsonObject());
        assertThat(p2.getObject("nestedp")).isEqualTo(p1.getJsonObject());
        p2.put("x", 3);
        assertThat(p2.x()).isEqualTo(3);
    }
}
