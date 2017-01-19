package com.objy.se.query;

import com.objy.data.Instance;
import com.objy.data.Variable;
import com.objy.db.ObjectId;
import com.objy.db.Transaction;
import com.objy.db.TransactionMode;
import com.objy.db.TransactionScope;
import com.objy.se.query.util.SimilarityHelper;

import com.google.gson.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Similarity extends QueryInterface 
{

	final static Logger logger = LoggerFactory.getLogger(Similarity.class);

	public Similarity(DatabaseManager manager, QuerySpec querySpec, ArrayBlockingQueue<String> resultQueue) {
		super(manager, querySpec, resultQueue);
	}


	public void runQuery() {
		System.out.println("GetEdges.runQuery()");
		
		manager.open();	// make sure we have a connection
		try (TransactionScope trxScope = new TransactionScope(TransactionMode.READ_ONLY))
		{
			long sTime = System.nanoTime();
			this.setStatus(1);
			System.out.println("querySpec: " + querySpec.queryStatement.toString());
			System.out.println("... ObjId1: " + querySpec.getObjId1());
			System.out.println("... ObjId2: " + querySpec.getObjId2());
			System.out.println("... MaxResults: " + querySpec.maxResult);

			if ((querySpec.getObjId1() == null)|| (querySpec.getObjId2() == null))
			{
				String output = "{\"context\":\"" + querySpec.getContext() + "\",\"status\":" +
						"\" Error processing similarity... null ObjId\"}";
				this.addResult(output);
			}
			else {
				//
				HashMap<Long, Integer> refGraph = SimilarityHelper.getGraphForVertex(querySpec.getObjId1());
				HashMap<Long, Integer> targetGraph = SimilarityHelper.getGraphForVertex(querySpec.getObjId2());
				String simString = "[" + querySpec.getObjId1() + "]<=>[" + querySpec.getObjId2() + "]: ";

				if ((refGraph.size() > 0 ) && (targetGraph.size() > 0)) {
					// run the similarity check
					// doSimilarity check.
					double simpleSimilarity = SimilarityHelper.getSimpleSimilarityIndex(refGraph, targetGraph);
					//double similarity = SimilarityHelper.getSimilarityIndex(refGraph, targetGraph);

					long delay = (System.nanoTime() - sTime);
					long delayInMillisec = TimeUnit.NANOSECONDS.toMillis(delay);

					NumberFormat numFormat = NumberFormat.getPercentInstance();
					numFormat.setMaximumFractionDigits(2);
					Similarity.logger.info("... Context: {}", querySpec.getContext());
					//Similarity.logger.info("Similarity Index: {}", numFormat.format(similarity));
					Similarity.logger.info("Similarity Index: {}", numFormat.format(simpleSimilarity));
					Similarity.logger.info("Time: {} msec", delayInMillisec );

					StringBuffer sbOutput = new StringBuffer();
					sbOutput.append("{\"context\":\"");
					sbOutput.append(querySpec.getContext());
					sbOutput.append("\",\"status\":\"");
					sbOutput.append(simString);
					sbOutput.append(" Sim Index: ");
					sbOutput.append(numFormat.format(simpleSimilarity));
//					sbOutput.append(", SimpleSim: ");
//					sbOutput.append(numFormat.format(simpleSimilarity));
					sbOutput.append(", time (msec): ");
					sbOutput.append(delayInMillisec);
					sbOutput.append("\"}");

//					String output = "{\"context\":\"" + querySpec.getContext() + "\",\"status\":" +
//							"\"Simple Similarity Index "+ simString + simpleSimilarity + "\"}";
//					this.addResult(output);
//					output = "{\"context\":\"" + querySpec.getContext() + "\",\"status\":" +
//							"\"Similarity Index " + simString  + similarity + "\"}";
//					this.addResult(output);
					this.addResult(sbOutput.toString());
				} else {
					// report error.
					String output = "{\"context\":\"" + querySpec.getContext() + "\",\"status\":" +
							"\"Error doing similarity " + simString + " - perhaps invalid IDs.\"}";
					this.addResult(output);
				}

			}
			trxScope.complete();
		} catch (Exception e) {
			this.setStatus(-1);
			e.printStackTrace();
		}
	}

	public JsonObject getCurrentResult() {
		return null;
	}

	@Override
	public void completeStat() {
		// TODO Auto-generated method stub
		
	}
}

