
public class BPlusTree {
	//Fields
	BTNode root;
	int nodeSize; //max keys per node in tree
	int nodeCount; //for assigning sequential nodeIDs
	int recordCount; //just in case we want to pull this at some point
	
	//Constructors
	//Build an initial tree with only an empty root node which values can then be inserted into.
	public BPlusTree(int nodeSize)
	{
		this.nodeCount = 1;
		this.nodeSize = nodeSize;
		this.recordCount = 0;
		this.root = new BTNode(0, this.nodeCount, this.nodeSize, true, true);
	}
	
	
	//Methods
	//Helper Method for Insertions
	//Given a key to insert and a leaf node to insert into or an index
	//node to follow to the next node down, identify which pointer (nodeArray or childArray)
	//the key should be inserted into, or which pointer should be followed down to the next level.
	public int findPointer(BTNode inNode, String key)
	{
		if( inNode.keyArray[0] == null || key.compareTo(inNode.keyArray[0]) < 0 ) //i.e. key < keyArray[0]
			return 0;
		
		for(int i = 0; i < inNode.nodeSize - 1; i++)
		{
			if( key.compareTo(inNode.keyArray[i]) >= 0 && ( (inNode.keyArray[i+1] == null) || key.compareTo(inNode.keyArray[i+1]) < 0) )
				return i+1;
		}
		
		//i.e. if( key.compareTo( inNode.keyArray[inNode.nodeSize - 1] ) >= 0 )
		return inNode.nodeSize;
	}
	
