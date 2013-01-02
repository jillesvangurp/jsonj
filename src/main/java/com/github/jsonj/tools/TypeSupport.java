package com.github.jsonj.tools;

import static com.github.jsonj.tools.JsonBuilder.array;
import static com.github.jsonj.tools.JsonBuilder.primitive;

import com.github.jsonj.JsonArray;

/**
 * Converting between some common types and json can be a bit tedious. The ones supported in this class
 * are by no means exhaustive. I tend to add to this class on a need to have basis.
 */
public class TypeSupport {

    /**
     * Convert 2d array of doubles to a geojson style multi polygon in a 3D array of arrays of [latitude,longitude]
     * arrays.
     *
     * @return a JsonArray
     */
    public static JsonArray getGeoJsonPolygon(double[][] coordinates) {
        JsonArray points = array();
        for (int i=0;i<coordinates.length ; i++) {
            double[] values = coordinates[i];
            JsonArray subArray = array();
            for (int j = 0; j < values.length; j++) {
                subArray.add(primitive(values[j]));
            }
            points.add(subArray);
        }
        if(coordinates[0][0] != coordinates[coordinates.length-1][0] || coordinates[0][1] != coordinates[coordinates.length-1][1]) {
            // add last coordinate to close the polygon
            points.add(array(coordinates[0][0], coordinates[0][1]));
        }

        JsonArray geoJsonPolygon = array();
        geoJsonPolygon.add(points);
        return geoJsonPolygon;
    }

    /**
     * @param geoJsonMultiPolygon
     * @return the 2d array of coordinates for the first polygon in the GeoJson multipolygon.
     */
    public static double[][] fromGeoJsonPolygon(JsonArray geoJsonMultiPolygon) {
        JsonArray firstPolygon = geoJsonMultiPolygon.get(0).asArray();
        double[][] result = new double[firstPolygon.size()][firstPolygon.get(0).asArray().size()];
        for(int i =0; i<firstPolygon.size();i++) {
            JsonArray subArray = firstPolygon.get(i).asArray();
            for(int j =0; j<subArray.size();j++) {
                result[i][j] = subArray.get(j).asPrimitive().asDouble();
            }
        }
        return result;
    }

    /**
     * Convert json array of arrays of double primitives to a double[][].
     * @param arrayOfArrays
     * @return a double[][]
     */
    public static double[][] convertTo2DDoubleArray(JsonArray arrayOfArrays) {
        double[][] result = new double[arrayOfArrays.size()][arrayOfArrays.get(0).asArray().size()];
        for(int i =0; i<arrayOfArrays.size();i++) {
            JsonArray subArray = arrayOfArrays.get(i).asArray();
            for(int j =0; j<subArray.size();j++) {
                result[i][j] = subArray.get(j).asPrimitive().asDouble();
            }
        }
        return result;
    }
}
