import java.util.LinkedList;
import java.util.Queue;

/*
 * allowed libraries:

import java.lang.Comparable;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.lang.StringBuilder;
import java.lang.Math;
import java.lang.InstantiationException;
*/

class SubwayGraph {
	
	private SplayTree tree;
	// construct an empty graph
	public SubwayGraph()
	{
		tree = new SplayTree();
	}

	// optional debug representation of graph
	public String toString()
	{
		return tree.toString();
	}

	// Add vertex to the graph
	// (Point class is from project 4)
	public void insert(Point loc, String sat)
	{
		tree.insert_record(loc,sat);
	}

	// Add edge to the graph
	public void adjacent(Point loc1, Point loc2)
	{
		tree.addNeighbors(loc1, loc2);
	}

	// Try to find a shortest path between vertices
	public String route(Point start, Point finish)
	{
		return tree.bfs(start, finish);
	}

	// extra credit -- remove a vertex and all incident edges
	public void delete(Point loc)
	{
		tree.deleteStation(loc);
	}
}

