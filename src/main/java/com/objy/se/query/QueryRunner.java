package com.objy.se.query;

public class QueryRunner extends Thread {
	
	private QueryInterface query = null;
	public QueryRunner(QueryInterface query)
	{
		this.query = query;
	}
	
	public void run()
	{
		System.out.println("... running the query in a thread: " + this.getId());
		this.query.run();
	}
	
	public QueryInterface getQuery()
	{
		return this.query;
	}

}
