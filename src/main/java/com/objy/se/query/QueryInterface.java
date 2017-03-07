package com.objy.se.query;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.objectivity.definitions.object.ObjyObject;
import com.objy.se.query.util.EdgeObjyObject;
import com.objy.se.query.util.SchemaHelper;
import com.objy.se.query.util.QueryArrayAttribute;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.objy.data.Instance;
import com.objy.data.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class QueryInterface {
	final static Logger logger = LoggerFactory.getLogger(QueryInterface.class);
	public static final String EOP = "EOP";
	protected int status = 0;
	protected String statusMessage = null;
	protected QuerySpec querySpec = null;
	protected DatabaseManager manager = null;
	protected ArrayBlockingQueue<String> result = null;
	protected JsonObject stat = new JsonObject();
	private long startTime = 0;
	private long timeDelay = 0;

	protected long countNodeStat = 0;
	protected long countEdgeStat = 0;
	protected long alreadySentEdgesCountStat = 0;
	protected HashMap<String, Integer> alreadySentEdges = new HashMap<>();
    
    protected Utils utils = new Utils();


	protected QueryInterface(DatabaseManager manager, QuerySpec querySpec,
			ArrayBlockingQueue<String> resultQueue) {
		this.manager = manager;
		this.querySpec = querySpec;
		this.result = resultQueue;
		this.setStatus(0);
	}

	public void setStatus(int value) {
		status = value;
	}

	public int getStatus() {
		return this.status;
	}

	public void addResult(String object) {
		try {
			this.result.put(object);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void processEdges(java.util.List<EdgeObjyObject> edgeList,
								HashMap<String, Integer> sentNodeData) {
		try {
			Iterator<EdgeObjyObject> edgeObjItr = edgeList.iterator();
			while (edgeObjItr.hasNext()) {
				// get the first edge.
				EdgeObjyObject edgeObj = edgeObjItr.next();
				ObjyObject fromObj = edgeObj.from();
				ObjyObject toObj = edgeObj.to();
				//System.out.println("\t fromClass; " + fromObj._class);
				if (!sentNodeData.containsKey(fromObj.oid))
				{
					sentNodeData.put(fromObj.oid, 1);
					// produce a node with empty edges, we'll add the edge with the destination object.
					JsonObject json = new JsonObject();
					//String output = "{\"context\":\"" + querySpec.getContext() + "\",\"node\":";
					json.addProperty("context", querySpec.getContext());
					JsonObject jsonNode = getNodeData(fromObj, querySpec.verbose);
					//output += getNodeData(fromObj, querySpec.verbose);
					//output += "}";
					json.add("node", jsonNode);
					this.addResult(json.toString());
					this.countNodeStat++;
				}

				//System.out.println("\t toClass; " + toObj._class);
				//StringBuilder sbOutput = getNodeData(fromObj, toObj, querySpec.verbose);
				JsonObject jsonEdge = getNodeData(edgeObj, sentNodeData, querySpec.verbose);
				this.addResult(jsonEdge.toString());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void processEdgesAsOneJson(java.util.List<EdgeObjyObject> edgeList,
										 HashMap<String, Integer> sentNodeData) {
		try {
			Iterator<EdgeObjyObject> edgeObjItr = edgeList.iterator();
			JsonObject json = new JsonObject();
			json.addProperty("context", querySpec.getContext());

			JsonArray jsonNodeArray = new JsonArray();
			JsonArray jsonEdgeArray = new JsonArray();

			while (edgeObjItr.hasNext()) {
				// get the first edge.
				EdgeObjyObject edgeObj = edgeObjItr.next();
				ObjyObject fromObj = edgeObj.from();
				ObjyObject toObj = edgeObj.to();
				//System.out.println("\t fromClass; " + fromObj._class);
				if (!sentNodeData.containsKey(fromObj.oid))
				{
					sentNodeData.put(fromObj.oid, 1);
					// produce a node with empty edges, we'll add the edge with the destination object.
					//String output = "{\"context\":\"" + querySpec.getContext() + "\",\"node\":";
					JsonObject jsonNode = getNodeData(fromObj, querySpec.verbose);
					//output += getNodeData(fromObj, querySpec.verbose);
					//output += "}";
//					this.addResult(json.toString());
					jsonNodeArray.add(jsonNode);
					this.countNodeStat++;
				}
				if (!sentNodeData.containsKey(toObj.oid))
				{
					sentNodeData.put(toObj.oid, 1);
					JsonObject jsonNode = getNodeData(toObj, querySpec.verbose);
					jsonNodeArray.add(jsonNode);
					this.countNodeStat++;
				}

				//JsonObject jsonEdge = getNodeData(fromObj, toObj, sentNodeData, querySpec.verbose);
				//this.addResult(jsonEdge.toString());
				String edgeId = fromObj.oid + "|" + toObj.oid;
				if (!alreadySentEdges.containsKey(edgeId))
				{
					alreadySentEdges.put(edgeId, 1);
					JsonObject jsonEdge = new JsonObject();
					jsonEdge.addProperty("id", edgeId);
					jsonEdge.addProperty("source", fromObj.oid);
					jsonEdge.addProperty("target", toObj.oid);
                    jsonEdge.addProperty("label", edgeObj.getLebel());
					jsonEdgeArray.add(jsonEdge);
					this.countEdgeStat++;
				}
			}
			json.add("nodes", jsonNodeArray);
			json.add("edges", jsonEdgeArray);
			this.addResult(json.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected JsonObject getNodeData(ObjyObject node, int level) {
		JsonObject jsonNode = new JsonObject();

		if (node == null)
			return jsonNode;

		jsonNode.addProperty("id", node.oid);
		jsonNode.addProperty("label", SchemaHelper.labelMapper.get(node._class));
		JsonObject jsonNodeData = new JsonObject();
		boolean firstEntry = true;
		for(java.util.Map.Entry<String, Object> mapEntry : node.attributes.entrySet())
		{
			if (mapEntry.getValue() instanceof QueryArrayAttribute) {
				int numElements = ((QueryArrayAttribute) mapEntry.getValue()).getNumElements();
				jsonNodeData.addProperty(mapEntry.getKey(), numElements);
			} else {
                if (mapEntry.getValue() instanceof String)
    				jsonNodeData.addProperty(mapEntry.getKey(), mapEntry.getValue().toString());
                else if (mapEntry.getValue() instanceof Boolean) 
    				jsonNodeData.addProperty(mapEntry.getKey(), (Boolean) mapEntry.getValue());
                else if (mapEntry.getValue() instanceof Number)
    				jsonNodeData.addProperty(mapEntry.getKey(), (Number) mapEntry.getValue());
            }
		}
		jsonNode.add("data", jsonNodeData);
		return jsonNode;
	}

	protected JsonObject getNodeData(EdgeObjyObject edgeObj, HashMap<String, Integer> sentNodesData,
									 int level) {
		JsonObject json = new JsonObject();
        ObjyObject fromObj = edgeObj.from();
        ObjyObject toObj = edgeObj.to();

		json.addProperty("context", querySpec.getContext());
		if (!sentNodesData.containsKey(toObj.oid)) {
			sentNodesData.put(toObj.oid, 1);
			json.add("node", getNodeData(toObj, querySpec.verbose));
			this.countNodeStat++;
		}
		// add the edge
		String edgeId = fromObj.oid + "|" + toObj.oid;
		if (!alreadySentEdges.containsKey(edgeId))
		{
			alreadySentEdges.put(edgeId, 1);
			JsonObject jsonEdge = new JsonObject();
			jsonEdge.addProperty("id", edgeId);
			jsonEdge.addProperty("source", fromObj.oid);
			jsonEdge.addProperty("target", toObj.oid);
            jsonEdge.addProperty("label", edgeObj.getLebel());
			json.add("edge", jsonEdge);
			this.countEdgeStat++;
		}
		else {
			alreadySentEdgesCountStat++;
		}
		return json;
	}

	protected void processNeighbors(Instance instance) {
		ObjyObject obj = new ObjyObject();
		obj.attributes = new HashMap<>();
		HashMap<String, Integer> knownNodeOids = new HashMap<String, Integer>();

		com.objy.data.Class targetClass = com.objy.data.Class.lookupClass("ooObj");
		//System.out.println("... found class: " + targetClass.getName());
		HashMap<String, ObjyObject> cachedObjyObjects = new HashMap<String, ObjyObject>();
		if (instance != null) {
			Sequence edgeSequence = instance.getEdges(targetClass);
			java.util.List<EdgeObjyObject> edgeList = utils.getEdges(edgeSequence, querySpec.maxResult);
			processEdgesAsOneJson(edgeList, knownNodeOids);
		}
	}


	private void startTimer() {
		this.startTime = System.nanoTime();
	}

	private void stopTimer() {
		this.timeDelay = (System.nanoTime() - this.startTime);
	}

	protected long getTimerInMillisec() {
		long delayInMills = TimeUnit.NANOSECONDS.toMillis(this.timeDelay);
		return delayInMills;
	}

	public abstract void completeStat();
	public abstract void runQuery();

	public void run() {
		try {
			this.startTimer();
			this.runQuery();
			this.stopTimer();

			this.completeStat();

			long timeInMs = getTimerInMillisec();
			String output = "{\"context\":\"" + querySpec.getContext() + "\",\"status\":" +
					"\"Query Time: " + timeInMs + " msec.\"}";
			this.addResult(output);

		} catch(Exception e){
			this.setStatus(-1);
			e.printStackTrace();
		} finally{
			this.addResult(querySpec.getContext() + "," + QueryInterface.EOP);
			System.out.println(">>>>>>");
			System.out.println(">>>>>> Done query on thread: "
					+ Thread.currentThread().getId());
			System.out.println(">>>>>>");
		}
	}

	static java.text.MessageFormat resultFormat = new java.text.MessageFormat(
			"'{'\"stat\":{0},\"result\":{1}'}'");
	static java.text.MessageFormat statFormat = new java.text.MessageFormat(
			"'{'\"stat\":{0}'}'");

}
