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
import com.objy.data.dataSpecificationBuilder.ReferenceSpecificationBuilder;
import com.objy.data.schemaProvider.SchemaProvider;
import com.objy.db.ObjectId;
import com.objy.db.ObjectivityException;
import com.objy.db.Transaction;


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
        // make sure we have the schema.
	}

	public void runQuery() {
		System.out.println("ExecuteDOUdpate.runQuery()");
		manager.open();	// make sure we have a connection
		try {
            Transaction trx = new Transaction(TransactionMode.READ_UPDATE);
            String tagOid = null;
            
			this.setStatus(1);
					System.out.println("querySpec: " + querySpec.queryStatement.toString());
			System.out.println("... DO: " + querySpec.getDoStatement());
			Statement doStatement = new Statement(LanguageRegistry.lookupLanguage("DO"),
					querySpec.getDoStatement());
            Variable resultVar = doStatement.execute();
            LogicalType resultVarType = resultVar.getSpecification().getLogicalType();
            System.out.println(resultVarType.toString());
            if (resultVarType.equals(LogicalType.INSTANCE)) {
                Instance instance = resultVar.instanceValue();

                // do the inverse connection
                tagOid = instance.getObjectId().toString();
                System.out.println("connection OID: " +  tagOid + 
                        " ... to OID: " + querySpec.getObjRef());
                Reference taggedRef = new Reference(ObjectId.fromString(querySpec.getObjRef()));
                Variable attrVar = new Variable();
                Variable attr =  taggedRef.getReferencedObject().getAttributeValue("m_Tag");
                attr.set(new Reference(instance.getObjectId()));
            }
            
            trx.commit();
            
            if (tagOid != null)
            {
              // fetch tag and send it to GUI.
              trx.start(TransactionMode.READ_ONLY);
              Reference tagObjRef = new Reference(ObjectId.fromString(tagOid));
              processNeighbors(tagObjRef.getReferencedObject());

              System.out.println("countNode: " + this.countNodeStat);
              System.out.println("countEdge: " + this.countEdgeStat);
              System.out.println("AlreadySentEdgesCount: " + this.alreadySentEdgesCountStat);
              
              trx.commit();
            }

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
    
//    /**
//     * create schema for the tag class if it doesn't exist.
//     */
//    public static void createSchema() {
//      try (TransactionScope tx = new TransactionScope(TransactionMode.READ_UPDATE))
//      {
//        // Object Reference
//        DataSpecification objectRefSpec
//                = new ReferenceSpecificationBuilder() 
//                    .setReferencedClass("ooObj")
//                    .build();
//        com.objy.data.Class tagClassRep = new ClassBuilder("Tag")
//                  .setSuperclass("ooObj")
//                  .addAttribute(LogicalType.STRING, "m_Label")
//                  .addAttribute("m_Ref", objectRefSpec)
//                  .build();
//        
//        SchemaProvider provider = SchemaProvider.getDefaultPersistentProvider();
//        provider.represent(tagClassRep);
//
//        tx.complete();
//      }
//      catch(ObjectivityException e){
//        e.printStackTrace();
//      }
//    }
}