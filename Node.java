//This class is used to store leaf level data in the tree.  Using a node array to store data
//in leaf nodes of tree (see BTNode class) so that I can set up and maitain a contiguous list ]
//of leaf node values without reproducing the leaf nodes as a separate array or linked list.

public class Node {
	//Fields
	Node next;
	String value; //String to be stored in tree
	int frequency; //Frequency of stored string value
	
	public Node(String value)
	{
		this.value = value;
		this.frequency = 1;
	}
	
	public Node()
	{
		this.value = null;
		this.frequency = 1;
	}
	
	public static String NodestoString(Node[] A)
	{
		String res = "";
		
		for(int i = 0; i < A.length; i++)
			res += A[i].toString();
				
		return res;
	}
	
	
	@Override
	public String toString()
	{
		String res = "";		
		res += "Value: " + this.value + ", Frequency:" + this.frequency + "\n";		
		return res;	
	}

}
