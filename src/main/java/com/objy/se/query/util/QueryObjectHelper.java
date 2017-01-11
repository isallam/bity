package com.objy.se.query.util;

import com.objectivity.backend.helper.ObjectHelper;
import com.objectivity.backend.helper.ObjectivityHelper;
import com.objectivity.definitions.object.ArrayAttribute;
import com.objy.data.*;

/**
 * Created by ibrahim on 10/6/16.
 */
public class QueryObjectHelper extends ObjectHelper {

    public static Object getTransient(DataSpecification spec, Variable persistentValue) throws Exception {
        LogicalType type = spec.getLogicalType();
        Object tObj = null;
        if(persistentValue != null && !persistentValue.isNullValue()) {
            if(type == LogicalType.BOOLEAN) {
                tObj = getBoolean(persistentValue);
            } else if(type == LogicalType.CHARACTER) {
                tObj = getCharacter(persistentValue);
            } else if(type == LogicalType.DATE) {
                tObj = getDate(persistentValue);
            } else if(type == LogicalType.DATE_TIME) {
                tObj = getDateTime(persistentValue);
            } else if(type == LogicalType.TIME) {
                tObj = getTime(persistentValue);
            } else if(type == LogicalType.DATE_TIME_OFFSET) {
                tObj = getDateTimeOffset(persistentValue);
            } else if(type == LogicalType.INTERVAL) {
                tObj = getInterval(persistentValue);
            } else if(type == LogicalType.GUID) {
                tObj = getGuid(persistentValue);
            } else if(type == LogicalType.INTEGER) {
                boolean needBigInt = false;
                if(spec.getEncoding() == Encoding.Integer.UNSIGNED.getValue() && spec.getStorage() == com.objy.data.Storage.Integer.B64.getValue()) {
                    needBigInt = true;
                }

                tObj = getInteger(persistentValue, needBigInt);
            } else if(type == LogicalType.REAL && spec.getStorage() == Storage.Real.B32.getValue()) {
                tObj = getFloat(persistentValue);
            } else if(type == LogicalType.REAL && spec.getStorage() == Storage.Real.B64.getValue()) {
                tObj = getDouble(persistentValue);
            } else if(type == LogicalType.STRING) {
                tObj = getString(persistentValue);
            } else if(type == LogicalType.SET) {
                tObj = getSet(spec, persistentValue);
            } else if(type == LogicalType.LIST) {
                tObj = getList(persistentValue); // we are using local getLis() impl.
            } else if(type == LogicalType.MAP) {
                tObj = getMap(spec, persistentValue);
            } else if(type == LogicalType.SEQUENCE) {
                tObj = getSequence(spec, persistentValue);
            } else if(type == LogicalType.INSTANCE) {
                tObj = getInstance(spec, persistentValue);
            } else if(type == LogicalType.REFERENCE) {
                tObj = getReference(persistentValue);
            }
        } else {
            tObj = null;
        }

        return tObj;
    }

    public static String getReference(Variable var) {
        //System.out.println("QueryObjectHelper.getReference()");
        return var.referenceValue().getObjectId().toString();
        //return ObjectHelper.getReference(var);
    }
    public static QueryArrayAttribute getList(Variable var) {
        //System.out.println("QueryObjectHelper.getList()");
        List persistentList = var.listValue();
        int numElements = persistentList.size();
        return new QueryArrayAttribute(numElements);
    }
    public static Boolean getBoolean(Variable var) {
      Boolean value = var.booleanValue();
      return value;
    }

}
