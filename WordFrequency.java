import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Scanner;

public class WordFrequency {
	
	public static int menu(Scanner kb)
	{
		int res = -10;
		
		while(res < -1 || res > 6)
		{
			System.out.println("Please select from the following:");
			System.out.println("1. Print the words in the tree.");
			System.out.println("2. Display tree structure using BTNode ID's");
			System.out.println("3. Select a node to display.");
			System.out.println("4. Insert a word.");
			System.out.println("5. Search for a word.");
			System.out.println("6. Perform a range search.");
			System.out.println("-1. Quit program.");
			res = Integer.parseInt(kb.nextLine());			
		}
		
		return res;
	}

	public static void main(String[] args) throws Exception{
		
		BPlusTree tree = new BPlusTree(3);
		HashTable stopWords = new HashTable();
		Scanner kb = new Scanner(System.in);
		
		//Process stopwords file into the hash table
		//File in = new File(args[1]);
		File in = new File("stopwords.txt");
		Scanner inStops = new Scanner(in);
		while(inStops.hasNext())
		{
			String word = inStops.nextLine();
			stopWords.insert(word, word);
		}
		inStops.close();
		
		//Process input file of words into the tree, excluding those in stopWords hash table
		//File startWords = new File(args[0]);
		//Input text file is just a written document.  Need to separate into words then input to tree
		File startWords = new File("StartWords2.txt");
		Scanner onFile = new Scanner(startWords);
		String wholeFile = "";
		
		//Process file into separate words.
		//First read file into one large string
		while(onFile.hasNext())
			wholeFile += onFile.nextLine();
		
		//Now split string on whitespace to get array of words to insert.		
		String[] startWordsArray = wholeFile.split("\\s+");
		
		//Now insert all the words in the array into the tree.
		for(int i = 0; i < startWordsArray.length; i++)
		{
			if( !stopWords.contains(startWordsArray[i].trim().replaceAll("\\p{Punct}", "")) )
				tree.insertWord(tree.root, startWordsArray[i].trim().replaceAll("\\p{Punct}", "").toLowerCase());
		}

		
		//Now run the menu and give the user options.
		int menu = -10;
		while(menu != -1)
		{
			menu = menu(kb);
			
			if(menu == 1) //Print all words in alpha order
				tree.printWords();
			else if(menu == 2) //Print tree structure using nodeIDs
				tree.printTreeIDs(tree.root);
			else if(menu == 3) //Print a particular node
			{
				System.out.println("Enter the numerical ID for the node to display.");
				int ID = Integer.parseInt(kb.nextLine());
				tree.printNode(tree.root, ID);
			}
			else if(menu == 4) //Insert a new word
			{
				System.out.println("Type in word to insert.");
				String word = kb.nextLine();
				if(!stopWords.contains(word))
					tree.insertWord(tree.root, word.toLowerCase());
			}
			else if(menu == 5) //Search a word
			{
				System.out.println("Enter word to search.");
				String word = kb.nextLine();
				if(tree.contains(word) < 0)
					System.out.println("Word not in tree.");
				else
				{
					BTNode leaf = tree.findLeaf(tree.root, word);
					int i = tree.contains(word);
					System.out.println(leaf.leafData[i].value + ", " + leaf.leafData[i].frequency);
				}
			}
			else if(menu == 6) //Range search
			{
				System.out.println("Type in lower bound only.");
				String lower = kb.nextLine();
				System.out.println("Type in upper bound only.");
				String upper = kb.nextLine();
				tree.printRange(lower, upper);
			}
		}
		
		
		//////////////// Testing/Experiments ///////////////////
//		 URL url;
//		 InputStream is = null;
//		 BufferedReader br;
//		 String line;
//		 
//		 url = new URL("http://www.stackoverflow.com/");
//	     is = url.openStream();  // throws an IOException
//	     br = new BufferedReader(new InputStreamReader(is));
//
//	     while ((line = br.readLine()) != null) 
//	    	 System.out.println(line);
		
		//TEST - Split leaf node, new key to right node - insert a, b, c, d
//		BPlusTree tree = new BPlusTree(3);
//		tree.insertWord(tree.root, "a");
//		System.out.println(tree.root.toString());
//		
//		tree.insertWord(tree.root, "b");
//		System.out.println(tree.root.toString());
//		
//		tree.insertWord(tree.root,  "c");
//		System.out.println(tree.root.toString());
//		
//		tree.insertWord(tree.root, "d");
//		System.out.println(tree.root.toString()); // PASS
		
		
		//TEST - Split leaf node, new to to left node - insert b, c, d, a
//		BPlusTree tree = new BPlusTree(3);
//		tree.insertWord(tree.root, "b");
//		System.out.println(tree.root.toString());
//		
//		tree.insertWord(tree.root, "c");
//		System.out.println(tree.root.toString());
//		
//		tree.insertWord(tree.root,  "d");
//		System.out.println(tree.root.toString());
//		
//		tree.insertWord(tree.root, "a");
//		System.out.println(tree.root.toString());  // PASS
		
		//TEST - split leaf node with parent, new node to right node, 
		//       after the above insertions, insert e, f
//		tree.insertWord(tree.root, "e");
//		System.out.println(tree.root.toString());
//		tree.insertWord(tree.root, "f");
//		System.out.println(tree.root.toString()); //PASS
		
		//TEST - split leaf node with parent, new node to left node
		//       insert c, d, e, f, b, a
//		tree.insertWord(tree.root, "c");
//		tree.insertWord(tree.root, "d");
//		tree.insertWord(tree.root, "e");
//		tree.insertWord(tree.root, "f");
//		tree.insertWord(tree.root, "b");
//		tree.insertWord(tree.root, "a");
//		System.out.println(tree.root.toString()); // PASS
		
		//TEST CHECKPOINT FOR INDEX NODE TESTS
		//The following insertions prep a tree for a series of index node insertions
//		tree.insertWord(tree.root, "b");
//		tree.insertWord(tree.root, "d");
//		tree.insertWord(tree.root, "f");
//		tree.insertWord(tree.root, "h");
//		tree.insertWord(tree.root, "j");
//		tree.insertWord(tree.root, "l");
//		System.out.println(); // PASS - Parents good up through here
		
		//TEST - Insert values such that an index node is split and the
		//	inserted key ends up in the right node.
//		tree.insertWord(tree.root, "n"); //Parents good
//		tree.insertWord(tree.root, "p"); //Parents good
//		tree.insertWord(tree.root, "r"); //Parents good
//		tree.insertWord(tree.root, "t"); //Good to here again
//		System.out.println(); // PASS
		
		//TEST - Insert values such that an index node is split and the
		//	inserted key ends up in the left node.
//		tree.insertWord(tree.root, "g"); 
//		tree.insertWord(tree.root, "i"); 
//		tree.insertWord(tree.root, "a"); 
//		tree.insertWord(tree.root, "c"); 
//		System.out.println(); // PASS
		
		//TEST - Insert values such that an index node is split and the
		//	inserted key ends is pushed up to the next level up.
//		tree.insertWord(tree.root, "b"); 
//		tree.insertWord(tree.root, "f"); 
//		tree.insertWord(tree.root, "j"); 
//		tree.insertWord(tree.root, "h"); 
//		tree.insertWord(tree.root, "z"); 
//		tree.insertWord(tree.root, "s"); 
//		tree.insertWord(tree.root, "v"); 
//		tree.insertWord(tree.root, "x"); 
//		tree.insertWord(tree.root, "t");
//		tree.insertWord(tree.root, "u"); 
		//System.out.println(); //PASS
		
		//TEST - contains
		//System.out.println(tree.contains("c"));
		
		//TEST - insert duplicate word, check frequency
		//tree.insertWord(tree.root, "b"); 
		//System.out.println(); //PASS
		
		//TEST - printWords (in alpha order)
		//tree.printWords(); //PASS
		
		//TEST - printRange
		//tree.printRange("f", "w"); //PASS
		
		//TEST - print tree structure using print tree ID's
		//tree.printTreeIDs(tree.root);
		//System.out.println();
	

	}
	
}
