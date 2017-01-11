package com.objy.se.query;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuerySpec {
	final static Logger logger = LoggerFactory.getLogger(QuerySpec.class);

	public JsonObject queryStatement = null;
	
	public String type = null;

	public int originLimit = 10;

	private String objRef = null;
	private String objId1 = null;
	private String objId2 = null;

	public int verbose = 1;

	private String doStatement;

	public int maxResult = -1;

	public String getContext() {
		return context;
	}

	private String context = null;


	public QuerySpec(JsonObject request) {
		this.queryStatement = request;
	}
	
	public String getDoStatement() {
		return this.doStatement;	
	}

	public boolean setup(int verboseLevel) {
		boolean status = true;

		if (this.queryStatement.has("qType") == false) {
			logger.error(
					"Query message does not contain query qType parameter {}",
					this.queryStatement.toString());
			status = false;
		} else {
			this.type = (String) this.queryStatement.getAsJsonPrimitive("qType").getAsString();
		}

		if (status & this.queryStatement.has("qContext") == false) {
			logger.error(
					"Query message does not contain query qContext parameter {}",
					this.queryStatement.toString());
			status = false;
		} else {
			this.context = (String) this.queryStatement.getAsJsonPrimitive("qContext").getAsString();
		}

		if (status && queryStatement.has("doStatement")) {
			this.doStatement = (String) queryStatement.getAsJsonPrimitive("doStatement").getAsString();
		}

		if (status && queryStatement.has("originLimit")) {
			this.originLimit = queryStatement.getAsJsonPrimitive("originLimit").getAsInt();
		}

		if (status && (verboseLevel > -2)) {
			verbose = verboseLevel;
		} else if (status && queryStatement.has("verbose")) {
			verbose = queryStatement.getAsJsonPrimitive("verbose").getAsInt();
		}

		if (status && queryStatement.has("maxResult")) {
			maxResult = queryStatement.getAsJsonPrimitive("maxResult").getAsInt();
		}
		if (status && queryStatement.has("objRef") == true) {
			this.objRef = queryStatement.getAsJsonPrimitive("objRef").getAsString();
		}
		// objyId is very specific to classes that has m_Id as a string attribute.
		if (status && queryStatement.has("objId1") == true) {
			this.objId1 = queryStatement.getAsJsonPrimitive("objId1").getAsString();
			// we need two ids to do the similarity check.
			if (status && queryStatement.has("objId2") == true) {
				this.objId2 = queryStatement.getAsJsonPrimitive("objId2").getAsString();
			}
		}

		return status;
	}

	public String getObjRef() {
		return objRef;
	}
	public String getObjId1() {
		return objId1;
	}
	public String getObjId2() {
		return objId2;
	}

//	private void setObjRef(String objRef) {
//		this.objRef = objRef;
//	}
}
