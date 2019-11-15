package uk5;

/**
 * Created by mahdi on 2018-11-28.
 */


import org.apache.commons.lang.StringUtils;
import util.Utils;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.util.*;

/**
 * A Generalized Suffix Tree, based on the Ukkonen's paper "On-line construction of suffix trees"
 * http://www.cs.helsinki.fi/u/ukkonen/SuffixT1withFigs.pdf
 *
 * Allows for fast storage and fast(er) retrieval by creating a tree-based index out of a set of strings.
 * Unlike common suffix trees, which are generally used to build an index out of one (very) long string,
 * a Generalized Suffix Tree can be used to build an index over many strings.
 *
 * Its main operations are put and search:
 * Put adds the given key to the index, allowing for later retrieval of the given value.
 * Search can be used to retrieve the set of all the values that were put in the index with keys that contain a given input.
 *
 * In particular, after put(K, V), search(H) will return a set containing V for any string H that is substring of K.
 *
 * The overall complexity of the retrieval operation (search) is O(m) where m is the length of the string to search within the index.
 *
 * Although the implementation is based on the original design by Ukkonen, there are a few aspects where it differs significantly.
 *
 * The tree is composed of a set of nodes and labeled edges. The labels on the edges can have any length as long as it's greater than 0.
 * The only constraint is that no two edges going out from the same node will start with the same character.
 *
 * Because of this, a given (startNode, stringSuffix) pair can denote a unique path within the tree, and it is the path (if any) that can be
 * composed by sequentially traversing all the edges (e1, e2, ...) starting from startNode such that (e1.label + e2.label + ...) is equal
 * to the stringSuffix.
 * See the search method for details.
 *
 * The union of all the edge labels from the root to a given leaf node denotes the set of the strings explicitly contained within the GST.
 * In addition to those Strings, there are a set of different strings that are implicitly contained within the GST, and it is composed of
 * the strings built by concatenating e1.label + e2.label + ... + $end, where e1, e2, ... is a proper path and $end is prefix of any of
 * the labels of the edges starting from the last node of the path.
 *
 * This kind of "implicit path" is important in the testAndSplit method.
 *
 */
public class SubstringMaxSearch {

    /**
     * The index of the last item that was added to the GST
     */
    private int last = 0;
    /**
     * The root of the suffix tree
     */
    private final Node root = new Node();
    /**
     * The last leaf that was added during the update operation
     */
    private Node activeLeaf = root;

    /**
     * Searches for the given word within the GST.
     *
     * Returns all the indexes for which the key contains the <tt>word</tt> that was
     * supplied as input.
     *
     * @param word the key to search for
     * @return the collection of indexes associated with the input <tt>word</tt>
     */
//    public Collection<Integer> search(String word) {
//        return search(word, -1);
//    }

    /**
     * Searches for the given word within the GST and returns at most the given number of matches.
     *
     * @param word the key to search for
    //     * @param results the max number of results to return
     * @return at most <tt>results</tt> values for the given word
     */
//    public ArrayList<String> search1(String word, int position) {
//        ArrayList<String> suffPosition =new ArrayList<>();
//        Node tmpNode = searchNode(word);
//        if(tmpNode == null){return suffPosition;}
//        if (tmpNode.getSuffPosition().size() > 0){
//            for (String s: tmpNode.getSuffPosition()){
//                Integer pos = Integer.parseInt(s.split(";")[1]);
//                if(pos == position){
//                    suffPosition.add(s.split(";")[0]);
//                }
//            }
//        }
//        if(tmpNode.getEdges().size() > 0){
//           getSuffPositions(tmpNode,position);
//        }
//        for (int i = 0; i < suffPos.size(); i++) {
//            suffPosition.add(suffPos.get(i));
//        }
//        suffPos.clear();
//        return suffPosition;
//    }


