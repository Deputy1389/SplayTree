//Sean Gallagher
//Deputy1389

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Point defined by integer Cartesian coordinates.
 *
 * This just stores the two endpoints as plain int coordinates,
 * and as public attributes -- not encapsulated at all.
 */
class Point implements Comparable<Point>{

	/** Cartesian coordinates are just plain public attributes. */
	public int x, y;

	/** Ctor for a point, given its x, y coordinates. */
	public Point(int xx, int yy){
		x = xx;
		y = yy;
	}

	/** Copy of a point. */
	public Point(Point p){
		this(p.x, p.y);
	}

	/** Comparison is lexicographical: vertical first, horizontal second. */
	public int compareTo(Point p){
		final int dy = p.y - y;
        return 0 == dy ? p.x - x : dy;
	}

	/** Stringify in a very plain comma-separated format. */
	public String toString(){
		return "" + x + ", " + y;
	}
}


/**
 * Node of the tree.  It's a standard BST node except for the parent ptr.
 * There are some helper methods here specific to the application.
 */
class Node{

	public Point record;
	public String satellite;
	public ArrayList<Point> neighbors;
	public Node left, right, parent;
	public boolean visited;
	
	Node(Point rec, String sat){
		record = rec;
		satellite = sat;
		neighbors = new ArrayList<Point>();
		visited = false;
	}
	
	public void visit(boolean b){
		visited = b;
	}
	
	public void addToNeihbors(Point p){
		neighbors.add(p);
	}
	
	public String toString(){
		return satellite;
	}
}



/**
 * Splay tree (BST invented by Sleator and Tarjan).
 */
class SplayTree{
	private Node root;
	
	//breadth first search
	//this method searches the the for the dest point from the source point using breadth first
	//search. It returns a string that lists the route from source to dest
	public String bfs(Point source, Point dest){
		ArrayList<String> str = new ArrayList<String>();	//arraylist to hold the station strings
		Queue<Node> queue = new LinkedList<Node>();			//queue to hold nodes when searching
	    if (root == null)
	        return "Emptry Tree";
	    unvisit();						//unvisit the tree
	    root = splay(root, source);		//get the source node in the root

	    root.parent = null;				//set parent to null
	    queue.clear();	
	    queue.add(root);				//add source to queue
	    Node node;
	    while(!queue.isEmpty()){		//runs until the queue is done
	        node = queue.remove();		//sets node to what the queue popped
	        root = splay(root, node.record);	//gets the node into the root
	        root.visit(true);					//this node has been visited
	        node.visit(true);			
	        if(node.record.compareTo(dest)==0){	//if the dest has been found break
	        	break;
	        }
	        
	        for(int i=0; i<node.neighbors.size(); i++){	//iterate over the nodes neighbors
	        	root = splay(root, node.neighbors.get(i));	//gets the neighbor node
	        	if(!root.visited){				//if it hasnt been visited yet
	        		root.parent = node;			//set the parent
	        		root.visit(true);			//this node has been visited
	        		queue.add(root);			//add the neighbor to the queue
	        	}
	        }
	        if(queue.isEmpty()){
	        	System.out.println("Empty Q");
	        }
	    }
	    node = root;	//store the dest node into node
	    
	    while(node.parent!=null){		//loop through the parents
	    	str.add("Station "+node.record + " " + node);	//add the string for the node
	    	node = node.parent;								//go to the nodes parent
	    }
	    str.add("Station "+node.record + " " + node);		//add the final string
	    String string="";
	    for(int i=str.size()-1; i>=0; i--){		//reverse the strings
	    	string += str.get(i);
	    	if(i!=0)
	    		string += "\n";
	    }
	    return string;		//return the string
	}
	
	//This delete method is the same from project 4 with the neighbor deletion added
	public void deleteStation(Point key){
        if (root == null) 
        	return; 			//empty tree
        root = splay(root, key);//get the node to the root
        Node node = root;
        int cmp = key.compareTo(root.record);
        
        if (cmp == 0){ //if the root is the key node delete it
            if (root.left == null){	//if the root only has a right child
                root = root.right;	//the right child is now the root
            }else{
                Node x = root.right;		
                root = root.left;			//set root to left child
                root = splay(root, key);
                root.right = x;				//set the right child to the previous roots right child
            }
        }
        //neighbor deletion
        for(int i=0; i<node.neighbors.size(); i++){		//iterate over the neighbors
        	root = splay(root, node.neighbors.get(i));	//get the node of the neihbor
        	root.neighbors.remove(node.record);			//remove the deleted node from their list
        	}
	}
	

	//this method sets loc1 and loc2 as neighbors
	public void addNeighbors(Point loc1, Point loc2) {
		root = splay(root, loc1);	//find loc1 node
		root.neighbors.add(loc2);	//add loc2 to loc1's neighbor list
		root = splay(root, loc2);	//find loc2 node
		root.neighbors.add(loc1);	//add loc1 to loc2's neighbor list
	}
	
	//unvisit helper
	public void unvisit(){
		root = unvisit(root);
	}
	
	//unvisit sets visit to false and parent to null for all nodes in the tree
	public Node unvisit(Node node){
		node.visit(false);	//visit = false
		node.parent=null;
		if(node.left!=null)	//checks the left child
			node.left = unvisit(node.left);	//recursive call on left child
		if(node.right!=null)	//checks right child
			node.right = unvisit(node.right);	//recursive call on right child
		return node;	//returns the updated node
	}

