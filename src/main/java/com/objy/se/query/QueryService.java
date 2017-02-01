package com.objy.se.query;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;

public class QueryService extends WebSocketApplication {

	Queue<WebSocket> wsQueue = new ConcurrentLinkedQueue<WebSocket>();
	
    //WebSocket clientSocket = null;

    // TBD: we need this to be configurable.
    String bootfilePath = null;
    String propertyFileName = "NONE";
    
    public QueryService(String bootfile) {
        this.bootfilePath = bootfile;
    }

    @Override
    public void onConnect(WebSocket socket) {
        System.out.println("Socket Connected: " + socket.toString());
        wsQueue.add(socket);
    }
    
    @Override
    protected boolean onError(WebSocket socket, Throwable t) {
    	System.out.println("Socket error...");
    	wsQueue.remove(socket);
    	return super.onError(socket, t);
    }

    @Override
    public void onPing(WebSocket socket, byte[] bytes)
    {
    	System.out.println("Got PING from socket: " + socket.toString());
    }

    @Override
    public void onPong(WebSocket socket, byte[] bytes)
    {
    	System.out.println("Got PONG from socket: " + socket.toString());
    }
    
    @Override
    public void onMessage(WebSocket socket, String message) {
        System.out.println("Received TEXT message: " + message);
        JsonParser parser = new JsonParser();
        try {
            JsonObject request = (JsonObject) parser.parse(message);

            DatabaseManager manager = DatabaseManager.GetInstance(bootfilePath, propertyFileName);
            QuerySpec querySpec = new QuerySpec(request);
            // setup the specs.
            int verbose = request.getAsJsonPrimitive("verbose").getAsInt();
            boolean success = querySpec.setup(verbose);
            if (!success)
            {
                // TBD... need to send back an error message.
                System.out.println("Aborting the request.");
                return;
            }
            QueryInterface query = null;
            ArrayBlockingQueue<String> resultQueue = new ArrayBlockingQueue<String>(500);
            
            String qType = request.getAsJsonPrimitive("qType").getAsString();
            System.out.println("WE got qType: " + qType);
            String qContext = request.getAsJsonPrimitive("qContext").getAsString();
            System.out.println("... for qContext: " + qContext);
            
            if (qType.equalsIgnoreCase("DOQuery")) {
            	query = new ExecuteDO(manager, querySpec, resultQueue);
            } else if (qType.equalsIgnoreCase("getEdges")) {
                query = new GetEdges(manager, querySpec, resultQueue);
            } else if (qType.equalsIgnoreCase("similarity")) {
                query = new Similarity(manager, querySpec, resultQueue);
            } else if (qType.equalsIgnoreCase("DoUpdate")) {
                query = new ExecuteDOUpdate(manager, querySpec, resultQueue);
            } else {
                System.out.println("Error: There is no handler for: " + qType);
            }
            
            if (query != null)
            {
	            System.out.println("current thread: " + Thread.currentThread().getId());
	            QueryRunner qRunner = new QueryRunner(query);
	            qRunner.start();

	            // we should be able to cancel query if needed.
	            processResultQueue(resultQueue, socket);
            }

            } catch (JsonParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

    // TBD: we need a method to interrupt the wait
    private void processResultQueue(ArrayBlockingQueue<String> resultQueue,
            WebSocket socket) {
        boolean done = false;
        String result = null;

        System.out.println("processing result queue...");

        do {
            try {
                //System.out.println("... doing a poll on the result queue.");
                result = resultQueue.poll(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            JsonObject jsonResult = new JsonObject();
            if (result != null) {
                if (result.endsWith(QueryInterface.EOP)) {
                    String[] outcome = result.split(",");
                    jsonResult.addProperty("type", "EndOfResults");
                    jsonResult.addProperty("context", outcome[0]);
                    done = true;
                } else {
                    jsonResult.addProperty("type", "message");
                    jsonResult.addProperty("data", result);
                }
                socket.send(jsonResult.toString());
            }
        } while (!done);
        System.out.println("Done processing result queue...");
    }

    @Override
    public void onClose(WebSocket socket, DataFrame frame) {
        if (socket != null && frame != null) {
            System.out.println("Socket Closed: " + socket.toString() + " - payload: " + frame.getTextPayload());
        } else if (socket != null) {
            System.out.println("Socket Closed: " + socket.toString());
            wsQueue.remove(socket);
        } else {
            System.out.println("Socket to client closed.");
        }
    }

}