	//Helper method to traverse from the root to the leaf a key needs to be inserted into
	public BTNode findLeaf(BTNode root, String key)
	{
		BTNode cur = root;
		while(cur.leaf == false)
		{
			//Keep updating cur node by finding proper child node and replacing cur with it.
			int i = findPointer(cur, key);
			cur = cur.childNodeArray[i]; 
		}
		return cur; //at proper leaf node after exiting or loop.
	}
	
		
	//Primary/outer insertWord method.  Utilizes findLeaf (and findPointer by extension) and insertNode.
	//findLeaf returns the leaf node to insert the word into, insertNode is a method that manages all the
	//details of the insertion of a new key into a node, including splitting, and new parent node creation.
	public void insertWord(BTNode root, String key)
	{
		BTNode leafNode = findLeaf(root, key); //Locate the proper leaf node to insert key into.
		//Check if tree contains word.  If not, insert
		if(this.contains(key) == -1)
			insertNode(leafNode, key, null, null); //Insert key into leafNode.
				
		//If tree contains word, increment frequency.
		else
			for(int i = 0; i < leafNode.nodeSize; i++)
			{
				if( key.equals(leafNode.keyArray[i]) )
					leafNode.leafData[i].frequency++;
			}
		
		//Re-wire leaf nodes so they all connect to each other in the right sequence. Checks to make sure root is not the only node. 
//		if(!root.leaf)
//			this.reWire(this.root);

	}
	
	
	//Helper method to re-wire leaf nodes after insertions so that all leaf nodes point to the next consecutive leaf node
//	public void reWire(BTNode root)
//	{
//		BTNode leaf = root;
//		while(!leaf.leaf)
//			leaf = leaf.childNodeArray[0];
//		
//		
//		
//		
//	}
	
	
	//insertNode method is the heavy lifter.  This method manages inserting a key into a given BTNode in a tree.
	//Includes management of splitting nodes and any new node creation.
	//This method operates recursively, repeating conditionally until it can insert a key into a non-full node.
	//Parameters LChild and RChild are only used on recursive calls to insert a key upwards into an index node, in which case 
	//two new children will have been created by a split at the level below and will be represented by these parameters.
	//Since the initial call of this method will always be at the root, the intial call will always have these parameters null.
	public void insertNode(BTNode inNode, String key, BTNode LChild, BTNode RChild)
	{
		int i = findPointer(inNode, key); //location to insert the key, and the first (left) pointer associated with it for BTNodes, and the one record associated for key nodes.
		
		////////////////////// Non-Full Node Insertion //////////////////////////
		if(inNode.keyCount < inNode.nodeSize)
		{
			//Non-Full Leaf Insertion
			if(inNode.leaf)
			{
				//Storage variables used to shuffle values already in the node to the right of the ith position
				String temp1 = null;
				String temp2 = key;
				
				//Since this is a leaf node, which is one to one with respect to keys and records, we can shuffle keys and records to the right simultaneously.
				for(int j = i; j < inNode.nodeSize; j++)
				{
					temp1 = inNode.leafData[j].value;
					inNode.leafData[j].value = temp2; //shuffle nodes storing records to the right
					inNode.keyArray[j] = temp2; // shuffle array of keys to the right
					temp2 = temp1;					
				}
				inNode.leafData[i].frequency = 1; //Set frequency to 1 on first insertion.
				inNode.keyCount++; //remember to increment keyCount so we know how full the node is.
			}
			
			//Non-Full Index Node Insertion
			else if(!inNode.leaf)
			{
				//Will need to shuffle key array values starting at index i to the right up to index nodeSize - 1
				//Will need to shuffle childNode array objects starting at index i to the right up to index nodeSize
				//Variables for shuffling key array
				String temp1;
				String temp2 = key;
				
				//Variable for shuffling childNode array
				BTNode nTemp1;
				BTNode nTemp2 = null; //See description above second for loop for why this starts null
				
				//Start by shuffling keys to right and inserting new key
				for(int j = i; j < inNode.nodeSize; j++)
				{
					temp1 = inNode.keyArray[j];
					inNode.keyArray[j] = temp2;
					temp2 = temp1;
				}
				
				//Then shuffle childNodes - need to go one index farther than previous loop for this
				//Will have 2 child nodes as incoming parameters to the method call in this case.  LChild goes into index i and RChild into index i+1
				//So, we will first shuffle keys to the right with no new insertions, then insert new child nodes at end.
				for(int j = i; j <= inNode.nodeSize; j++)
				{
					nTemp1 = inNode.childNodeArray[j];
					inNode.childNodeArray[j] = nTemp2;
					nTemp2 = nTemp1;
				}
				//Now replace indices i and i+1 with LChild and RChild.  Index i will be null and index i+1 will be overwritten with the updated values for that slot
				inNode.childNodeArray[i] = LChild;
				inNode.childNodeArray[i].parent = inNode;
				inNode.childNodeArray[i+1] = RChild;
				inNode.childNodeArray[i+1].parent = inNode;
				inNode.keyCount++; //remember to increment keyCount so we know how full the node is.
			}
		}

		
		///////////////////////// INSERTION FOR NON-FULL NODES COMPLETE //////////////////////////
		
		//Full Node Insertion - Splitting Required
		else if(inNode.keyCount == inNode.nodeSize)
		{
			//Full Leaf Node Insertion
			if(inNode.leaf)
			{
				//Create new "Left" leaf node that will hold the left half of the values.  Also increase nodeCount of tree since creating one new node.
				this.nodeCount++;
				BTNode LNode = new BTNode(0, this.nodeCount, this.nodeSize, true, false);
				//Wire LNode's leafData array into inNodes like a linked list (the + part of the tree) and set LNode parent to inNode parent.
				LNode.leafData[this.nodeSize].next = inNode.leafData[0];
				LNode.parent = inNode.parent;
				
				//For leaf nodes we have 2 cases to handle when an insertion results in a split - new key ends up in left node (LNode) or right node (inNode).
				//1 - i <= (nodeSize / 2) - 1 (nodeSize is even) OR i <= (nodeSize / 2) (nodeSize is odd).  This means we will be inserting the new key into a position that will end up in the newly created LNode.
				//								LNode will get the first ((nodeSize + 1) / 2) - 1 values.  Since we have nodeSize+1 values to deal with when splitting is needed
				//									we want the smallest half of those in LNode.  We know in this case that must include our new key.  So, we need half - 1 of all
				//									values being considered, including the key being inserted
				//2 - i > (nodeSize / 2) - 1 (even) OR i > (nodeSize) / 2 (odd).  This means we will be inserting the new key into the existing inNode after removing the first half of values and collapsing the node.
				//								LNode will get the first (nodeSize + 1) / 2 values since our new key will be inserted into the "right" node (inNode).
				//Case 1 - new key into LNode, take first (nodeSize + 1 / 2) - 1 values into LNode
				if( this.nodeSize%2 == 0 && i < (this.nodeSize / 2) || this.nodeSize%2 != 0 && i <= (this.nodeSize / 2) )
				{
					//Transfer smallest half of values - 1 into LNode prior to inserting new key
					//Be sure to erase (turn null) values stored in indices transferred to LNode
					for(int j = 0; j < ((this.nodeSize + 1) / 2) - 1; j++ )
					{
						LNode.keyArray[j] = new String(inNode.keyArray[j]); //Transfer key to LNode
						inNode.keyArray[j] = null; //Erase key from inNode
						LNode.leafData[j].value = new String(inNode.leafData[j].value); // Transfer leafData record to LNode
						LNode.leafData[j].frequency = inNode.leafData[j].frequency;
						inNode.leafData[j].value = null; //Erase record from 
						LNode.keyCount++;
						inNode.keyCount--;
					}
					//Now insert the new key into LNode to finish building LNode.
					insertNode(LNode, key, null, null); //This will run on the Non-Full Insertion portion of this method up at the beginning.
					inNode.collapse(); //collapse inNode so that remaining keys are stored starting from index 0 instead of index > 0 after copying values over to LNode.
				}
				else //i.e. new key will be inserted into the right node (inNode).  So we will pull the first ((nodeSize + 1) / 2) values into LNode.
				{
					for(int j = 0; j < (this.nodeSize + 1) / 2; j++)
					{
						LNode.keyArray[j] = new String(inNode.keyArray[j]); //Transfer key to LNode
						inNode.keyArray[j] = null; //Erase key from inNode
						LNode.leafData[j].value = new String(inNode.leafData[j].value); // Transfer leafData record to LNode
						LNode.leafData[j].frequency = inNode.leafData[j].frequency;
						inNode.leafData[j].value = null; //Erase record from 
						LNode.keyCount++;
						inNode.keyCount--;
					}
					//Now collapse inNode and insert new key into inNode to finish splitting inNode.
					inNode.collapse();
					insertNode(inNode, key, null, null); //This will run on the Non-Full Insertion portion of this method up at the beginning.
				}
				
				//Finally, copy up the "middle key", i.e. the one now stored in inNode.keyArray[0].  LNode and inNode will be used and LChild and RChild parameters respectively.
				//Also make sure that the leaf node to the left of LNode (if it exists) has it's leafData array linked to LNode.
				if(inNode.parent != null)
				{
					//NEW CODE - re-wire leaf nodes so the end of one leaf node points to the next leaf node
					BTNode parent = LNode.parent;
					int k = findPointer(parent, LNode.keyArray[0]);
					
					if(k > 0)
						parent.childNodeArray[k - 1].leafData[this.nodeSize].next = LNode.leafData[0];
					
					//EXTRA NEW CODE
					//Need to walk up until k not equal 0.  If we get to root and k still = 0, then we're done, that was left-most leaf node.
					//When we do get a value of k != 0, we need to traverse to the left node, then to the right-most node until at a leaf.
					//Then connect that leaf to LNode.
					else if(k == 0)
					{
						//Step 1 - walk up until k != 0
						while( k == 0 && !parent.root) //Then k = 0 and we need to move up the tree to see if there are any left siblings for our leaf node.
						{
							parent = parent.parent;
							k = findPointer(parent, LNode.keyArray[0]);
						} //parent is the root at this point if k still = 0.
						
						//Step 2 - if k != 0 now, travel down tree to needed leaf node.
						if(k != 0)
						{
							BTNode toConnect;
							//Start by getting first child node to the left of the path leading to our LNode
							toConnect = parent.childNodeArray[k - 1];
							
							//Then follow the right-most childNodes until we get to a leaf node.
							while(!toConnect.leaf)
								toConnect = toConnect.childNodeArray[toConnect.keyCount];
							
							//Then connect our resulting leaf node to LNode.
							toConnect.leafData[this.nodeSize].next = LNode.leafData[0];
						}
					}
					
					//Recursive insertion - Not Part of "New Code" above
					insertNode(inNode.parent, inNode.keyArray[0], LNode, inNode);

				}
				if(inNode.parent == null)
				{
					//Note: This only happens when splitting a root node, requiring the creation of a new root node for the resulting 2 children.
					this.nodeCount++;
					BTNode newParent = new BTNode(0, this.nodeCount, this.nodeSize, false, true); //since new parent required, will definitely be a root node.
					LNode.parent = newParent;
					inNode.parent = newParent;
					inNode.root = false;
					this.root = newParent;
					insertNode(newParent, inNode.keyArray[0], LNode, inNode);
					
					//NEW CODE - re-wire leaf nodes so the end of one leaf node points to the next leaf node
					int k = findPointer(newParent, LNode.keyArray[0]);
					if(k > 0)
						newParent.childNodeArray[k - 1].leafData[this.nodeSize].next = LNode.leafData[0];
					
				}
				
			}
			//Full Non-Leaf Node Insertion
			//Couple of key differences:
			//1 - leaf nodes are 1 to 1 between keyArray and leafData array.  Index nodes are not since they have 2 children including overlap with adjacent keys.
			//	  So, we can shuffle keys and childNodes just like we did above, but when transferring keys to an LNode, if we transfer n keys, we must transfer n+1 children.
			//    After collapsing inNode, this will leave the first childNode in inNode as null.  Pushing up the first key and collapsing again as well as inserting new child nodes will take care of this issue.
			//2 - Since we are inserting into an index node, we can guarantee that this call is the result of a split in a node one level down.  As such, what was previously
			//    one child node has become 2 and the insertNode call will include the LChild and RChild parameters.  After using findPointer to get an i value and splitting the nodes, 
			//    LChild will be inserted in index i and RChild into index i+1.
			//3 - Instead of 2, we will have 3 cases to deal with since we have to put LChild and RChild in the right locations and since we are pushing up the middle key.
			//    A) If i is in the middle index (i.e. = nodeSize / 2 for evens, nodeSize / 2 + 1 for odds) then LChild goes into the first available index of LNode after splitting and RChild goes into the first index of inNode after splitting and collapsing.
			//       In this case the key is not inserted into either node but is pushed up with a new insert call.
			//    B) if i is less than the middle then the key will go into the left node.  Proceed as above but push up middle key.
			//    C) If i is greater than the middle index the key will go into the right node.  Proceed as above but  push up middle key.
			//Note:  The term "middle index" indicates the result from findPointer meaning the key would be inserted into inNode[0] after splitting, assuming a leaf node (copy up vs. push up)
			else if(!inNode.leaf)
			{
				this.nodeCount++;
				BTNode LNode = new BTNode(0, this.nodeCount, this.nodeSize, false, false);
				LNode.parent = inNode.parent;
				
				//Case 1 - i represents the middle index of the keyArray, key will not be inserted into either node but will be pushed up.
				//         Transfer the first (nodeSize + 1) / 2 nodes to LNode.
				if( this.nodeSize%2 == 0 && i == this.nodeSize / 2 || this.nodeSize%2 != 0 && i == (this.nodeSize / 2) + 1 )
				{
					//Transfer values below middle index to LNode
					for(int j = 0; j < (this.nodeSize + 1) / 2; j++)
					{
						LNode.keyArray[j] = new String(inNode.keyArray[j]); //Transfer key to LNode
						inNode.keyArray[j] = null; //Erase key from inNode
						LNode.childNodeArray[j] = inNode.childNodeArray[j]; //Transfer left child node down
						LNode.childNodeArray[j].parent = LNode; //Transfer parentage along with child nodes
						inNode.childNodeArray[j] = null; //Erase child from inNode
						LNode.keyCount++;
						inNode.keyCount--;
					}
					//Then we need just need to collapse inNode, and overwrite LNode.childArray[keyCount] with LChild and inNode.childArray[0] with RChild and our nodes are split!
					inNode.collapse();
					LNode.childNodeArray[LNode.keyCount] = LChild;
					LNode.childNodeArray[LNode.keyCount].parent = LNode;
					inNode.childNodeArray[0] = RChild;
					inNode.childNodeArray[0].parent = inNode;
					
					//Lastly we will call the recursive insert for this case.  Key = key, LChild = LNode, RChild = inNode.
					if(inNode.parent != null)
						insertNode(inNode.parent, key, LNode, inNode);
					if(inNode.parent == null)
					{
						//Note: This only happens when splitting a root node, requiring the creation of a new root node for the resulting 2 children.
						this.nodeCount++;
						BTNode newParent = new BTNode(0, this.nodeCount, this.nodeSize, false, true); //since new parent required, will definitely be a root node.
						LNode.parent = newParent;
						inNode.parent = newParent;
						inNode.root = false;
						this.root = newParent;
						insertNode(newParent, key, LNode, inNode);
					}
				}
				
				//Case 2 - i is less than middle index of keyArray.  Key will be inserted into left node.
				else if( this.nodeSize%2 == 0 && i < this.nodeSize / 2 || this.nodeSize%2 != 0 && i < (this.nodeSize / 2) + 1 )
				{
					//Transfer values to LNode.  One less value than in previous case since we will insert key into LNode.
					for(int j = 0; j < ((this.nodeSize + 1) / 2) - 1; j++)
					{
						LNode.keyArray[j] = new String(inNode.keyArray[j]); //Transfer key to LNode
						inNode.keyArray[j] = null; //Erase key from inNode
						LNode.childNodeArray[j] = inNode.childNodeArray[j]; //Transfer left child node down
						LNode.childNodeArray[j].parent = LNode;
						inNode.childNodeArray[j] = null; //Erase child from inNode
						LNode.keyCount++;
						inNode.keyCount--;
					}
					//Now collapse inNode, then we'll push up the key in inNode.keyArray[0] while updating child nodes
					inNode.collapse(); //childNodeArray[0] now needs to be transferred to LNode.childNodeArray[keyCount]
					LNode.childNodeArray[LNode.keyCount] = inNode.childNodeArray[0];
					LNode.childNodeArray[LNode.keyCount].parent = LNode;
					inNode.childNodeArray[0] = null; //erase this child node.  Will be fixed when pushing up first key in inNode.
					String parentKey = new String(inNode.keyArray[0]); //Will use this on recursive insertNode call after finalizing LNode and inNode.
					inNode.keyArray[0] = null;
					inNode.collapse();
					inNode.keyCount--;
					
					//Now we need to insert the key and child nodes into LNode.
					insertNode(LNode, key, LChild, RChild);
					
					//Lastly, call our recursive insert to push up parentKey
					if(inNode.parent != null)
						insertNode(inNode.parent, parentKey, LNode, inNode);
					if(inNode.parent == null)
					{
						//Note: This only happens when splitting a root node, requiring the creation of a new root node for the resulting 2 children.
						this.nodeCount++;
						BTNode newParent = new BTNode(0, this.nodeCount, this.nodeSize, false, true); //since new parent required, will definitely be a root node.
						LNode.parent = newParent;
						inNode.parent = newParent;
						inNode.root = false;
						this.root = newParent;
						insertNode(newParent, parentKey, LNode, inNode);
					}
				}
				
				//Case 3 - Key gets inserted into right node (inNode) but would not end up in keyArray[0] (middle index, pushed up, see Case 1).
				else
				{
					//Transfer values below middle index to LNode
					for(int j = 0; j < (this.nodeSize + 1) / 2; j++)
					{
						LNode.keyArray[j] = new String(inNode.keyArray[j]); //Transfer key to LNode
						inNode.keyArray[j] = null; //Erase key from inNode
						LNode.childNodeArray[j] = inNode.childNodeArray[j]; //Transfer left child node down
						LNode.childNodeArray[j].parent = LNode;
						inNode.childNodeArray[j] = null; //Erase child from inNode
						LNode.keyCount++;
						inNode.keyCount--;
					}
					//Then collapse inNode, transfer inNode.childNode[0] over to LNode prior to pushing up key in keyArray[0], then insert key into inNode
					inNode.collapse();
					LNode.childNodeArray[LNode.keyCount] = inNode.childNodeArray[0]; //transfer child to LNode
					LNode.childNodeArray[LNode.keyCount].parent = LNode;
					inNode.childNodeArray[0] = null; // erase child from inNode.
					insertNode(inNode, key, LChild, RChild);
					
					//Now we can push up the key in inNode.keyArray[0].
					String parentKey = new String(inNode.keyArray[0]);
					inNode.keyArray[0] = null;
					inNode.keyCount--;
					inNode.collapse();
					
					//push up
					if(inNode.parent != null)
						insertNode(inNode.parent, parentKey, LNode, inNode);
					if(inNode.parent == null)
					{
						//Note: This only happens when splitting a root node, requiring the creation of a new root node for the resulting 2 children.
						this.nodeCount++;
						BTNode newParent = new BTNode(0, this.nodeCount, this.nodeSize, false, true); //since new parent required, will definitely be a root node.
						LNode.parent = newParent;
						inNode.parent = newParent;
						inNode.root = false;
						this.root = newParent;
						insertNode(newParent, parentKey, LNode, inNode);
					}
					
				}
			}
		}
	}
	
