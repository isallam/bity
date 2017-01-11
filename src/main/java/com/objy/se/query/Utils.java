package com.objy.se.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.objectivity.backend.helper.ObjectivityHelper;
import com.objectivity.definitions.object.ObjyObject;
import com.objy.data.*;
import com.objy.data.Class;
import com.objy.db.ObjectId;
import com.objy.se.query.util.EdgeObjyObject;
import com.objy.se.query.util.QueryObjectHelper;

import static com.objectivity.backend.impl.QueryImpl.OID_ATTR_NAME;

public class Utils {

	public static final String PATH_ATTRIBUTE = "__path__";

	public static boolean isInternalClass(String className) {
		boolean retValue = (className.compareTo("financial.TimeSegment") == 0);
		return retValue;
	}

	/**
	 *
	 * @param inst
	 * @return
	 * @throws Exception
	 */
	public static ObjyObject handleInstance(Instance inst,
											HashMap<String, ObjyObject> cachedObjyObject) throws Exception
	{
		//treat all DO projections as if they were objects
		ObjyObject obj = new ObjyObject();
		obj.attributes = new HashMap<>();
		Class objClass = inst.getClass(true);

		// check if the instance is a walk first.
		//iterate over each attribute
		Iterator<Variable> attrItr = objClass.getAttributes().iterator();
		while (attrItr.hasNext())
		{
			//use attribute value from schema to get attribute name
			Attribute attr = attrItr.next().attributeValue();
			String attrName = attr.getName().trim();
			// TBD... a hack until we get the LogicalType == WALK to work correctly
			String pathAttrib = "p";
			if (attrName.equals(pathAttrib)) // this is a path
			{
				Variable walkVar = inst.getAttributeValue(pathAttrib);
				Walk walk = walkVar.walkValue();
				java.util.List<EdgeObjyObject> edgeList = getEdges(walk.edges(), -1 /*all edges */, cachedObjyObject);
				obj.attributes.put(PATH_ATTRIBUTE, edgeList);
				obj._class = "WALK";
				//System.out.println("... this is a WALK");

			} else {
				if (!attrName.equals(OID_ATTR_NAME)) {
//					System.out.println("(A)... AttrName: " + attrName);
					java.lang.Object val = QueryObjectHelper.getTransient(attr.getAttributeValueSpecification(),
							inst.getAttributeValue(attrName));
					obj.attributes.put(attrName, val);
				}
				else //handle OID_ATTR_NAME by adding it as an oid on the base object
				{
					String oidAsString = inst.getAttributeValue(OID_ATTR_NAME).stringValue();
					//System.out.println("(B)... oidAsString: " + oidAsString);
					obj.oid = oidAsString;
				}
			}
		}
		//no OID_ATTR_NAME which means returned Instance is an Object not a projection
		if (obj.oid == null || obj.oid.isEmpty())
		{
			Variable v = inst.getIdentifier();
			//obj.oid = getTransient(v.getSpecification(), v).toString();
			ObjectId objectId = ObjectivityHelper.uLongStringToObjectId(QueryObjectHelper.getTransient(v.getSpecification(), v).toString());
			obj.oid = objectId.toString();
//			System.out.println("...\t... obj.oid is null, setting oid to: " + obj.oid);
		}
		if (obj._class == null || obj._class.isEmpty())
		{
			if (!obj.oid.equals("")) {
				// better get the class of the actual object, getting the projection name is useless.
				//System.out.println("getting class for OID: " + obj.oid);
				Instance objInst = Instance.lookup(ObjectId.fromString(obj.oid));
				obj._class = objInst.getClass(true).getName();
				//System.out.println("...\t... obj._class: " + obj._class);
			}
		}

		return obj;
	}

	public static java.util.List<EdgeObjyObject> getEdges(Sequence edgeSequence, int maxResults,
														  HashMap<String, ObjyObject> cachedObjyObjects) {
		Iterator<Variable> edgeItr = edgeSequence.iterator();
		Edge edge = null;
		int count = 0;
		java.util.List<EdgeObjyObject> edgeArray = new ArrayList<EdgeObjyObject>();
		try {
			while (edgeItr.hasNext()) {
				edge = edgeItr.next().edgeValue();
				String fromOid = edge.from().getObjectId().toString();
				ObjyObject objFrom = cachedObjyObjects.get(fromOid);
				if (objFrom == null) {
					objFrom = handleInstance(edge.from(), cachedObjyObjects);
					cachedObjyObjects.put(fromOid, objFrom);
				}
				String toOid = edge.to().getObjectId().toString();
				ObjyObject objTo = cachedObjyObjects.get(toOid);
				if (objTo == null) {
					objTo = handleInstance(edge.to(), cachedObjyObjects);
					cachedObjyObjects.put(toOid, objTo);
				}

				EdgeObjyObject edgeObjy = new EdgeObjyObject(edge.getLabel() ,objFrom, objTo);
				edgeArray.add(edgeObjy);

				count++;

				if (maxResults > 0 && (count >= maxResults))
					break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return edgeArray;
	}


}
