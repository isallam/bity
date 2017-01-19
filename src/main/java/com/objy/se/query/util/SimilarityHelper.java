package com.objy.se.query.util;

import com.objy.data.*;
import com.objy.db.ObjectId;
import com.objy.expression.language.LanguageRegistry;
import com.objy.statement.Statement;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ibrahim on 1/18/17.
 * based on the other version in "finy"
 */
public class SimilarityHelper {

    static char transTypeOrder = 'D';
    static char transTypeFill = '8';
    static char transTypeCancel = 'F';
    static char transTypeCancelReplace = 'G';

    static long transOrder = 1;
    static long transFill = 2;
    static long transCancel = 3;
    static long transCancelReplace = 4;

    final static Logger logger = LoggerFactory.getLogger(SimilarityHelper.class);

    public static HashMap<Long, Integer> getGraphForVertex_DO_version(String id)
    {
        HashMap<Long, Integer> subGraph = new HashMap<Long, Integer>();
//		String queryString = "Match p=(:Basket{m_Id=='" + basketId + "'})" +
//					"-[:m_Transactions]->" +
//					"((:Transaction)-[:m_Children]->(:Transaction))*1..2" +
//					"-[:m_Security]->(:Security) return p";
        String queryString1 = 
                "Match p=(:Transaction{$$THIS_REFERENCE==" + id + "})-->() return p";

//		String queryString2 = "Match p=(:Basket{m_Id=='" + basketId + "'})" +
//				"-[:m_Transactions]->" +
//				"(:Transaction)-[:m_Children*1..5]->(:Transaction{m_Type=='F'})" +
//				"-[:m_Security]->(:Security) return p";

        String queryString = queryString1;

        Statement doStatement = new Statement(LanguageRegistry.lookupLanguage("DO"),
                queryString);

        Variable results = doStatement.execute();
        System.out.println(results.getSpecification().getLogicalType().toString());
        if (results.getSpecification().getLogicalType() == LogicalType.SEQUENCE)
        {
            // we got sequence of results.
            Iterator<Variable> resultItr = results.sequenceValue().iterator();
            int numResults = 0;
            //int numWalks = 0;

            HashMap<Long, Integer> uniqueVertices = new HashMap<Long, Integer>();

            /***
             * The first algorithm will look at the basekt, securities and the type of
             * transactions, we'll count how many of each type and add it to the subgraph
             * collection.
             */
            Variable resultVar = null;
            while (resultItr.hasNext())
            {
                resultVar = resultItr.next();
                // we have a list of projection instances.
                //System.out.println("... logicalType: " + resultVar.getSpecification().getLogicalType().toString());
                Instance instance = resultVar.instanceValue();
                //com.objy.data.Class clazz = instance.getClass(true);
                //System.out.println("...... class:" + clazz.getName());
                Variable walkVar = instance.getAttributeValue("p");
                Walk walk = walkVar.walkValue();
                //System.out.println("......... Walk length: " + walk.length());
                processWalk(walk, uniqueVertices, subGraph);
                //break;
                numResults++;
//                if ((numResults % 100) == 0)
//                {
//                    Similarity.getLogger().info("... pathFound: {}", numResults);
//                }
            }
            logger.info("for {} - found {} path", id, numResults);
            //Similarity.getLogger().info("SubGraph: " + subGraph.toString());
        }

        // TODO Auto-generated method stub
        return subGraph;
    }

    public static HashMap<Long, Integer> getGraphForVertex(String id)
    {
        HashMap<Long, Integer> subGraph = new HashMap<Long, Integer>();

        ObjectId vObjectId = ObjectId.fromString(id);
		com.objy.data.Class targetClass = com.objy.data.Class.lookupClass("ooObj");
        Instance vertex = Instance.lookup(vObjectId);
		if (vertex != null) {
			Sequence edgeSequence = vertex.getEdges(targetClass);

            int numResults = 0;

            HashMap<Long, Integer> uniqueVertices = new HashMap<Long, Integer>();

            processEdges(edgeSequence, uniqueVertices, subGraph);
            
            numResults++;
            logger.info("for {} - found {} path", id, numResults);
            //Similarity.getLogger().info("SubGraph: " + subGraph.toString());
        }

        // TODO Auto-generated method stub
        return subGraph;
    }

