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
     * Convert 2d array of doubles to the json equivalent. Useful for polygons.
     * @param twoDArray
     * @return a JsonArray
     */
    public static JsonArray convert2DDoubleArray(double[][] twoDArray) {
        JsonArray result = array();
        for (int i=0;i<twoDArray.length ; i++) {
            double[] values = twoDArray[i];
            JsonArray subArray = array();
            for (int j = 0; j < values.length; j++) {
                subArray.add(primitive(values[j]));
            }
            result.add(subArray);
        }
        return result;
    }
    
    /**
     * Convert json array of arrays of double primitives to a double[][].
     * @param arrayOfArrays
     * @return a double[][]
     */
    public static double[][] convert2DDoubleArray(JsonArray arrayOfArrays) {
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
