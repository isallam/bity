package com.objy.se.query.util;

import com.objy.data.Instance;

public class VertexEntry {
	public int priorityValue;
	public Instance vertex;
	public VertexEntry(Instance vertex, int priorityValue)
	{
		this.vertex = vertex;
		this.priorityValue = priorityValue;
	}
}
