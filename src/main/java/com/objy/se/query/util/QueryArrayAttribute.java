package com.objy.se.query.util;

/**
 * Created by ibrahim on 10/11/16.
 */
public class QueryArrayAttribute {
    int numElements = 0;
    public QueryArrayAttribute(int numElements) {
        this.numElements = numElements;
    }
    public void setNumElements(int val) {
        numElements = val;
    }
    public int getNumElements() {
        return numElements;
    }
}