	//Method to print all the words in the tree in alpha order.
	public void printWords()
	{
		BTNode leaf = this.root; //This node will traverse down the tree to the leaf node with the first words
		while(!leaf.leaf)
		{
			leaf = leaf.childNodeArray[0];
		}//After this, leaf is at the leaf node with smallest vals
		
		//Now print all the words using the linked nodes (not BTNodes, see Node class)
		Node cur = leaf.leafData[0]; //i.e. the first word stored here
		while(cur != null) //Null when we have walked off the bottom level of the tree
		{
			if(cur.value != null) //This ensures we don't try to print a value from the nodeSize+1'st node since that is just used to connect to next leaf and stores no records
			{
				System.out.println(cur.value + ", " + cur.frequency);
			}
			cur = cur.next;
		}
	}
	
	//Display tree structure using only NodeID's
	public void printTreeIDs(BTNode cur) //First call should be on this.root
	{
		String space = "    "; //this will be used to control spacing in printed output
		int numParents = 0; //Used to determine how many times to "space" results
		
		//Stop condition - at a leaf node
		if(cur.leaf)
		{
			//Determine spacing
			BTNode temp = cur;
			while(temp.parent != null)
			{
				numParents++;
				temp = temp.parent;
			}
			
			//Start building output to print, starting with proper spacing.
			String out = "";
			for(int i = 0; i < numParents; i++)
				out += space;
			
			//Add nodeID to print to out string and print results, includinga newline.
			out += cur.nodeID;
			System.out.println(out);			
		}
		
		
		//Main case, not a leaf node.  Print node ID, then recursively call method for all children
		else
		{
			//Determine spacing
			BTNode temp = cur;
			while(temp.parent != null)
			{
				numParents++;
				temp = temp.parent;
			}
			
			//Start building output to print, starting with proper spacing.
			String out = "";
			for(int i = 0; i < numParents; i++)
				out += space;
			
			//Add nodeID to print to out string and print results, includinga newline.
			out += cur.nodeID;
			System.out.println(out);
			
			//Lastly, recursively call method on all children
			for(int i = 0; i <= cur.keyCount; i++)
			{
				printTreeIDs(cur.childNodeArray[i]);
			}				
		}
	}
	
