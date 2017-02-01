package com.objy.se.query;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.objy.se.query.util.SchemaHelper;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketEngine;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.objy.data.Variable;
import com.objy.db.TransactionMode;
import com.objy.db.TransactionScope;
import com.objy.expression.language.LanguageRegistry;
import com.objy.statement.Statement;

public class QueryServer {

	private static class QueryServerParams { 
		@Parameter(names = "-bootfile", description = "Name of the graph DB bootfile.")
		public String bootFile = null;
		@Parameter(names = "-port", description = "port number for the web service")
		public Integer portNum = 8181;
	};
	
	private static QueryServerParams _params = new QueryServerParams();
	
    public static void main(String[] args)
    {
    	
    	processParams(args);
    	
        HttpServer server = null;
		try {
			server = HttpServer.createSimpleServer("", 
					/*InetAddress.getLocalHost().getHostAddress(),*/_params.portNum);

			server.getServerConfiguration().addHttpHandler(
					new HttpHandler() {

						@Override
						public void service(Request arg0, Response response)
								throws Exception {
							final SimpleDateFormat format = 
									new SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
							final String date = format.format(new Date(System.currentTimeMillis()));
							response.setContentType("text/plain");
							response.setContentLength(date.length());
							response.getWriter().write(date);
						}
						
					}, "/time");
			
			CLStaticHttpHandler staticHttpHandler = 
					new CLStaticHttpHandler(QueryServer.class.getClassLoader());
			staticHttpHandler.addDocRoot("/assets/public/");
	
			server.getServerConfiguration().addHttpHandler(staticHttpHandler);
	        
			final WebSocketAddOn addon = new WebSocketAddOn();
			for (NetworkListener listener : server.getListeners()) {
			    listener.registerAddOn(addon);
			}
			
			final WebSocketApplication queryService = new QueryService(_params.bootFile);
			WebSocketEngine.getEngine().register("", "/", queryService);

			server.start();
			// init objy and some buffering..
			init(_params.bootFile, null);
			
			System.out.println("Press any key to stop the server...");
		    System.in.read();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		}
    }

	private static void init(String bootfilePath, String propertyFileName) {
		System.out.println("...Initialization...");

		DatabaseManager manager = DatabaseManager.GetInstance(bootfilePath, propertyFileName);
		manager.open();	// make sure we have a connection

        try (TransactionScope trxScope = new TransactionScope(TransactionMode.READ_ONLY)) 
		{
			String initClassName = SchemaHelper.getInitClassName();

			String queryString = "from " + initClassName + " return *";//"from Account return *";
			System.out.println("... DO: " + queryString);
			Statement doStatement = new Statement(LanguageRegistry.lookupLanguage("DO"), 
					queryString);
	
			Variable results = doStatement.execute();
			System.out.println(results.getSpecification().getLogicalType().toString());
			trxScope.complete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void processParams(String[] args) {
		System.out.println("args:" + args[0] + "," + args[1]);
		JCommander commander = new JCommander(_params, args);
		commander.setProgramName("FinDataQueryServer");
		
		if (_params.bootFile == null) 
		{
			System.err.println("You need to provide a graph DB bootfile path");
			commander.usage();
			System.exit(1);
		}
	}

	private static HttpServer startServer() {
		//final ResourceConfig rc = new ResourceConfig().
		return null;
	}

}