    private static void processEdges(Sequence edgeSequence,
                                HashMap<Long, Integer> uniqueVertices, 
                                HashMap<Long, Integer> subGraph) {
        Iterator<Variable> edgeItr = edgeSequence.iterator();
        Variable edgeVar = null;
        while(edgeItr.hasNext())
        {
            edgeVar = edgeItr.next();
            Edge edge = edgeVar.edgeValue();

//			String from = edge.from().getClass(true).getName() + "^" +
//					edge.from().getObjectId().toString();
//			String to = edge.to().getClass(true).getName() + "^" +
//					edge.to().getObjectId().toString();
//			System.out.println("........edge: ["+ from + "]->["+to+"]");

            processVertex(edge.from(), uniqueVertices, subGraph);
            processVertex(edge.to(), uniqueVertices, subGraph);
        }

    }

    private static void processWalk(Walk walk,
                                    HashMap<Long, Integer> uniqueVertices, HashMap<Long, Integer> subGraph) {
        Iterator<Variable> edgeItr = walk.edges().iterator();
        Variable edgeVar = null;
        while(edgeItr.hasNext())
        {
            edgeVar = edgeItr.next();
            Edge edge = edgeVar.edgeValue();

//			String from = edge.from().getClass(true).getName() + "^" +
//					edge.from().getObjectId().toString();
//			String to = edge.to().getClass(true).getName() + "^" +
//					edge.to().getObjectId().toString();
//			System.out.println("........edge: ["+ from + "]->["+to+"]");

            processVertex(edge.from(), uniqueVertices, subGraph);
            processVertex(edge.to(), uniqueVertices, subGraph);
        }

    }

    private static void processVertex(Instance instance,
                                      HashMap<Long, Integer> uniqueVertices, 
                                      HashMap<Long, Integer> subGraph)
    {
        long objId = instance.getObjectId().asLong();
        if (uniqueVertices.containsKey(objId) == false) {
            uniqueVertices.put(objId, 1);
            long classNumber = instance.getClassNumber();
            
            if ((classNumber == SchemaHelper.inputClassNumber) || 
                    (classNumber == SchemaHelper.outputClassNumber))
            {
                if (subGraph.containsKey(classNumber) == false)
                {
                    subGraph.put(classNumber,  1);
                }
                else
                {
                    subGraph.put(classNumber,  subGraph.get(classNumber) +1);
                }
            }
            else {
                System.out.println("can't process class num:" + classNumber);
            }
        }
        return;
    }


    public static double getSimpleSimilarityIndex(
            HashMap<Long, Integer> refGraph,
            HashMap<Long, Integer> targetGraph) {
        double index = -1;

        int union = simpleUnion(refGraph, targetGraph);
        int intersection = simpleIntersect(refGraph, targetGraph);

        logger.info("simpleUnion: {} -- simpleIntersection: {}", union, intersection);

        index = intersection / (double)union;

        return index;
    }

    private static int simpleIntersect(HashMap<Long, Integer> a,
                                       HashMap<Long, Integer> b) {
      int intersectValue = 0;
      
      if ((a == null) || (b == null))
            return 0;
        if ((a.size() == 0) || (b.size() == 0))
            return 0;
        
        Iterator<java.util.Map.Entry<Long, Integer>> iterator = a.entrySet().iterator();
        while (iterator.hasNext()) {
            java.util.Map.Entry<Long, Integer> pairs = iterator.next();
            Long key = pairs.getKey();
            if (b.containsKey(key) == true) {
                intersectValue += Math.min(pairs.getValue(), b.get(key));
            }
        }
        return intersectValue;
    }

