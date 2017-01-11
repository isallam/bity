package com.objy.se.query.util;

import java.util.HashMap;

/**
 * Created by ibrahim on 9/22/16.
 */
public class SchemaHelper {

    private static String initClassName = "Block";

    // Map class name to the label to send to the GUI.
    public static HashMap<String, String> labelMapper = new HashMap<String, String>();
    public static String[] classNames = {
            "Block",
            "Transaction",
            "Address",
            "Input",
            "Output"
    };

    static {
        labelMapper.put("Block", "Block");
        labelMapper.put("Transaction", "Transaction");
        labelMapper.put("Address", "Address");
        labelMapper.put("Input", "Input");
        labelMapper.put("Output", "Output");
    }

    public static String getInitClassName() { return initClassName; }

}