    public ArrayList<String> encryptedSearch(String word, String position, String key) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        ArrayList<String> suffPosition =new ArrayList<>();
        Pair<Node, Integer> matchedNode = encryptedSearchNode(word, key);
        Node tmpNode = matchedNode.getFirst();
        int matchCount = matchedNode.getSecond();
//        position = Integer.toBinaryString(Integer.parseInt(position));
        if(tmpNode == null){return suffPosition;}
        if (tmpNode.getSuffPosition().size() > 0){
            for (String s: tmpNode.getSuffPosition()){
                String encryptedSeqNum = s.split(":")[0].split(";")[0];
                String encryptedSuffPosition = s.split(":")[0].split(";")[1];
//                int suffPositionLength = encryptedSuffPosition.length();
//                int queryPosLength = position.length();
//                String decryptionKey = "";
//                if(suffPositionLength != queryPosLength){
//                    if(suffPositionLength < queryPosLength){
//                        encryptedSuffPosition = StringUtils.leftPad(encryptedSuffPosition, queryPosLength, "0");
//                        decryptionKey = key.substring(0,queryPosLength);
//                    }else{
//                        position = StringUtils.leftPad(position, suffPositionLength, "0");
//                        decryptionKey = key.substring(0,suffPositionLength);
//                    }
//                }
//                CheckEquality eq = new CheckEquality();
                Boolean positionFlag = false;
//                if(queryPosLength == suffPositionLength){
//                    positionFlag = eq.encryptedPositionCheck(position, encryptedSuffPosition, decryptionKey);
//                }
                if(position.equals(encryptedSuffPosition)){
                    positionFlag = true;
                }
                if (positionFlag) {
                    suffPosition.add(encryptedSeqNum+";"+matchCount);
                }
            }
        }
        if(tmpNode.getEdges().size() > 0){
            getSuffPositions(tmpNode,position,key,matchCount);
        }
        for (int i = 0; i < suffPos.size(); i++) {
            suffPosition.add(suffPos.get(i));
        }
        suffPos.clear();
        return suffPosition;
    }

    ArrayList<String> suffPos =new ArrayList<>();

    public void getSuffPositions(Node tmpNode, String position, String key, int matchCount) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        //going to check children up to leaf node recursively
        Map<Character, Edge> edges = tmpNode.getEdges();
        int numberOfChild = edges.size();
        for (int i = 0; i < numberOfChild; i++) {
            Edge edge = (Edge) edges.values().toArray()[i];
            Node dest = edge.getDest();
            if (dest.getSuffPosition().size() > 0){
                for (String s: dest.getSuffPosition()){
                    String encryptedSeqNum = s.split(":")[0].split(";")[0];
                    String encryptedSuffPosition = s.split(":")[0].split(";")[1];
//                    int suffPositionLength = encryptedSuffPosition.length();
//                    int queryPosLength = position.length();
//                    String decryptionKey = key.substring(0,suffPositionLength);
//                    if(suffPositionLength != queryPosLength){
//                        if(suffPositionLength < queryPosLength){
//                            encryptedSuffPosition = StringUtils.leftPad(encryptedSuffPosition, queryPosLength, "0");
//                            decryptionKey = key.substring(0,queryPosLength);
//                        }else{
//                            position = StringUtils.leftPad(position, suffPositionLength, "0");
//                            decryptionKey = key.substring(0,suffPositionLength);
//                        }
//                    }
//                    CheckEquality eq = new CheckEquality();
                    Boolean positionFlag = false;
//                    if(queryPosLength == suffPositionLength){
//                        positionFlag = eq.encryptedPositionCheck(position, encryptedSuffPosition, decryptionKey);
//                    }
                    if(position.equals(encryptedSuffPosition)){
                        positionFlag = true;
                    }
                    if (positionFlag) {
                        suffPos.add(encryptedSeqNum+";"+matchCount);
                    }
                }
            }
            getSuffPositions(dest, position, key,matchCount);
            if (i==numberOfChild-1){return;}
        }
    }

    public void sufPosUpdate(String s, Integer seqNum){
        String suf = "";
        for (int i = s.length(); i > 0 ; --i) {
            suf = s.substring(i-1);
            Node srchNode = searchNode(suf);
            String suffPos = Integer.toString(seqNum)+";"+ Integer.toString(i);
//            int seqLength = Integer.toString(seqNum).length();
//            int suffPosLength = Integer.toString(i).length();
            srchNode.setSuffPosition(suffPos);
//            srchNode.setSuffPosition(suffPos+":"+Integer.toString(seqLength)+","+Integer.toString(suffPosLength));
        }
    }
    public Collection<Integer> search(String word) {
        return search(word, -1);
    }

    private void encryptTree(Node startNode, SecretKey secKey, IvParameterSpec ivSpec) throws Exception {
        Map<Character, Edge> edges = startNode.getEdges();
        int numberOfChild = edges.size();
        for (int i = 0; i < numberOfChild; i++) {
            Edge edge = (Edge) edges.values().toArray()[i];
            String encryptedVal = new String (AESCTR.encryptText(edge.getLabel(), secKey, ivSpec));
            edge.setLabel(encryptedVal);
            Node dest = edge.getDest();
            ArrayList<String> tempSuffPosition =new ArrayList<>();
            if (dest.getSuffPosition().size() > 0){
                for (String s: dest.getSuffPosition()){
                    tempSuffPosition.add(s);
                }
                for (int j = 0; j < tempSuffPosition.size(); j++) {
                    String encryptedSuffPos = new String (AESCTR.encryptText(tempSuffPosition.get(j), secKey, ivSpec));
                    dest.removeSuffPosition(tempSuffPosition.get(j));
                    dest.setSuffPosition(encryptedSuffPos);
                }
            }

            if (dest.getEdges().size() == 0) {
                return;
            }else{encryptTree(dest, secKey, ivSpec);}
//            if (i==numberOfChild-1){return;}
        }
    }

    private void encryptTree_(Node startNode, String key) {
        Map<Character, Edge> edges = startNode.getEdges();
        int numberOfChild = edges.size();
        for (int i = 0; i < numberOfChild; i++) {
            Edge edge = (Edge) edges.values().toArray()[i];
            String modifiedKey = key.substring(0,edge.getDest().getDataLength());
            boolean[] keyBoolean = Utils.fromStringOriginal(modifiedKey);
            boolean[] labelBool = Utils.fromStringOriginal(edge.getLabel());
            boolean[]  encryptedSeq = new boolean[keyBoolean.length];
            String encryptedVal = "";
            for (int j = 0; j < keyBoolean.length; j++){
                encryptedSeq[j] = keyBoolean[j]^labelBool[j%labelBool.length];//IV introduce
                encryptedVal +=encryptedSeq[j]?"1":"0";
            }
//            String label = edge.getLabel();
//            StringBuilder encryptedSeq = new StringBuilder();
//            for(int j = 0; j < modifiedKey.length(); j++)
//                encryptedSeq.append((char)(modifiedKey.charAt(j) ^ label.charAt(j % label.length())));
//            String encryptedVal = encryptedSeq.toString();
            edge.setLabel(encryptedVal);
            Node dest = edge.getDest();
//            ArrayList<String> tempSuffPosition =new ArrayList<>();
//            if (dest.getSuffPosition().size() > 0){
//                for (String s: dest.getSuffPosition()){
//                    tempSuffPosition.add(s);
//                }
//                for (int j = 0; j < tempSuffPosition.size(); j++) {
////                    String seqAndSuffPosLength = tempSuffPosition.get(j).split(":")[1];
//                    String sequenceNumber = tempSuffPosition.get(j).split(";")[0];
//                    String suffPos = tempSuffPosition.get(j).split(":")[0].split(";")[1];
//                    boolean[] seqNumBool = Utils.fromStringOriginal(Integer.toBinaryString(Integer.parseInt(sequenceNumber)));
//                    boolean[] seqNumKeyBool = Utils.fromStringOriginal(key.substring(0,seqNumBool.length));
//                    //encryption of the sequence number
//                    encryptedSeq = new boolean[seqNumKeyBool.length];
//                    encryptedVal = "";
//                    for (int l = 0; l < seqNumKeyBool.length; l++){
//                        encryptedSeq[l] = seqNumKeyBool[l]^seqNumBool[l%seqNumBool.length];//IV introduce
//                        encryptedVal +=encryptedSeq[l]?"1":"0";
//                    }
//                    String encryptedSeqNumber = encryptedVal;
//                    //encryption of the suffix position
//                    boolean[] suffixPosBool = Utils.fromStringOriginal(Integer.toBinaryString(Integer.parseInt(suffPos)));
//                    boolean[] suffPosKeyBool = Utils.fromStringOriginal(key.substring(0,suffixPosBool.length));
//                    encryptedSeq = new boolean[suffPosKeyBool.length];
//                    encryptedVal = "";
//                    for (int l = 0; l < suffPosKeyBool.length; l++){
//                        encryptedSeq[l] = suffPosKeyBool[l]^suffixPosBool[l%suffixPosBool.length];//IV introduce
//                        encryptedVal +=encryptedSeq[l]?"1":"0";
//                    }
//                    String encryptedSuffPos = encryptedVal;
//                    dest.removeSuffPosition(tempSuffPosition.get(j));
//                    dest.setSuffPosition(encryptedSeqNumber+";"+encryptedSuffPos);
////                    dest.setSuffPosition(encryptedSeqNumber+";"+encryptedSuffPos+":"+seqAndSuffPosLength);
//                }
//            }

            if (dest.getEdges().size() == 0) {
                return;
            }else{encryptTree_(dest, key);}
//            if (i==numberOfChild-1){return;}
        }
    }

    /**
     * Searches for the given word within the GST and returns at most the given number of matches.
     *
     * @param word the key to search for
     * @param results the max number of results to return
     * @return at most <tt>results</tt> values for the given word
     */
    public Collection<Integer> search(String word, int results) {
        Node tmpNode = searchNode(word);
        if (tmpNode == null) {
            return Collections.EMPTY_LIST;
        }
        return tmpNode.getData(results);
    }

    /**
     * Searches for the given word within the GST and returns at most the given number of matches.
     *
     * @param word the key to search for
     * @param to the max number of results to return
     * @return at most <tt>results</tt> values for the given word
     */
    public ResultInfo searchWithCount(String word, int to) {
        Node tmpNode = searchNode(word);
        if (tmpNode == null) {
            return new ResultInfo(Collections.EMPTY_LIST, 0);
        }

        return new ResultInfo(tmpNode.getData(to), tmpNode.getResultCount());
    }

    /**
     * Returns the tree node (if present) that corresponds to the given string.
     */
    private Node searchNode(String word) {
        /*
         * Verifies if exists a path from the root to a node such that the concatenation
         * of all the labels on the path is a superstring of the given word.
         * If such a path is found, the last node on it is returned.
         */
        Node currentNode = root;
        Edge currentEdge;

        for (int i = 0; i < word.length(); ++i) {
            char ch = word.charAt(i);
            // follow the edge corresponding to this char
            currentEdge = currentNode.getEdge(ch);
            if (null == currentEdge) {
                // there is no edge starting with this char
                return null;
            } else {
                String label = currentEdge.getLabel();
                int lenToMatch = Math.min(word.length() - i, label.length());
                if (!word.regionMatches(i, label, 0, lenToMatch)) {
                    // the label on the edge does not correspond to the one in the string to search
                    return null;
                }

                if (label.length() >= word.length() - i) {
                    return currentEdge.getDest();
                } else {
                    // advance to next node
                    currentNode = currentEdge.getDest();
                    i += lenToMatch - 1;
                }
            }
        }

        return null;
    }

    private Pair<Node, Integer> encryptedSearchNode(String query, String key) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        /*
         * Verifies if exists a path from the root to a node such that the concatenation
         * of all the labels on the path is a superstring of the given word.
         * If such a path is found, the last node on it is returned.
         */
        Node currentNode = root;
        Edge currentEdge;
        int matchCount = 0;
        Node lastMatched = new Node();
        CheckEquality eq = new CheckEquality();

        for (int i = 0; i < query.length(); ++i) {
            char ch = query.charAt(i);
            // follow the edge corresponding to this char
            currentEdge = currentNode.getEdge(ch);
            if (null == currentEdge) {// there is no edge starting with this char
                if(matchCount > 0){
                    return new Pair<Node, Integer>(lastMatched,matchCount);
                }
                return null;
            } else {
                String encryptedLabel = currentEdge.getLabel();
                String finalQuery;
                int labelLength = currentEdge.getDest().getDataLength();
                if (labelLength > query.substring(i).length()){
//                    System.out.println("Original label length is greater than the query!!!");
                    encryptedLabel = encryptedLabel.substring(0,query.substring(i).length());
                    finalQuery = query.substring(i);
                }
                else {
                    finalQuery = query.substring(i,i+labelLength);
                }
//                String finalQuery = "";
//                for (int j = i; j < i+labelLength; j++) {
//                    finalQuery += query.charAt(j);
//                }
                int lenToMatch = finalQuery.length();
                String decryptionKey = key.substring(0,lenToMatch);
                eq.create_socket();
                int match = eq.encryptLabelMatches(finalQuery, encryptedLabel, decryptionKey,i==query.length()-1);
                eq.close_socket();
                matchCount += match;
//                if (!word.regionMatches(i, label, 0, lenToMatch)) {
                if ((match == 0 || match < lenToMatch) && matchCount >0) {
                    // the label on the edge does not correspond to the one in the string to search
                    return new Pair<Node, Integer>(currentEdge.getDest(),matchCount);
                }
                else{
                    if (labelLength >= (query.length() - i)) {
                        return new Pair<Node, Integer>(currentEdge.getDest(),matchCount);
                    } else {
                        // advance to next node
                        currentNode = currentEdge.getDest();
                        lastMatched = currentNode;
                        i += lenToMatch - 1;
                    }
                }
            }
        }
