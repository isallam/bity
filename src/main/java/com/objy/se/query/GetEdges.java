package com.objy.se.query;

import com.objectivity.definitions.object.ObjyObject;
import com.objy.data.*;
import com.objy.se.query.util.EdgeObjyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.objy.db.ObjectId;
import com.objy.db.TransactionMode;
import com.objy.db.TransactionScope;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

public class GetEdges extends QueryInterface {
	final static Logger logger = LoggerFactory.getLogger(GetEdges.class);

	public GetEdges(DatabaseManager manager, QuerySpec querySpec, ArrayBlockingQueue<String> resultQueue) {
		super(manager, querySpec, resultQueue);
	}

	public void runQuery() {
		System.out.println("GetEdges.runQuery()");
		manager.open();	// make sure we have a connection
		try (TransactionScope trxScope = new TransactionScope(TransactionMode.READ_ONLY)) 
		{
			this.setStatus(1);
			System.out.println("querySpec: " + querySpec.queryStatement.toString());
			System.out.println("... ObjRef: " + querySpec.getObjRef());
			System.out.println("... MaxResults: " + querySpec.maxResult);

			//ObjectId vObjectId = ObjectivityHelper.uLongStringToObjectId(String.valueOf(querySpec.getObjRef()));
			ObjectId vObjectId = ObjectId.fromString(querySpec.getObjRef());
			Instance vertex = Instance.lookup(vObjectId);
			
			com.objy.data.Class clazz = vertex.getClass(true);
			System.out.println("...processing object of type: " + clazz.getName());
			
			processNeighbors(vertex);

			System.out.println("countNode: " + this.countNodeStat);
			System.out.println("countEdge: " + this.countEdgeStat);
			System.out.println("AlreadySentEdgesCount: " + this.alreadySentEdgesCountStat);

			trxScope.complete();
		} catch (Exception e) {
			this.setStatus(-1);
			e.printStackTrace();
		}
	}

	public void completeStat() {
		this.stat.addProperty("query", "find_vertex");
		this.stat.addProperty("doStatement", querySpec.getDoStatement());
		this.stat.addProperty("maxResult", querySpec.maxResult);
	}
}
