package com.objy.se.query;

import com.objectivity.definitions.object.ObjyObject;
import com.objy.data.*;
import com.objy.policy.Policies;
import com.objy.se.query.util.EdgeObjyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.objy.db.TransactionMode;
import com.objy.db.TransactionScope;
import com.objy.expression.language.LanguageRegistry;
import com.objy.statement.Statement;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

import com.google.gson.JsonObject;


public class ExecuteDO extends QueryInterface {
	private java.util.List<EdgeObjyObject> toProcessEdgeList = new ArrayList<>();
	final static Logger logger = LoggerFactory.getLogger(GetEdges.class);

	public ExecuteDO(DatabaseManager manager, QuerySpec querySpec,
					 ArrayBlockingQueue<String> resultQueue) {
		super(manager, querySpec, resultQueue);
	}

	public void runQuery() {
		System.out.println("ExecuteDO.runQuery()");
		manager.open();	// make sure we have a connection
		try (TransactionScope trxScope = new TransactionScope(TransactionMode.READ_ONLY)) {
			this.setStatus(1);
					System.out.println("querySpec: " + querySpec.queryStatement.toString());
			System.out.println("... DO: " + querySpec.getDoStatement());
			Statement doStatement = new Statement(LanguageRegistry.lookupLanguage("DO"),
					querySpec.getDoStatement());

			// Add the identifier to the results projection.
			Policies policies = new Policies();
			policies.add("AddIdentifier.enable", new Variable(true));

			Variable results = doStatement.execute(policies);

			System.out.println(results.getSpecification().getLogicalType().toString());
			if (results.getSpecification().getLogicalType() == LogicalType.SEQUENCE) {
				// we got sequence of results.
				Iterator<Variable> resultItr = results.sequenceValue().iterator();
				int numResults = 0;

				Variable resultVar = null;
				LogicalType resultVarType = null;
				HashMap<String, Integer> sentNodeData = new HashMap<String, Integer>();
				HashMap<String, ObjyObject> cachedObjyObjects = new HashMap<String, ObjyObject>();
				int count = 0;
				while (resultItr.hasNext() && numResults < querySpec.maxResult) {
					resultVar = resultItr.next();
					resultVarType = resultVar.getSpecification().getLogicalType();
					//System.out.println("... Node type: " + resultVarType.name());
					if (resultVarType.equals(LogicalType.INSTANCE)) {
						Instance instance = resultVar.instanceValue();

						ObjyObject objyObject = Utils.handleInstance(instance, cachedObjyObjects);

						// dump the results attributes...
						//System.out.println("... attributes: " + objyObject.attributes.toString());
						if (objyObject.attributes.containsKey(Utils.PATH_ATTRIBUTE))
						{
							processPathResults(objyObject, sentNodeData, querySpec.verbose);
							count++;
						}
						else {
							if (!sentNodeData.containsKey((String)objyObject.oid))
							{
								sentNodeData.put(objyObject.oid, 1);
								JsonObject json = new JsonObject();
								json.addProperty("context", querySpec.getContext());
								JsonObject nodeElement = getNodeData(objyObject, querySpec.verbose);
								json.add("node", nodeElement);
								this.addResult(json.toString());
							}
						}
						numResults++;
						this.setStatus(2);
					}
				}
				if (toProcessEdgeList.size() > 0)  // we have some to process.
				{
					processEdgesAsOneJson(toProcessEdgeList, sentNodeData);
					toProcessEdgeList.clear();
				}

				System.out.println("PathAttribute count: " + count);
				System.out.println("countNode: " + this.countNodeStat);
				System.out.println("countEdge: " + this.countEdgeStat);
				System.out.println("AlreadySentEdgesCount: " + this.alreadySentEdgesCountStat);
			}
			else
				{
					System.out.println("... GOT one node: " + results.instanceValue().getObjectId().toString());
				}
				trxScope.complete();
		} catch(Exception e){
			this.setStatus(-1);
			e.printStackTrace();
		}
	}

	/***
	 *
	 * @param objyObject
	 * @param verbose
	 */
	private void processPathResults(ObjyObject objyObject, HashMap<String, Integer> sentNodeData, int verbose) {

		//System.out.println(" >>> processsPathResults()");
		java.util.List<EdgeObjyObject> edgeList =
				(java.util.List<EdgeObjyObject>) objyObject.attributes.get(Utils.PATH_ATTRIBUTE);
		toProcessEdgeList.addAll(edgeList);
		if (toProcessEdgeList.size() > 100) {
			processEdgesAsOneJson(toProcessEdgeList, sentNodeData);
			toProcessEdgeList.clear();
		}
	}



	public void completeStat() {
		this.stat.addProperty("query", "DOQuery");
		this.stat.addProperty("doStatement", querySpec.getDoStatement());
		this.stat.addProperty("maxResult", querySpec.maxResult);
	}
}
