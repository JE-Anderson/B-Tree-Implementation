
public class BTNode {
	//Fields
	public int keyCount; //to determine if key is full and if not, what next open index is
	public int nodeID; //ID for the node.  ID's will be created sequentially for each new node.
	public int nodeSize; //Max size for nodes
	public BTNode parent; //Parent node for this node.
	public String[] keyArray; //If node is index or leaf node, this array stores the keys (words in this case) stored in the node for indexing.
	public BTNode[] childNodeArray; //If node is index node, this array stores pointers to this nodes child nodes
	public Node[] leafData; //If node is leaf node, linked list stores each data value and points to the next value to the right in the tree.  Last node reserved for next leaf over in tree.
	public boolean leaf; //determines if node is a leaf node or index node
	public boolean root; //determines if nodes is root node
	
	
	//Constructors

	
	public BTNode (int keyCount, int nodeID, int nodeSize, boolean leaf, boolean root)
	{
		this.keyCount = keyCount;
		this.nodeID = nodeID;
		this.nodeSize = nodeSize;
		this.keyArray = new String[nodeSize];
		this.leaf = leaf;
		this.root = root;
		
		if(leaf == false)
		{
			this.childNodeArray = new BTNode[nodeSize + 1];
			this.leafData = null;
		}
		if(leaf == true)
		{
			this.leafData = new Node[nodeSize + 1];
			for(int i = 0; i < nodeSize + 1; i++)
			{
				this.leafData[i] = new Node();
			}
			
			this.childNodeArray = null;
			
			for(int i = 0; i < nodeSize; i++) //Links the n+1 pointer nodes.  Linkages between leaves will be handled via the insert method in BTree class.
				leafData[i].next = leafData[i+1];
		}		
	}
	
	//Method for collapsing a BTNode which has null values in the 1st index or more and
	//which stores one or more keys beyond those initial null values.
	//This method transfers all keys and pointers to the 'left-most' positions in the 
	//node.  This will be used to support splitting a node when the first half of the values
	//in a given node are copied into a new "left" node, leaving the right half of the values
	//remaining in the original node that is now the "right" node.
	//This will only be used after splitting a full node, so all arrays will have a non-null
	//value in their last slot.
	public void collapse()
	{
		int i = -1; //initial value just to initiate loops.  Will be overwritten by first for-loop below.
		int j = 0;
		int k;
		
		//find first index in keyArray that stores an acutal key
		boolean keepFirst = false;
		for(k = 0; k < this.keyArray.length; k++)
		{
			if( !(this.keyArray[k] == null) && keepFirst == false)
			{
				i = k;
				keepFirst = true;
			}
		}
		
		//Now collapse node.  Index i is the index of the first key in the keyArray and 
		//index j is used to transfer values one at a time back into the start of the nodes
		//arrays.
		for(; i < this.keyArray.length; i++, j++)
		{
			this.keyArray[j] = this.keyArray[i];
			this.keyArray[i] = null;
			
			if(this.leaf) //i.e. working with leafData Node[] array
			{
				this.leafData[j].value = this.leafData[i].value;
				this.leafData[i].value = null;
			}
			
			else //i.e. this is an index node and working with childNodeArray of BTNodes
			{
				this.childNodeArray[j] = this.childNodeArray[i];
				this.childNodeArray[i] = null;
			}
		}
		
		//finally, if this is an index node, grab the last child node which the for-loop above excludes and transfer it over.
		if(!this.leaf)
		{
			this.childNodeArray[j] = this.childNodeArray[i];
			this.childNodeArray[i] = null;
		}
	}
	
	
	@Override
	public String toString()
	{
		String res = "Node ID: " + this.nodeID + "\n";
		
		if(this.leaf)
		{
			res += "Keys/Records: \n";
			for(int i = 0; i < nodeSize; i++)
			{
				res += this.keyArray[i] + ", " + this.leafData[i].value + "\n";
			}
			res += "\n";
		}
		
		else
		{
			res += "Keys: ";
			for(int i = 0; i < nodeSize; i++)
			{
				res += this.keyArray[i] + ", ";
			}
			res += "\nChildIDs: ";
			for(int i = 0; i <= nodeSize; i++)
			{
				if( this.childNodeArray[i] != null)
				res += this.childNodeArray[i].nodeID + ", ";
			}
		}
		
		return res;
	}
	
}
