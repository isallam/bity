package com.objy.se.query;

import com.objy.data.*;
import com.objy.db.Connection;
import com.objy.db.TransactionMode;
import com.objy.db.TransactionScope;
import com.objy.expression.language.LanguageRegistry;
import com.objy.se.query.util.SchemaHelper;
import com.objy.statement.Statement;

import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseManager {

	private static DatabaseManager SharedInstance = null;
	private String propertyFileName;
	private String graphDbFilePath;
	private HashMap<String, com.objy.data.Class> classTypeMap = new HashMap<String, com.objy.data.Class>();
	private HashMap<String, Long> classNumberMap = new HashMap<String, Long>();
	private Connection connection = null;

	static Logger logger = null;      // Set up logging
	public static Logger getLogger()
	{
		if (logger == null)
		logger = LoggerFactory.getLogger(DatabaseManager.class);
		return logger;
	}
	
	private DatabaseManager(String graphDbFilePath, String propertyFile) {
		this.setGraphDbFilePath(graphDbFilePath);
		this.setPropertyFileName(propertyFileName);
	}

	private void setup() {

		try (TransactionScope trxScope = new TransactionScope(TransactionMode.READ_ONLY))
		{
			com.objy.data.Class classObj = null;
			long classNumber = 0;
			for (String className : SchemaHelper.classNames) {
				classObj = com.objy.data.Class.lookupClass(className);
				classNumber = classObj.getClassNumber();
				classTypeMap.put(className, classObj);
				classNumberMap.put(className, classNumber);
			}
            
            // TBD:... for now we have redundant information between whats in 
            //         the SchemaHelper and the classNumberMap above.
            SchemaHelper.cacheSchemaInfo();

			trxScope.complete();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
        // make sure we have the tag class created.
        //ExecuteDOUpdate.createSchema();
	}

	private void setPropertyFileName(String value) {
		this.propertyFileName = value;
	}

	private void setGraphDbFilePath(String value) {
		this.graphDbFilePath = value;
	}

	public String getPropertyFileName() {
		return this.propertyFileName;
	}

	public String getGraphDbFilePath() {
		return this.graphDbFilePath;
	}

	public synchronized static DatabaseManager GetInstance(
			String graphDbFilePath, String propertyFileName) {
		if (DatabaseManager.SharedInstance == null) {
			com.objy.db.Objy.enableConfiguration();
			com.objy.db.Objy.startup();
			DatabaseManager.SharedInstance =
					new DatabaseManager(graphDbFilePath, propertyFileName);
		}
		return DatabaseManager.SharedInstance;
	}

	public synchronized static void DestroyInstance() {
		if (DatabaseManager.SharedInstance == null) {
			DatabaseManager.SharedInstance = null;
		}
	}

	public boolean open() {
		boolean retValue = true;
		
		try {
			Connection conn = Connection.getCurrent();
			if (conn == null)
			{
				new Connection(graphDbFilePath);
				setup();
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			retValue = false;
		}
		return retValue;
	}
		
	public synchronized static void terminate() {
		com.objy.db.Objy.shutdown();
		DatabaseManager.SharedInstance = null;
	}

}