    private static int simpleUnion(HashMap<Long, Integer> a,
                                   HashMap<Long, Integer> b) {
        int unionValue = 0;
        
        if (a == null) {
            if (b == null)
                return 0;
            return b.size();
        }
        if (b == null) {
            return a.size();
        }
        if (a == b)
            return a.size();

        if (a.size() == 0)
            return b.size();
        if (b.size() == 0)
            return a.size();
        
        Iterator<java.util.Map.Entry<Long, Integer>> iterator = a.entrySet().iterator();
        while (iterator.hasNext()) {
            java.util.Map.Entry<Long, Integer> pairs = iterator.next();
            Long key = pairs.getKey();
            if (b.containsKey(key) == true) {
                unionValue += Math.max(pairs.getValue(), b.get(key));
            }
        }
        return unionValue;
    }

    public static double getSimilarityIndex(
            HashMap<Long, Integer> refGraph,
            HashMap<Long, Integer> targetGraph)
    {
        double index = -1;

        int union = Union(refGraph, targetGraph);
        int intersection = Intersect(refGraph, targetGraph);

        System.out.println("Union: " + union + " -- Intersection: " + intersection);

        index = intersection / (double)union;

        return index;

    }

    private static int Union(HashMap<Long, Integer> a,
                             HashMap<Long, Integer> b) {
        if (a == null) {
            if (b == null)
                return 0;
            return realGraphSize(b);
        }
        if (b == null) {
            return realGraphSize(a);
        }
        if (a == b) {
            return realGraphSize(a);
        }

        if (a.size() == 0)
            return realGraphSize(b);
        if (b.size() == 0)
            return realGraphSize(a);

        HashMap<Long, Integer> resultGraph = new HashMap<Long, Integer>(a);
        // iterate over b and add or adjust the transKey value as needed
        Iterator<java.util.Map.Entry<Long, Integer>> entryItr = b.entrySet().iterator();
        while (entryItr.hasNext())
        {
            java.util.Map.Entry<Long, Integer> entry = entryItr.next();
            Long key = entry.getKey();
            if ((key == transTypeOrder) || (key == transTypeFill) ||
                    (key == transTypeCancel) || (key == transTypeCancelReplace))
            {
                Integer val = Integer.max(resultGraph.get(key),  entry.getValue());
                resultGraph.put(key,  val);
            }
            else if (resultGraph.containsKey(key) == false)
            {
                resultGraph.put(key, entry.getValue());
            }
        }

        return realGraphSize(resultGraph);
    }

    private static int realGraphSize(HashMap<Long, Integer> a) {
        int size_transactions = (a.containsKey(transOrder)?a.get(transOrder):0);
        size_transactions += (a.containsKey(transFill)?a.get(transFill):0);
        size_transactions += (a.containsKey(transCancel)?a.get(transCancel):0);
        size_transactions += (a.containsKey(transCancelReplace)?a.get(transCancelReplace):0);
        return (a.size() + size_transactions -4);
    }

    private static int Intersect(HashMap<Long, Integer>  a,
                                 HashMap<Long, Integer>  b) {
        if ((a == null) || (b == null))
            return 0;
        if ((a.size() == 0) || (b.size() == 0))
            return 0;
        Iterator<java.util.Map.Entry<Long, Integer>> iterator = a.entrySet().iterator();
        int counter = 0;
        while (iterator.hasNext()) {
            java.util.Map.Entry<Long, Integer> entry = iterator.next();
            Long key = entry.getKey();
            if (b.containsKey(key) == true) {
                counter += 1;
            }
        }
        // measure the transaction type intersection.
        int aVal = a.containsKey(transOrder)?a.get(transOrder):0;
        int bVal = b.containsKey(transOrder)?b.get(transOrder):0;
        int transCount = Integer.min(aVal, bVal);
        aVal = a.containsKey(transFill)?a.get(transFill):0;
        bVal = b.containsKey(transFill)?b.get(transFill):0;
        transCount += Integer.min(aVal, bVal);
        aVal = a.containsKey(transCancel)?a.get(transCancel):0;
        bVal = b.containsKey(transCancel)?b.get(transCancel):0;
        transCount += Integer.min(aVal, bVal);
        aVal = a.containsKey(transCancelReplace)?a.get(transCancelReplace):0;
        bVal = b.containsKey(transCancelReplace)?b.get(transCancelReplace):0;
        transCount += Integer.min(aVal, bVal);
        return (counter - 4 + transCount);
    }


}
