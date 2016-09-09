package com.github.jsonj.tools;

import static com.github.jsonj.tools.GeoJsonSupport.swapLatLon;
import static com.github.jsonj.tools.GeoJsonSupport.toJsonJLineString;
import static com.github.jsonj.tools.GeoJsonSupport.toJsonJPoint;
import static com.github.jsonj.tools.JsonBuilder.array;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.jsonj.JsonArray;
import javax.annotation.Nonnull;
import org.testng.annotations.Test;

@Test
public class GeoJsonSupportTest {
    @Nonnull double[] point1=new double[] {1.0,2.0};
    @Nonnull double[] point2=new double[] {2.0,2.0};
    @Nonnull double[] point3=new double[] {2.0,1.0};
    @Nonnull double[][] lineString1=new double[][] {point1,point2,point3};
    @Nonnull double[][] lineString2=new double[][] {point3,point2,point1};

    public void shouldSwapPoint() {
        assertThat(swapLatLon(toJsonJPoint(point1)), is(array(2.0,1.0)));
    }

    public void shouldConvertLineString() {
        JsonArray ls = toJsonJLineString(lineString1);
        assertThat(ls.first().asArray(), is(toJsonJPoint(point1)));
        assertThat(ls.last().asArray(), is(toJsonJPoint(point3)));
    }

    public void shouldSwapPoints() {
        JsonArray ls = toJsonJLineString(lineString1);
        JsonArray swapped = swapLatLon(ls);
        assertThat(swapped.first().asArray(), is(array(2.0,1.0)));
    }

    public void shouldConvertLineStringToPolygon() {
        JsonArray polygon = GeoJsonSupport.lineStringToPolygon(lineString1);
        assertThat(polygon.first().asArray().last().asArray(), is(toJsonJPoint(point1)));
    }
}
