import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import com.sun.tools.doclets.formats.html.SourceToHTMLConverter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** CompactPrefixTree class, implements Dictionary ADT and
 *  several additional methods. Can be used as a spell checker.
 *  Fill in code in the methods of this class. You may add additional methods. */
public class CompactPrefixTree implements Dictionary {

    private Node root; // the root of the tree

    /** Default constructor.
     * Creates an empty "dictionary" (compact prefix tree).
     * */
    public CompactPrefixTree(){
        root = new Node();
    }

    /**
     * Creates a dictionary ("compact prefix tree")
     * using words from the given file.
     * @param filename the name of the file with words
     */
    public CompactPrefixTree(String filename) {
        root = new Node();
        try {
            File file = new File(filename);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String word = reader.readLine();
            while(word != null) {
                this.add(word);
                word = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Adds a given word to the dictionary.
     * @param word the word to add to the dictionary
     */
    public void add(String word) {
        add(word.toLowerCase(), root); // Calling private add method
    }

    /**
     * Checks if a given word is in the dictionary
     * @param word the word to check
     * @return true if the word is in the dictionary, false otherwise
     */
    public boolean check(String word) {
        return check(word.toLowerCase(), root); // Calling private check method
    }

    /**
     * Checks if a given prefix is a prefix of any word stored in the dictionary
     * @param prefix The prefix of a word
     * @return true if the prefix is a prefix of any word in the dictionary, false otherwise
     */
    public boolean checkPrefix(String prefix) {
        return checkPrefix(prefix.toLowerCase(), root); // Calling private checkPrefix method
    }


    /**
     * Prints all the words in the dictionary, in alphabetical order,
     * one word per line.
     */
    public void print() {
        print("", root); // Calling private print method
    }

    /**
     * Print out the nodes of the compact prefix tree, in a pre-order fashion.
     * First, print out the root at the current indentation level
     * (followed by * if the node's valid bit is set to true),
     * then print out the children of the node at a higher indentation level.
     */
    public void printTree() {
        printTree("", this.root);
    }

    private void printTree(String empty, Node node) {
        if(node == null) return;
        if(node.isWord) {
            System.out.println(empty + node.prefix + "*");
        } else {
            System.out.println(empty + node.prefix);
        }
        for(Node child: node.children) {
            printTree(empty + "  ", child);
        }
    }

    /**
     * Print out the nodes of the tree to a file, using indentations to specify the level
     * of the node.
     * @param filename the name of the file where to output the tree
     */
    public void printTree(String filename) {
        Path outpath = Paths.get(filename);
        try (BufferedWriter output = Files.newBufferedWriter(outpath)){
            output.write(buildTree("", this.root, new StringBuilder()));
        }  catch(IOException e) {
            e.getMessage();
        }
    }





    /**
     * Return an array of the entries in the dictionary that are as close as possible to
     * the parameter word.  If the word passed in is in the dictionary, then
     * return an array of length 1 that contains only that word.  If the word is
     * not in the dictionary, then return an array of numSuggestions different words
     * that are in the dictionary, that are as close as possible to the target word.
     * Implementation details are up to you, but you are required to make it efficient
     * and make good use ot the compact prefix tree.
     *
     * @param word The word to check
     * @param numSuggestions The length of the array to return.  Note that if the word is
     * in the dictionary, this parameter will be ignored, and the array will contain a
     * single world.
     * @return An array of the closest entries in the dictionary to the target word
     */

    public String[] suggest(String word, int numSuggestions) {
        return this.suggest(word, numSuggestions, this.root, "");
    }

    // ---------- Private helper methods ---------------

    /**
     * This method will find a certain number of closest word of given word, but note that it is not
     * perfect, some bug might occur if the tree do not have as many word as we want to find
     * @param word
     * @param numSuggestions
     * @param node
     * @param currentPrefix: We sum up the currentPrefix to form the corresponding word
     * @return An array of suggested word
     */
    private String[] suggest(String word, int numSuggestions, Node node, String currentPrefix) {
        //Base case 1: If word is a word, we return that word
        if(this.check(currentPrefix + word)) {
            String[] result = new String[1];
            result[0] = currentPrefix + node.prefix;
            return result;

        } else if(word.equals(node.prefix) || !this.comPrefix(word, node.prefix).equals(node.prefix)) {
            //Base case 2: If word matches the current node's prefix, but it is not a word
            // || Base Case 3:
            // If the maximum common prefix does not match the node's prefix
            //Or the word is not a word
            //We suggest prefix + ClosestSuffix;
            //We advance the node
            String[] result = new String[numSuggestions];
            //Find certain number of suggested word
            for (int i = 0; i < numSuggestions; i++) {
                if (this.findTheClosestSuffix(node, result, currentPrefix) != null)
                    result[i] = currentPrefix + this.findTheClosestSuffix(node, result, currentPrefix);
                else
                    //If the findTheClosestSuffix return null, it means there is no more similar word under this node
                    //So we deduct the last letter of the word, and pass it to the suggest function again
                    return suggest(word.substring(0, word.length() - 2), numSuggestions, root, "");
            }
            return result;
        }

        //Other wise, the prefix of the node does match the word's prefix(Not whole word). We advance the node
        String suffix = this.suffix(word, node.prefix);
        Node child = node.children[this.getIndexOfCharacter(suffix)];
        //Advance to corresponding child node, if the child node is null
        if(child == null) {
            String[] result = new String[numSuggestions];

            for(int i = 0; i < numSuggestions; i++) {
                if(this.findTheClosestSuffix(node, result, currentPrefix) != null)
                    result[i] = currentPrefix + this.findTheClosestSuffix(node, result, currentPrefix);
                else
                    return suggest(word.substring(0, word.length() - 2), numSuggestions, root, "");
            }
            return result;
        }
        //Advance to next level
        return this.suggest(suffix, numSuggestions, child, currentPrefix += node.prefix);//Update the current prefix
    }

    /**
     * This method finds a closest suffix from the node or it's child which does not duplicate the suffix in rec.
     * If could not find, the method returns null
     * @param node
     * @param rec
     * @return
     */
    private String findTheClosestSuffix(Node node, String[] rec, String currentPrefix) {
        //If index == 0; indicates that there is no replicated word in suggestion array
        if(node.isWord && !this.recommended(rec, currentPrefix + node.prefix)) {
            return node.prefix;
        }
        Node child;
        currentPrefix += node.prefix;
        //Search for 26 child for the closest word
        for(int i = 0; i < 26; i++) {
            child = node.children[i];
            if(child != null && findTheClosestSuffix(child, rec, currentPrefix) != null)
                return node.prefix + findTheClosestSuffix(child, rec, currentPrefix);
        }
        //If we can not find a new closest suffix, we return null, and the suggest method will
        //search the suggest words again with the search word input deducted the last letter.
        return null;
    }

    /**This method return true if the word is already in String array rec, otherwise return false
     *
     * @param rec
     * @param word
     * @return
     */
    private boolean recommended(String[] rec, String word) {
        for(String elem: rec) {
            if(word.equals(elem)) return true;
        }
        return false;
    }


    /**
     *  A private add method that adds a given string to the tree
     * @param s the string to add
     * @param node the root of a tree where we want to add a new string

     * @return a reference to the root of the tree that contains s
     */
    private Node add(String s, Node node) {
        //Base case, if the node is null
        if(node == null) {
            node = new Node();
            node.addPrefix(s);//node.prefix = s;
            node.isWord();//node.isWord = true
            return node;
        }

        //Base case, if the prefix matches s
        if(s.equals(node.prefix)) {
            node.isWord();//Make this node's isWord true
            return node;
        }

        //If the prefix of this node match the prefix of s
        if(this.comPrefix(node.prefix, s).equals(node.prefix)) {
            String suffix = s.substring(node.prefix.length());
            int index = suffix.charAt(0) - 'a';
            Node child = node.children[index];
            node.addChild(add(suffix, child));
            return node;
        }
        //Base Case
        //If the prefix of this node can not match prefix
        String comm = this.comPrefix(node.prefix, s);
        //Create a new node with maximum common prefix
        Node insert = new Node();
        insert.addPrefix(comm);
        //Break this node's prefix and suffix, update the old node's prefix with suffix
        node.addPrefix(node.prefix.substring(comm.length()));
        //Create a new node with s's suffix, and isWord
        Node endS = new Node();
        endS.addPrefix(s.substring(comm.length()));
        endS.isWord();
        //Add endS and node to insert
        insert.addChild(endS);
        insert.addChild(node);
        //return the insert
        return insert;
    }

    /**
     *
     * @param s
     * @return
     */
    private int getIndexOfCharacter(String s) {
        return s.charAt(0) - 'a';
    }

    /**
     *This method return s's suffix (Reduced the common prefix with prefix)
     * @param s
     * @param prefix
     * @return s's suffix
     */
    private String suffix(String s, String prefix) {
        return s.substring(comPrefix(s, prefix).length());
    }

    /**
     * This method find the maximum common prefix of two words
     * @param s1 the first string to check
     * @param s2 the second string of a tree
     * @return The String of the maximum common prefix
     */
    public String comPrefix(String s1, String s2) {
        int i = 0;
        while(i < s1.length() && i < s2.length() && s1.charAt(i) == s2.charAt(i)) {
            i++;
        }
        return s1.substring(0, i);
    }


    /**
     *
     * @param empty
     * @param node
     * @param result
     * @return The String representing the tree
     */
    private String buildTree(String empty, Node node, StringBuilder result) {
        if(node == null) return "";
        if(node.isWord) {
            result.append(empty + node.prefix + "*\n");
        } else {
            result.append(empty + node.prefix + "\n");
        }
        for(Node child: node.children) {
            buildTree(empty + "  ", child, result);
        }
        return result.toString();
    }

    /** A private method to check whether a given string is stored in the tree.
     *
     * @param s the string to check
     * @param node the root of a tree
     * @return true if the prefix is in the dictionary, false otherwise
     */
    private boolean check(String s, Node node) {
        //If the tree is empty,then the word is not in the tree
        if(node == null) return false;
        //If the prefix stored at the root of the tree is not a prefix of the word,the word is
        //not in the tree
        if(!this.comPrefix(s, node.prefix).equals(node.prefix))
            return false;
        if(node.prefix.equals(s)) {
            return node.isWord;
        }
        if(this.comPrefix(s, node.prefix).equals(node.prefix)) {
            // If the common prefix equals to this node prefix
            String suffix = suffix(s, node.prefix);
            return check(suffix, node.children[this.getIndexOfCharacter(suffix)]);//Check next level
        }
        return false;
    }

    /**
     * A private recursive method to check whether a given prefix is in the tree
     *
     * @param prefix the prefix
     * @param node the root of the tree
     * @return true if the prefix is in the dictionary, false otherwise
     */
    private boolean checkPrefix(String prefix, Node node) {
        //If the tree is empty,then the word is not in the tree
        if(node == null) return false;
        //If find the prefix, return true.
        if(prefix.equals(this.comPrefix(prefix, node.prefix))) {
            return true;
        }
        if(this.comPrefix(prefix, node.prefix).equals(node.prefix)) {
            // If the common prefix equals to this node prefix
            String suffix = suffix(prefix, node.prefix);
            return checkPrefix(suffix, node.children[this.getIndexOfCharacter(suffix)]);//Check next level
        }
        return false;
    }

    /**
     * Outputs all the words stored in the dictionary
     * to the console, in alphabetical order, one word per line.
     * @param s the string obtained by concatenating prefixes on the way to this node
     * @param node the root of the tree
     */
    private void print(String s, Node node) {
        //Base Case: If isWord, print its word
        if(node == null) return;
        if(node.isWord) System.out.println(s + node.prefix);
        int i = 0;
        for(Node child: node.children) {
            if(child != null) {
                print(s + node.prefix, child);
            }
            i++;
        }
    }

    // FILL IN CODE: add a private suggest method. Decide which parameters
    // it should have

    // --------- Private class Node ------------
    // Represents a node in a compact prefix tree
    private class Node {
        String prefix; // prefix stored in the node
        Node children[]; // array of children (26 children)
        boolean isWord; // true if by concatenating all prefixes on the path from the root to this node, we get a valid word

        Node() {
            isWord = false;
            prefix = "";
            children = new Node[26]; // initialize the array of children
        }

        /**
         * Add a child to current node
         * @param node
         */
        public void addChild(Node node) {
            int index = getIndexOfCharacter(node.prefix);
            this.children[index] = node;
        }

        /**
         * Add prefix
         * @param s
         */
        public void addPrefix(String s) {
            this.prefix = s;
        }

        /**
         * Turn isWord to true
         */
        public void isWord() {
            this.isWord = true;
        }
    }

    //Personal small test cases.
    public static void main(String[] args) {
        CompactPrefixTree a = new CompactPrefixTree();
        System.out.println("apple and ape common prefix is: " + a.comPrefix("apple", "ape"));
        a.add("apple");
        a.add("ape");
        a.add("able");
        a.add("zebra");
        a.add("cart");
        a.add("cat");
        a.add("cats");
        a.print();
        System.out.println(a.check("zebra"));
        a.printTree("you.txt");
        System.out.println(a.check("z"));
        CompactPrefixTree c = new CompactPrefixTree();
        System.out.println("Hey: ");
        CompactPrefixTree b = new CompactPrefixTree("words_ospd.txt");
        System.out.println(b.checkPrefix("aa"));

    }

}