	//Search a word.  Return index in leaf node that stores the value, else return -1.
	public int contains(String key)
	{
		int res = -1;
		
		//Locate leaf that would contain key
		BTNode leaf = findLeaf(this.root, key);
		
		//Check if leaf does contain key
		for(int j = 0; j < leaf.keyCount; j++)
		{
			if(key.equals(leaf.keyArray[j]))
				res = j;
		}
		
		return res;
	}
	
	//Print all values between a given range, inclusive
	public void printRange(String lower, String upper)
	{
		BTNode lowerLeaf = findLeaf(this.root, lower);
		int i = findPointer(lowerLeaf, lower);
		if(this.contains(lower) > -1)
			i = this.contains(lower);
		
		//Now read all values starting from i in lowerLeaf and going to the next node to read until the next nodes value is > upper
		Node node = lowerLeaf.leafData[i];
		while(node != null && (node.value == null ||  node.value.compareTo(upper) < 1) )
		{
			if(node.value != null)
				System.out.println(node.value + ", " + node.frequency);
				node = node.next;			
		}
	}
	
	//Print a particular node identified by nodeID
	public void printNode(BTNode inNode, int nodeID) //First call is at root
	{
		if(nodeID == inNode.nodeID)
		{
			System.out.println("Node ID: " + inNode.nodeID);
			if(inNode.leaf)
			{
				for(int i = 0; i < inNode.keyCount; i++)
					System.out.print(inNode.keyArray[i] + ", " + inNode.leafData[i].frequency + "; ");
			}

			
			else //i.e. !inNode.leaf
			{
				for(int i = 0; i < inNode.keyCount; i++)
					System.out.print(inNode.keyArray[i] + ", ");
			}
			System.out.println();
		}
		
		//recursive search for correct node
		else if(nodeID != inNode.nodeID && !inNode.leaf)
		{
			for(int i = 0; i <= inNode.keyCount; i++)
			{
				printNode(inNode.childNodeArray[i], nodeID);
			}
		}
	}
}