//        eq.close_socket();
        return null;
    }

    private void datalengthUpdate(Node startNode) {
        Map<Character, Edge> edges = startNode.getEdges();
        int numberOfChild = edges.size();
        for (int i = 0; i < numberOfChild; i++) {
            Edge edge = (Edge) edges.values().toArray()[i];
            int dataLen = edge.getLabel().length();
            Node dest = edge.getDest();
            dest.setDataLength(dataLen);
            if (dest.getEdges().size() == 0) {
                return;
            }else{datalengthUpdate(dest);}
        }
    }


    /**
     * Adds the specified <tt>index</tt> to the GST under the given <tt>key</tt>.
     *
     * Entries must be inserted so that their indexes are in non-decreasing order,
     * otherwise an IllegalStateException will be raised.
     *
     * @param key the string key that will be added to the index
     * @param index the value that will be added to the index
     * @throws IllegalStateException if an invalid index is passed as input
     */
    public void put(String key, int index) throws IllegalStateException {
        if (index < last) {
            throw new IllegalStateException("The input index must not be less than any of the previously inserted ones. Got " + index + ", expected at least " + last);
        } else {
            last = index;
        }

        // reset activeLeaf
        activeLeaf = root;

        String remainder = key;
        Node s = root;

        // proceed with tree construction (closely related to procedure in
        // Ukkonen's paper)
        String text = "";
        // iterate over the string, one char at a time
        for (int i = 0; i < remainder.length(); i++) {
            // line 6
            text += remainder.charAt(i);
            // use intern to make sure the resulting string is in the pool.
            text = text.intern();

            // line 7: update the tree with the new transitions due to this new char
            Pair<Node, String> active = update(s, text, remainder.substring(i), index);
            // line 8: make sure the active pair is canonical
            active = canonize(active.getFirst(), active.getSecond());

            s = active.getFirst();
            text = active.getSecond();
        }

        // add leaf suffix link, is necessary
        if (null == activeLeaf.getSuffix() && activeLeaf != root && activeLeaf != s) {
            activeLeaf.setSuffix(s);
        }

    }

    /**
     * Tests whether the string stringPart + t is contained in the subtree that has inputs as root.
     * If that's not the case, and there exists a path of edges e1, e2, ... such that
     *     e1.label + e2.label + ... + $end = stringPart
     * and there is an edge g such that
     *     g.label = stringPart + rest
     *
     * Then g will be split in two different edges, one having $end as label, and the other one
     * having rest as label.
     *
     * @param inputs the starting node
     * @param stringPart the string to search
     * @param t the following character
     * @param remainder the remainder of the string to add to the index
     * @param value the value to add to the index
     * @return a pair containing
     *                  true/false depending on whether (stringPart + t) is contained in the subtree starting in inputs
     *                  the last node that can be reached by following the path denoted by stringPart starting from inputs
     *
     */
    private Pair<Boolean, Node> testAndSplit(final Node inputs, final String stringPart, final char t, final String remainder, final int value) {
        // descend the tree as far as possible
        Pair<Node, String> ret = canonize(inputs, stringPart);
        Node s = ret.getFirst();
        String str = ret.getSecond();

        if (!"".equals(str)) {
            Edge g = s.getEdge(str.charAt(0));

            String label = g.getLabel();
            // must see whether "str" is substring of the label of an edge
            if (label.length() > str.length() && label.charAt(str.length()) == t) {
                return new Pair<Boolean, Node>(true, s);
            } else {
                // need to split the edge
                String newlabel = label.substring(str.length());
                assert (label.startsWith(str));

                // build a new node
                Node r = new Node();
                // build a new edge
                Edge newedge = new Edge(str, r);

                g.setLabel(newlabel);

                // link s -> r
                r.addEdge(newlabel.charAt(0), g);
                s.addEdge(str.charAt(0), newedge);

                return new Pair<Boolean, Node>(false, r);
            }

        } else {
            Edge e = s.getEdge(t);
            if (null == e) {
                // if there is no t-transtion from s
                return new Pair<Boolean, Node>(false, s);
            } else {
                if (remainder.equals(e.getLabel())) {
                    // update payload of destination node
                    e.getDest().addRef(value);
                    return new Pair<Boolean, Node>(true, s);
                } else if (remainder.startsWith(e.getLabel())) {
                    return new Pair<Boolean, Node>(true, s);
                } else if (e.getLabel().startsWith(remainder)) {
                    // need to split as above
                    Node newNode = new Node();
                    newNode.addRef(value);

                    Edge newEdge = new Edge(remainder, newNode);

                    e.setLabel(e.getLabel().substring(remainder.length()));

                    newNode.addEdge(e.getLabel().charAt(0), e);

                    s.addEdge(t, newEdge);

                    return new Pair<Boolean, Node>(false, s);
                } else {
                    // they are different words. No prefix. but they may still share some common substr
                    return new Pair<Boolean, Node>(true, s);
                }
            }
        }

    }

    /**
     * Return a (Node, String) (n, remainder) pair such that n is a farthest descendant of
     * s (the input node) that can be reached by following a path of edges denoting
     * a prefix of inputstr and remainder will be string that must be
     * appended to the concatenation of labels from s to n to get inpustr.
     */
    private Pair<Node, String> canonize(final Node s, final String inputstr) {

        if ("".equals(inputstr)) {
            return new Pair<Node, String>(s, inputstr);
        } else {
            Node currentNode = s;
            String str = inputstr;
            Edge g = s.getEdge(str.charAt(0));
            // descend the tree as long as a proper label is found
            while (g != null && str.startsWith(g.getLabel())) {
                str = str.substring(g.getLabel().length());
                currentNode = g.getDest();
                if (str.length() > 0) {
                    g = currentNode.getEdge(str.charAt(0));
                }
            }

            return new Pair<Node, String>(currentNode, str);
        }
    }

    /**
     * Updates the tree starting from inputNode and by adding stringPart.
     *
     * Returns a reference (Node, String) pair for the string that has been added so far.
     * This means:
     * - the Node will be the Node that can be reached by the longest path string (S1)
     *   that can be obtained by concatenating consecutive edges in the tree and
     *   that is a substring of the string added so far to the tree.
     * - the String will be the remainder that must be added to S1 to get the string
     *   added so far.
     *
     * @param inputNode the node to start from
     * @param stringPart the string to add to the tree
     * @param rest the rest of the string
     * @param value the value to add to the index
     */
    private Pair<Node, String> update(final Node inputNode, final String stringPart, final String rest, final int value) {
        Node s = inputNode;
        String tempstr = stringPart;
        char newChar = stringPart.charAt(stringPart.length() - 1);

        // line 1
        Node oldroot = root;

        // line 1b
        Pair<Boolean, Node> ret = testAndSplit(s, tempstr.substring(0, tempstr.length() - 1), newChar, rest, value);

        Node r = ret.getSecond();
        boolean endpoint = ret.getFirst();

        Node leaf;
        // line 2
        while (!endpoint) {
            // line 3
            Edge tempEdge = r.getEdge(newChar);
            if (null != tempEdge) {
                // such a node is already present. This is one of the main differences from Ukkonen's case:
                // the tree can contain deeper nodes at this stage because different strings were added by previous iterations.
                leaf = tempEdge.getDest();
            } else {
                // must build a new leaf
                leaf = new Node();
                leaf.addRef(value);
                Edge newedge = new Edge(rest, leaf);
                r.addEdge(newChar, newedge);
            }

            // update suffix link for newly created leaf
            if (activeLeaf != root) {
                activeLeaf.setSuffix(leaf);
            }
            activeLeaf = leaf;

            // line 4
            if (oldroot != root) {
                oldroot.setSuffix(r);
            }

            // line 5
            oldroot = r;

            // line 6
            if (null == s.getSuffix()) { // root node
                assert (root == s);
                // this is a special case to handle what is referred to as node _|_ on the paper
                tempstr = tempstr.substring(1);
            } else {
                Pair<Node, String> canret = canonize(s.getSuffix(), safeCutLastChar(tempstr));
                s = canret.getFirst();
                // use intern to ensure that tempstr is a reference from the string pool
                tempstr = (canret.getSecond() + tempstr.charAt(tempstr.length() - 1)).intern();
            }

            // line 7
            ret = testAndSplit(s, safeCutLastChar(tempstr), newChar, rest, value);
            r = ret.getSecond();
            endpoint = ret.getFirst();

        }

        // line 8
        if (oldroot != root) {
            oldroot.setSuffix(r);
        }
        oldroot = root;

        return new Pair<Node, String>(s, tempstr);
    }

    Node getRoot() {
        return root;
    }

    private String safeCutLastChar(String seq) {
        if (seq.length() == 0) {
            return "";
        }
        return seq.substring(0, seq.length() - 1);
    }

    public int computeCount() {
        return root.computeAndCacheCount();
    }

    /**
     * An utility object, used to store the data returned by the GeneralizedSuffixTree GeneralizedSuffixTree.searchWithCount method.
     * It contains a collection of results and the total number of results present in the GST.
     */
    public static class ResultInfo {

        /**
         * The total number of results present in the database
         */
        public int totalResults;
        /**
         * The collection of (some) results present in the GST
         */
        public Collection<Integer> results;

        public ResultInfo(Collection<Integer> results, int totalResults) {
            this.totalResults = totalResults;
            this.results = results;
        }
    }

    /**
     * A private class used to return a tuples of two elements
     */
    private class Pair<A, B> {

        private final A first;
        private final B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        public A getFirst() {
            return first;
        }

        public B getSecond() {
            return second;
        }
    }
    public static void main(String[] args) throws Exception{
        SubstringMaxSearch in = new SubstringMaxSearch();
        //------------------------------------------suffix tree building-------------------------------------------
        Date date = new Date();
        List<String> treeSeqList = new ArrayList();
        Scanner fileScanner = new Scanner(new File("shimizu_data_2184.txt"));
//        Scanner fileScanner = new Scanner(new File("STSeq_row1000_col1000.txt"));
//        Scanner fileScanner = new Scanner(new File("suffixTreeSeq.txt"));
        while (fileScanner.hasNextLine()) {
//            while (fileScanner.hasNextLine() && j <800) {
            String s = fileScanner.nextLine();
            treeSeqList.add(s);
        }
        fileScanner.close();
        for (int i = 0; i < treeSeqList.size(); ++i) {//2184
//        for (int i = 0; i < 150; ++i) {
            System.out.println("processing... " + (i+1));
            in.put(treeSeqList.get(i), (i+1));
            in.sufPosUpdate(treeSeqList.get(i), (i+1));
        }
//        in.put("0000000000000000000000101000000100000000000010000100001101000100000000000000000000010010101000010000000000100001000000010000000010011000101000000000000000010000000001010000100010000000010000000001000000101000000010100100000000000001000100100001000000000000000000000101000000000000000000001000001000001000000010000000000000000000000001010001000000000000010000000000000001000000001000101000000000000000100010000000000000000000000000000000000110000000000100010000001000000101000100100001101000000001100000001000000010001010000000000000000000000000000000000001000100000001000100001000000011000000000010000000000000010000000010000000000000010000000000000000001000000000010000100011100100000101000000000001100000000010000011000100000010000010001000000000000000000000010000000000000000010000000000110000001000000000000000000000100000000001000100010000010000000000000000010100000000010000000010010010010000000000010000000000010000000010000100000100000000000000000000000000010001010011000000000000000010000010",10000);
//        in.put("100010",1);
//        in.sufPosUpdate("0000000000000000000000101000000100000000000010000100001101000100000000000000000000010010101000010000000000100001000000010000000010011000101000000000000000010000000001010000100010000000010000000001000000101000000010100100000000000001000100100001000000000000000000000101000000000000000000001000001000001000000010000000000000000000000001010001000000000000010000000000000001000000001000101000000000000000100010000000000000000000000000000000000110000000000100010000001000000101000100100001101000000001100000001000000010001010000000000000000000000000000000000001000100000001000100001000000011000000000010000000000000010000000010000000000000010000000000000000001000000000010000100011100100000101000000000001100000000010000011000100000010000010001000000000000000000000010000000000000000010000000000110000001000000000000000000000100000000001000100010000010000000000000000010100000000010000000010010010010000000000010000000000010000000010000100000100000000000000000000000000010001010011000000000000000010000010",10000);
//        in.sufPosUpdate("100010",1);
//        in.put("000110010",1);
//        in.put("100011100",2);
//        in.put("010110011",3);
//        in.sufPosUpdate("000110010",1);
//        in.sufPosUpdate("100011100",2);
//        in.sufPosUpdate("010110011",3);
//        in.put("010", 2);
//        in.sufPosUpdate("010",2);
//        in.put("111", 3);
//        in.sufPosUpdate("111",3);
        in.datalengthUpdate(in.getRoot());
        Date date1 = new Date();
        System.out.println("Tree building time: " + (date1.getTime() - date.getTime()));
        date = new Date();
        String strKey = "00001101110000010100000100001010100110101101111010001100101011100000111101010010000101001001110000011011110001110010010000011011101100111010001100010110111110010111010010110010110110110001000110101110000111011001111001011000111111001100100100101100000100011000110101001100011000111111110000000001010111001101011101010001110010010110101011001001001100111011001010001001100101101100110111010011000110110000010100010000011010101000100100111011010111101010100100110101000001101000011110011001111000110011100110011010100010101101110010000001000111010101011101011101011110100110100111011110101010100010101101111101110011101000101001101000001010010110011101110011010111010001001001000001011010001001010010110101100110001110001101001100111110100110010101100001000011010111111100001000111011100011001000011111110101100011010000101111000111111110110101110100100011101101010001111111111010001000110011100001110001010001001001100111000100111010100011110101010001111011011010101101111001000001001001101101011011100000010101010110000000011001000111111011000100000101010011011100000000110101100000111001111011010011000001001011101100101111000110011100111111011100100001110110001010100100000110000010100110110101010011100111000001100110101101110010010011001101010111001101101101101011111100100010101100001001110001000000100111101110100000011011111101101101000010001010101101110100100000101111110011000101101111111101011001110011110111000100110100111101111010100101110100110101101101110001001000010010111001011101101101010011001011001001100011000001110000101010111001001001111101100110001100111101100111101101011000101011110001101000010110001100010110001100011100110111000110011100101110100011100101101000000110011001110001000110001111001100000010110110011011011011111110000000110101111000010101011110001001100000000101100101110000100111000100011001000001110001111101101000010010010011111110101011010110001101101011101110111100100101100101110111001111000101100110011110000001000100110110111010101100011010100100000011101000111011101101010000110111110101110011001011101001100101110100110110001011111011111101101110110101010001011110100110001001110110110010110010001011100111000000000001010110001110001001101000001010010000010011101100001100111011111111000011010001010110011101001000101010100010001001001100011010111110011101110100110010101010100110110010001100011011010011100001101011111110111000100001101001110101011001010011000000001001011001000100011001110010111000111100001001010010110001011100111011100100110001110010011010111100000011110010001100111000010110110111000110010111001010110101001101011001001010011011101001111100101101101000010010100101001100010000101111010011011000101100011000010101101011010110100000101111110010000100010111110010000000110111101011101011100101011011000100111000111000100010001111110110001001000001000100101011011010110110000100011101101100110011010100001111001010101110011010110110111000111010000100101110010001100010100001011101110101101011001100101011000111010111110001101010011100000000011011111001101000101111111001011011101111001111011011111000110011101101011011101010010001011011000001001101010110001110111111100100101000110011110100011000101010001101101011100110001010000001110001011010000110100101100011011110010111000110110110001101000100010100100000101100011110011001111000101110010001101000100111101100011010111101110001010011001010101000010101110101011011100111100000011000000110100001011100101001011011011110110001000010001011111100001101101011110100101010010100010010111101001001100101010010010101111100101111000011011011111100000000100100100000000011100001110110001100101001110011010111110100011100011010101000111001111100110000111011010010111000000100000010000111000000110001001011000111100000001101111001111011010010000110101001001010010011010110101000111110101111010100010010101111101001011010000010000100101100011011000010111111100110110011100110111001101000111111011110011010110000101101100000011010000001100110110100011100011101000100000111110111111101111001000011001111101000001001111111000011000101110110000101011111010100000011100010100000011111100011110111010100100011011010111100101111000001000011100101111000000110110010000010110011000100100001001001001110111000111101100000111010100011111111110000111011011011000000010100001101101010000011001011101010010100010110101011100100111011010000000011111010001010010100011101000000111000101110000110010010010001101101100110011111010000001000111011101101011010110101110011000010111001000000011110110011100111000011000110010110110100111111011010000011111010000111100011000101110000010101010000001010100100101100000110001001010011001111010101000111000000000101111010010111001101000101111010011101011000101111111101001010011110001011001111000001000011110011110010011001110000101010011000101100000100001111001000101011010100100101110010001000101111001000111001001101111101001101100001101100011001111101010101001011010111110001000110101100011011110111100000000001000011110001110011111110110000001100110111000111100001";
//        String strKey = "1110011101010100000000100111110011000101000110010010110111001101100000000010101110011000110110110100000111011001101100110011100001010100101001010100110010010010111001110100011010010011001111001101011100001100000011000110011100100011001101011010011011001011110111011110110100111100110111101011011011100100011100001100001110110110011001001111111000101001010001010011101101111010111101100001000000010000100001011001000011001001100111100000111100000000001100110000010100001100011110001010011000100001001100100100001101000111011011111010001011111001100000101010011011111101101111001010001110110010000001010011111111111100010100101000110111100111111101101100000001010011010111111110000111101001101011100010101100000101101010010000110010000001011011011101110010011100001111010001000100101011001111111100000001001111010000100100101111110011010001111001001101001100111010001001011010110110001110001001110001110011010101010000001001110101111101011101000111011110111000110010001011110010111011100001110001100100";
        //----------------------------------------------encryption of the suffix tree-----------------------------
        in.encryptTree_(in.getRoot(),strKey);
//        in.encryptTree(in.getRoot(),secKey,ivSpec);
        date1 = new Date();
        System.out.println("Tree encryption time: " + (date1.getTime() - date.getTime()));
        //---------------------------------------------secure search using GC-------------------------------------
        date = new Date();
//        ArrayList<String> encryptedResult = in.encryptedSearch("1000000000010000000000000000001000000000000000100011100100000101000000000001100000000010000011000100000010000010001000001000000000000000010000000000000000010000000000110000001000000000000000000000100000000001000100010000010000000001000000010100000000010000000010010010010000000000010000000000000000000010000100000100000000000000000000100000010001000011000000000000000010000000000000000001000010000100110010010000000100000000100000000000010100000000010010000000001000000000110001101000000110000001001100011001100101010000000000100001000000000010000101000000000000000000010000000000000000000000011000000000001000000000000000000000000000000001000000011000000000000000000010000100111010100001001000000100000000000101010100000000010000001000000000000000010100110010011000000010000110001010000010000000000000000000100100010010000100100000000000000110000000000100001000000000000000010000000000001100000100000010000000100000100100001100000000010000010000010000000000000000000000001000000000000100110000000000000010101001000000000000000000000000001000000001000010000100010000000010010000000100100000001000010010000000100001000000000100001000000001001101100110000000000101000000001000000000000011010000000110000001000000001001000000001100000000000000101000000001010000100000000000001100000000000001100000000000000000011000001001001000000001000000000000010000000100000000001100000100000000000000000001001000000000000000000000000000100100010000000010000000100000000000000100100000001001011010000000000000010000100000000000101000010000001000000100001001001000000000000010000010000000000001000000100000000000001000100000000010000100001010000100000000000000000010000000100000000000000010000000001001110100000011010001000010000100000010000000000000001000000000100000000000000100100000010000000001000001000000000000000000000000000000000000000000000000000001000000001000000000000001100000101000000100000000001000000010000000100000000000001000000000001101000000000000000000000000101000000000000100000000010010011010000000001000000000101110110101000000000100000100100010000100100000000000010000000000000000010000000000000010100010000001001000000000000011000100000000000000000000000001000000000000000001000110100000000000000000000000001110010000000000001011010100000000000001000000000000000000000000000100000001101001011000000100000000000000000000000100100000001010000000000000001000000000100000001000000011000010000111101000100100010000001000000100000010000001001001001000000000000000001000000000000000001010000100010100000001001100011000001001100000000000000000111000001010000000000000000000000000100000000000000001000000000000000001000000000001001000000000010000000001101100001000000000000000010001010010110000000000001000010000010000000000000100000000010000100000000010000011000001001010000010000000010000100000000000000000110100000000000000000010010000000000000000000010001000100000100000011000100000000000000000001010000001000100011001000000000000000000000000000000010000010000000000000000000000100000001000000001000000000001000000000100000000000000000010001000000000100010010000000000000000010001000100000000010000000010000000001000000000010001000000011010011011000101010100010000000000000000000000000001000100000000000010000000000000000000000000000010100000000000000000100000000001000000000000011000000100001000010000000000000100000000000000000000000000000000000000100000000000000000010000000000110000000100000010000000000000010000000000001000000000000000000000000000000000000001000000100000000000000000000010010001010000000000001000000000000000010100010000000000010000000000010000001001000100000000000011100100001000111010110000000000000000100000010000000000000000000000001100110100001010000000010001000001000000100000000010000000000000000000000001000000000100000001000000000100000001100000000010000000000001000000000000000000000000100010010000000000000000001000001000000000010000000000100100101000110001000000110000110001000000000000000100000101000001000001000000010100110000000000000100010000000000000000010110000100010010100000000000000000000000000000000000000000000000000000000000000101010000000000000000000010110000000000100000000000000000000010010000100010000000001100010001000000000000000000000000100000000000000000000011000010010000000000000000101","625", strKey);
//        ArrayList<String> encryptedResult = in.encryptedSearch("10000100000000000100000000100100000000000010010000101011000000100001000000000100011000100000000100001000000000000000001000000000000000100","64", strKey);
//        String query = "0000000000000001000000101";
        String query = "000000011000100001001000";
//        String query = "1000110";
        ArrayList<String> encryptedResult = in.encryptedSearch(query,"1", strKey);
//        ArrayList<String> encryptedResult = in.encryptedSearch("10000000000110000001000000000000000000000100000000001","780", strKey);
        System.out.println("Total result found: " + encryptedResult.size());
        date1 = new Date();
        System.out.println("Searching time: " + (date1.getTime() - date.getTime())/1000 + " seconds");
        //---------------------------------------------decryption of the result-----------------------------------
        ArrayList<String> decryptedResult = new ArrayList<>();
        for (String s: encryptedResult){
            String encryptedSeqNum = s.split(";")[0];
            int matchCount = Integer.parseInt(s.split(";")[1]);
//            String decryptionKey = strKey.substring(0,encryptedSeqNum.length());
//            boolean[] keyBoolean = Utils.fromStringOriginal(decryptionKey);
//            boolean[] labelBool = Utils.fromStringOriginal(encryptedSeqNum);
//            boolean[]  decryptedSeq = new boolean[keyBoolean.length];
//            String decryptedVal = "";
//            for (int j = 0; j < keyBoolean.length; j++){
//                decryptedSeq[j] = keyBoolean[j]^labelBool[j%labelBool.length];//IV introduce
//                decryptedVal += decryptedSeq[j]?"1":"0";
//            }
//            String decryptedSeqNum = String.valueOf(Integer.parseInt(decryptedVal,2));
            decryptedResult.add("Sequence No: " + encryptedSeqNum + ", Match length: " + matchCount + ", Matched portion: " + query.substring(0,matchCount) + "\n");
//            decryptedResult.add("Sequence No: " + decryptedSeqNum + ", Match length: " + matchCount + ", Matched portion: " + query.substring(0,matchCount) + "\n");
//            System.out.println("After decryption: "+ decryptedSeqNum);

//            System.out.println(decryptedVal);
        }
        System.out.println("===========================result====================================");
        System.out.println("Query: " + query + "\nResult: \n");
        for (int i = 0; i < decryptedResult.size(); i++) {
            System.out.println(decryptedResult.get(i));
        }
        //-------------------------------communication overhead-----------------------------
        System.out.println("Total circuit used: " + CheckEquality.circuitCount + " and communication overhead: " + CheckEquality.circuitCount * 128 * 2 + " MB");
    }
}

