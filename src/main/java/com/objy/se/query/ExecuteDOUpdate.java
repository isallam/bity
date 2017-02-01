/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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


/**
 *
 * @author ibrahim
 */
public class ExecuteDOUpdate extends QueryInterface {
	private java.util.List<EdgeObjyObject> toProcessEdgeList = new ArrayList<>();
	final static Logger logger = LoggerFactory.getLogger(GetEdges.class);

	public ExecuteDOUpdate(DatabaseManager manager, QuerySpec querySpec,
					 ArrayBlockingQueue<String> resultQueue) {
		super(manager, querySpec, resultQueue);
	}

	public void runQuery() {
		System.out.println("ExecuteDO.runQuery()");
		manager.open();	// make sure we have a connection
		try (TransactionScope trxScope = new TransactionScope(TransactionMode.READ_UPDATE)) {
			this.setStatus(1);
					System.out.println("querySpec: " + querySpec.queryStatement.toString());
			System.out.println("... DO: " + querySpec.getDoStatement());
			Statement doStatement = new Statement(LanguageRegistry.lookupLanguage("DO"),
					querySpec.getDoStatement());

            trxScope.complete();
		} catch(Exception e){
			this.setStatus(-1);
			e.printStackTrace();
		}
    }
	public void completeStat() {
		this.stat.addProperty("query", "DOQuery");
		this.stat.addProperty("doStatement", querySpec.getDoStatement());
		this.stat.addProperty("maxResult", querySpec.maxResult);
	}
}