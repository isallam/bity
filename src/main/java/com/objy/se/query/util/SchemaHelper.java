package com.objy.se.query.util;

import java.util.HashMap;

/**
 * Created by ibrahim on 9/22/16.
 */
public class SchemaHelper {

    private static String initClassName = "Block";
    
    public static long blockClassNumber = 0;
    public static long transactionClassNumber = 0;
    public static long addressClassNumber = 0;
    public static long inputClassNumber = 0;
    public static long outputClassNumber = 0;
    public static long tagClassNumber = 0;

    // Map class name to the label to send to the GUI.
    public static HashMap<String, String> labelMapper = new HashMap<String, String>();
    public static String[] classNames = {
            "Block",
            "Transaction",
            "Address",
            "Input",
            "Output",
            "Tag"
    };

    static {
        labelMapper.put("Block", "Block");
        labelMapper.put("Transaction", "Transaction");
        labelMapper.put("Address", "Address");
        labelMapper.put("Input", "Input");
        labelMapper.put("Output", "Output");
        labelMapper.put("Tag", "Tag");
    }

    public static String getInitClassName() { return initClassName; }
    
    public static void cacheSchemaInfo() {
        // cache some schema info.
        com.objy.data.Class clazz = com.objy.data.Class.lookupClass("Block");
        blockClassNumber = clazz.getClassNumber();
        clazz = com.objy.data.Class.lookupClass("Transaction");
        transactionClassNumber = clazz.getClassNumber();
        clazz = com.objy.data.Class.lookupClass("Address");
        addressClassNumber = clazz.getClassNumber();
        clazz = com.objy.data.Class.lookupClass("Input");
        inputClassNumber = clazz.getClassNumber();
        clazz = com.objy.data.Class.lookupClass("Output");
        outputClassNumber = clazz.getClassNumber();
        clazz = com.objy.data.Class.lookupClass("Tag");
        tagClassNumber = clazz.getClassNumber();
      
    }

}
