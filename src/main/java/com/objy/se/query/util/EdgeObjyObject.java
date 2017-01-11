package com.objy.se.query.util;

import com.objectivity.definitions.object.ObjyObject;

/**
 * Created by ibrahim on 9/19/16.
 */
public class EdgeObjyObject {
    ObjyObject from;
    ObjyObject to;
    String label;

    public EdgeObjyObject(String label, ObjyObject from, ObjyObject to)
    {
        this.label = label;
        this.from = from;
        this.to = to;
    }

    public String getLebel() { return label;}
    public ObjyObject from() { return from; }
    public ObjyObject to()   { return to;   }
}
