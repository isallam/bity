package com.objy.se.query.util;

import java.util.ArrayList;
import java.util.List;

import com.objy.data.Instance;
import com.objy.data.Variable;

public class TopList {
	public List<VertexEntry> vertexList = new ArrayList<VertexEntry>();
	int maxSize = 10;
	int maxPriorityValue = Integer.MIN_VALUE;
	int minPriorityValue = Integer.MAX_VALUE;
	public TopList(int maxSize)
	{
		this.maxSize = maxSize;
	}
	
	public boolean add(Instance vertex, int priorityValue)
	{
		boolean retValue = true;
		if (vertexList.size() < maxSize)
		{
			VertexEntry entry = new VertexEntry(vertex, priorityValue);
			vertexList.add(entry);
			maxPriorityValue = Math.max(maxPriorityValue, priorityValue);
			minPriorityValue = Math.min(minPriorityValue, priorityValue);
			//Clustering.logger.info("minP: {} - maxP:{}", minPriorityValue, maxPriorityValue);
		}
		else if (priorityValue > maxPriorityValue || priorityValue > minPriorityValue)
		{
			// replace an entry with min priority value
			for (VertexEntry entry : vertexList)
			{
				if (entry.priorityValue == minPriorityValue)
				{
					entry.vertex = vertex;
					entry.priorityValue = priorityValue;
					maxPriorityValue = Math.max(maxPriorityValue, priorityValue);
					adjustMinValue();
					break;
				}
			}
			
		}
		return retValue;
	}

	private void adjustMinValue() {
		minPriorityValue = Integer.MAX_VALUE;
		for (VertexEntry entry : vertexList)
		{
			minPriorityValue = Math.min(minPriorityValue, entry.priorityValue);
		}
	}
	
}
