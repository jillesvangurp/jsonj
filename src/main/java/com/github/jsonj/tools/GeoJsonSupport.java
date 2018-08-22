package com.github.jsonj.tools;

import com.github.jsonj.JsonArray;
import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonObject;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;

import static com.github.jsonj.tools.JsonBuilder.array;
import static com.github.jsonj.tools.JsonBuilder.object;
import static com.github.jsonj.tools.JsonBuilder.primitive;

/**
 * GeoJson uses multi dimensional arrays to support points, lines, polygons, multi polygons, etc. This class offers some
 * convenience methods for converting to and from native arrays as well as methods for creating shape objects of various
 * types.
 */
public class GeoJsonSupport {

    /**
     * While many services use the order of latitude,longitude. GeoJson uses longitude,latitude in its arrays.
     * This method uses the geoJson convention
     * @param latitude latitude
     * @param longitude longitude
     * @return [longitude,latitude] as a JsonArray
     */
    public static @Nonnull JsonArray point(double latitude, double longitude) {
        return array(longitude,latitude);
    }

    public static @Nonnull JsonArray toJsonJPoint(@Nonnull double[] coordinates) {
        return array(coordinates[0],coordinates[1]);
    }

    public static @Nonnull double[] fromJsonJPoint(@Nonnull JsonArray coordinates) {
        return new double[] {coordinates.get(0).asDouble(), coordinates.get(1).asDouble()};
    }

    /**
     * Convert 2d array of doubles to a geojson style multi polygon in a 3D array of arrays of [latitude,longitude]
     * arrays.
     * @param lineString 2d array of doubles
     * @return a JsonArray array
     */
    public static @Nonnull JsonArray toJsonJLineString(@Nonnull double[][] lineString) {
        JsonArray points = array();
        for (double[] values : lineString) {
            JsonArray subArray = array();
            for (double value : values) {
                subArray.add(primitive(value));
            }
            points.add(subArray);
        }

        return points;
    }

    /**
     * Convert json array of arrays of double primitives to a double[][].
     * @param lineString 2d JsonArray of doubles
     * @return a double[][] 2d array of doubles
     */
    public static @Nonnull double[][] fromJsonJLineString(@Nonnull JsonArray lineString) {
        double[][] result = new double[lineString.size()][lineString.get(0).asArray().size()];
        for(int i =0; i<lineString.size();i++) {
            JsonArray subArray = lineString.get(i).asArray();
            for(int j =0; j<subArray.size();j++) {
                result[i][j] = subArray.get(j).asPrimitive().asDouble();
            }
        }
        return result;
    }

    public static@Nonnull  JsonArray toJsonJPolygon(@Nonnull double[][] polygon) {
        return toJsonJPolygon(new double [][][] {polygon});
    }


    public static @Nonnull JsonArray toJsonJPolygon(@Nonnull double[][][] polygon) {
        JsonArray result = array();
        for (double[][] element : polygon) {
            Validate.notNull(element);
            result.add(toJsonJLineString(element));
        }
        return result;
    }

    public static @Nonnull double[][][] fromJsonJPolygon(@Nonnull JsonArray polygon) {
        double[][][] result = new double[polygon.size()][0][0];
        int i=0;
        for(JsonElement e: polygon) {
            result[i]=fromJsonJLineString(e.asArray());
            i++;
        }
        return result;
    }

    public static @Nonnull JsonArray toJsonJMultiPolygon(@Nonnull double[][][][] multiPolygon) {
        JsonArray result = array();
        for (double[][][] element : multiPolygon) {
            Validate.notNull(element);
            result.add(toJsonJPolygon(element));
        }
        return result;
    }

    public static @Nonnull double[][][][] fromJsonJMultiPolygon(@Nonnull JsonArray multiPolygon) {
        double[][][][] result = new double[multiPolygon.size()][0][0][0];
        int i=0;
        for(JsonElement e: multiPolygon) {
            result[i]=fromJsonJPolygon(e.asArray());
            i++;
        }
        return result;
    }


    /**
     * Takes a two dimensional linestring and creates a three dimensional polygon from it. Also closes the polygon if needed.
     * @param lineString 2d array of doubles
     * @return JsonArray with the 3d polygon
     */
    public static @Nonnull JsonArray lineStringToPolygon(@Nonnull double[][] lineString) {
        JsonArray jsonJLineString = toJsonJLineString(lineString);
        if (lineString[0][0] != lineString[lineString.length - 1][0] || lineString[0][1] != lineString[lineString.length - 1][1]) {
            // add last coordinate to close the polygon
            jsonJLineString.add(array(lineString[0][0], lineString[0][1]));
        }
        JsonArray result = array();
        result.add(jsonJLineString);
        return result;
    }

    /**
     * Takes a two dimensional linestring and creates a three dimensional polygon from it. Also closes the polygon if needed.
     * @param lineString 2d JsonArray
     * @return 3d double array with the polygon
     */
    public static @Nonnull JsonArray lineStringToPolygon(@Nonnull JsonArray lineString) {
        if(!lineString.first().equals(lineString.last())) {
            JsonElement e = lineString.first().deepClone();
            lineString.add(e);
        }
        return array(lineString);
    }

    /**
     * GeoJson specifies the order of coordinates as x,y or in geo terms longitude followed by latitude.
     * Most data sources stick to latitude, longitude though. This function, swaps the two for any dimension array.
     *
     *
     * @param array 2d array with latitude, longitude pairs
     * @return swapped array 2d array with longitude, latitude pairs
     */
    public static @Nonnull JsonArray swapLatLon(@Nonnull JsonArray array) {
        if(array.isNotEmpty() && array.first().isArray()) {
            for(JsonElement e: array) {
                swapLatLon(e.asArray());
            }
        } else {
            if(array.size() < 2) {
                throw new IllegalArgumentException("need at least two coordinates");
            }
            JsonElement first = array.get(0);
            JsonElement second = array.get(1);
            array.set(0, second);
            array.set(1, first);

        }
        return array;
    }

    public static @Nonnull JsonObject shape(@Nonnull String type, @Nonnull JsonArray coordinates) {
        return object().put("type", type).put("coordinates", coordinates).get();
    }

    public static @Nonnull JsonObject pointShape(double latitude, double longitude) {
        return shape("Point", toJsonJPoint(new double[] {longitude, longitude}));
    }

    public static @Nonnull JsonObject pointShape(@Nonnull double[] coordinates) {
        return shape("Point", toJsonJPoint(coordinates));
    }

    public static @Nonnull JsonObject lineStringShape(@Nonnull double[][] coordinates) {
        return shape("LineString", toJsonJLineString(coordinates));
    }

    public static @Nonnull JsonObject polygonShape(@Nonnull double[][] coordinates) {
        return shape("Polygon", toJsonJPolygon(coordinates));
    }

    public static @Nonnull JsonObject polygonShape(@Nonnull double[][][] coordinates) {
        return shape("Polygon", toJsonJPolygon(coordinates));
    }

    public static @Nonnull JsonObject multiPolygonShape(@Nonnull double[][][][] coordinates) {
        return shape("MultiPolygon", toJsonJMultiPolygon(coordinates));
    }

}