	//toString helper
	public String toString(){
		return toString(root);
	}
	
	/** Portray tree as a string.  Optional but recommended. */
	public String toString(Node node){
		String result = "";
		if (node == null)
			return "";
		result +=toString(node.left); 		//adds the left child to the string
		result +="point " + node.record + " " + node.toString() + '\n'; //adds current node to string
		result +=toString(node.right);		//adds the right child to the string
		return result;
	}


	/**
	 * Search tree for key k.  Return its satellite data if found,
	 * and splay the found node.
	 * If not found, return null, and splay the would-be parent node.
	 */
	public String lookup(Point key){
		if (root == null) 					//if the root is empty
			return "not found";	
		root = splay(root, key);			//play the tree to get the key at the root
		if(root.record.compareTo(key) != 0) //if the key is not in the tree
			return "not found";			
		else return ""+root.toString();		//if the key is found
	}

	/**
	 * Insert a new record.
	 * First we do a search for the key.
	 * If the search fails, we insert a new record.
	 * Otherwise we update the satellite data with sat.
	 * Splay the new, or altered, node.
	 */
	public void insert_record(Point key, String sat){
		int compare;
		if(root == null){						//if the tree is empty
			root  = new Node(key, sat);			//create the root
			return;
		}
		root = splay(root, key);				//splay the tree
		compare = key.compareTo(root.record);	//compare the roots record with the parameter
		if (compare < 0){						//If the key is less than
			Node node = new Node(key, sat);		//create a new node
			node.left = root.left;				//set the roots left child to the new nodes left child
			node.right = root;					//set the root to the nodes right child
			root.left = null;					//set root.left to null so theres no duplicates
			root = node;						//the new node is now the root
			return;
		}else if (compare > 0){					//If the key is greater than
			Node node = new Node(key, sat);		//new node
			node.right = root.right;			//node.right is the right right child
			node.left = root;					//node.left is the root
			root.right = null;					//the roots right child is not null
			root = node;						//the root is now the new node
			return;
		}else{									//if duplicate key
			root.satellite = sat;				//replace the old satellite with the new
		}
	}
	
	private Node splay(Node node, Point key){
		if (node == null) 
			return null;
		int compare = key.compareTo(node.record);
		int compare2;
		if (compare < 0){
			if (node.left == null) {	//Key isn't in the tree
				return node;
			}
			compare2 = key.compareTo(node.left.record); 		//compares the key to the left child
			if (compare2 < 0) {									//if the key is smaller than the left child
				node.left.left = splay(node.left.left, key);	//splay 
				node = rotateRight(node);						//rotate to the right
			}else if (compare2 > 0) {							//if key is greater than the left child
				node.left.right = splay(node.left.right, key);	//splay
				if (node.left.right != null)					//if the left child has no right child
					node.left = rotateLeft(node.left);			//rotate the left child left
			}
			if (node.left == null) 			//if the key wasn't found
				return node;
			else                
				return rotateRight(node);	//else rotate tree right and return
		}

		else if (compare > 0){ 
			if (node.right == null) {	//Key isn't in the tree
				return node;
			}

			compare2 = key.compareTo(node.right.record);		//compare the key to the right child
			if (compare2 < 0){									//if less than
				node.right.left  = splay(node.right.left, key);	//splay right childs left child
				if (node.right.left != null)					//if it isnt null
					node.right = rotateRight(node.right);		//rotate the right child right
			}else if (compare2 > 0){							//if it is greater than the right child
				node.right.right = splay(node.right.right, key);//splay the right childs right child
				node = rotateLeft(node);						//then rotate the tree left
			}
			
			if (node.right == null) 		//if the key wasn't found
				return node;
			else return rotateLeft(node);	//else rotate tree left and return
		}
		else return node;	//if its the current root
	}

	//right rotate
	private Node rotateRight(Node node){
		Node x = node.left;	
		node.left = x.right;		
		x.right = node;				
		return x;
	}

	//left rotate
	private Node rotateLeft(Node node){
		Node x = node.right;
		node.right = x.left;
		x.left = node;
		return x;
	}

	/**
	 * Remove a record.
	 * Search for the key.  If not found, return null.
	 * If found, save the satellite data, remove the record, and splay
	 * appropriately.  Use one of the two following methods and update
	 * this comment to document which method you actually use.
	 *
	 * Appropriate method 1:  splay the bereaved parent.
	 *   some node x will be deleted, so splay the parent of x.
	 * Appropriate method 2:  splay twice.
	 *   Find the node u with the key.  Splay u, then immediately splay
	 *   the successor of u.  Finally, remove u by splicing.
	 *
	 * Return the satellite data from the deleted node.
	 */
	public String delete(Point key){
        if (root == null) 
        	return "Empty Tree"; //empty tree
        root = splay(root, key);//get the node to the root
        String str = ""+root;	//store it into the string
        int cmp = key.compareTo(root.record);
        
        if (cmp == 0){ //if the root is the key node delete it
            if (root.left == null){	//if the root only has a right child
                root = root.right;	//the right child is now the root
                return str;
            }else{
                Node x = root.right;		
                root = root.left;			//set root to left child
                root = splay(root, key);
                root.right = x;				//set the right child to the previous roots right child
                return str;
            }
        }
		return "not found";	//else it wasnt found
	}


}


