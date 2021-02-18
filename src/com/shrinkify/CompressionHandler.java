package com.shrinkify;

import com.shrinkify.util.ArrayUtil;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;
import java.io.*;
import java.util.regex.Pattern;

public class CompressionHandler implements Runnable{

    /*private int var;

    public MyRunnable(int var) {
        this.var = var;
    }*/
    public static String loadpath, savepath;

    public enum ProcessType{
        Encode,
        Decode
    }
    public static ProcessType type;


    public void run() {
        switch (type){
            case Encode:
                RunEncode();
                break;
            case Decode:
                RunDecode();
                break;
        }
    }


    Map<Character,Node> charNodeRefDict = new HashMap<>();

    private class Node{
        public Node parentNode;
        public Node leftNode;
        public Node rightNode;
        public int frequency;
        public char character;

        public Node(Node parentNode,Node leftNode,Node rightNode,int frequency,char character) {
            this.parentNode = parentNode;
            this.leftNode = leftNode;
            this.rightNode = rightNode;
            this.frequency = frequency;
            this.character = character;
        }

        public Boolean IsLeafNode()
        {
            if (leftNode == null & rightNode == null){
                return true;
            }
            return false;
        }
    };

    public void RunEncode(){
        String str = LoadFile();
        Map<Character,Integer> dict = CreateDict(str);
        Map<Character,Integer> savedDict = ArrayUtil.CopyDict(dict);
        int[] sorted_array = SortDict(dict);
        Node tree = CreateTree(sorted_array,dict);
        EncodeData(tree,str,savedDict);
    }
    public void RunDecode(){
        DecodeData();
    }

    private Map<Character,Integer> CreateDict(String _str){
        GUI.Log("Creating character Dictionary...");
        long startTime = System.nanoTime();

        String[] charArray = _str.split("", -1);
        Map<Character,Integer> charDict = new HashMap<>();
        int time = 0;
        for (String str : charArray)
        {
            ProgressBar(time, charArray.length);
            if (str.length() == 0){
                break;
            }
            char _char = str.charAt(0);
            if (charDict.containsKey(_char)){
                int val = charDict.get(_char);
                val = val+1;
                charDict.put(_char,val);
            }else{
                charDict.put(_char,1);
            }
            time++;
        }
        GUI.Log("Character Dictionary successfully created. Took " + (System.nanoTime() - startTime)/1000000  +" milliseconds.");
    return charDict;
    }

    private int[] SortDict(Map<Character,Integer> dict){
        GUI.Log("Sorting character dictionary...");
        long startTime = System.nanoTime();
        //Create array from dictionary key values
        int[] array = new int[dict.size()];
        int i = 0;
        for (Integer _int : dict.values())
        {
            ProgressBar(i,dict.size());
            array[i] = _int;
            i++;
        }
        //sort array
        ArrayUtil.QuickSort(array,0,array.length-1);
        GUI.Log("Successfully sorted. Took " + (System.nanoTime() - startTime)/1000000  +" milliseconds.");
        return array;
    }


    private Node CreateTree(int[] array, Map<Character,Integer> charDict){
        GUI.Log("Creating Binary Tree..");
        long startTime = System.nanoTime();
        List<Character> charDeleteBuffer= new ArrayList<Character>();
        List<Node> nodePool = new ArrayList<>();
        //Covert array into list of nodes
        int time = 0;

        for (int _int : array)
        {
            ProgressBar(time,array.length);
            for (Character _char : charDict.keySet())
            {
                int frequency = charDict.get(_char);
                if (frequency == _int) {
                    char character = _char;
                    //Create node object
                    Node newNode = new Node(null, null, null, frequency, character);
                    //Add node object to object pool, don't forget swimming trunks!
                    nodePool.add(newNode);
                    charDeleteBuffer.add(_char);
                    //save reference for quick searching later
                    charNodeRefDict.put(character,newNode);
                    break;
                }
            }
            //Delete traversed characters to prevent repeats
            for (Character _char : charDeleteBuffer) {
                charDict.remove(_char);
            }
            //clean up buffer
            charDeleteBuffer.clear();
            time++;
        }
        time = 0;
        while (nodePool.size()>1) {
            ProgressBar(time,nodePool.size());
            //Create tree with every pair of values
            Node childNode1 = nodePool.get(0);
            Node childNode2 = nodePool.get(1);
            int freq = childNode1.frequency + childNode2.frequency;
            //doesn't matter what char we pass in here as nodes with no children are the only chars we care about
            Node parentNode = new Node(null, childNode1, childNode2, freq, 'n');
            //update children parent
            childNode1.parentNode = parentNode;
            childNode2.parentNode = parentNode;
            //delete children
            nodePool.remove(childNode1);
            nodePool.remove(childNode2);
            //find suitable index to keep in order and insert parent node into tree
            int pos = 0;
            for (int i = 0; i < nodePool.size() - 1; i++) {
                pos = i;
                if (freq <= nodePool.get(i).frequency) {
                    break;
                }else{
                    pos=nodePool.size() - 1;
                }
            }
            if (nodePool.size()>0) {
                nodePool.add(pos + 1, parentNode);
            }else{
                nodePool.add(pos, parentNode);
            }
            time++;
        }
        Node rootNode = nodePool.get(0);
        //Add extra parent so all codes start with 1
        Node superNode = new Node(null, null, rootNode, 0, 'n');
        //update children parent
        rootNode.parentNode = superNode;
        nodePool.remove(rootNode);
        nodePool.add(superNode);
        GUI.Log("Binary Tree successfully Initialised. Took " + (System.nanoTime() - startTime)/1000000  +" milliseconds.");
        return superNode;

    }

    private String LoadFile(){
        GUI.Log("Loading file at "+ loadpath);
        String path = loadpath;//"C:\\Users\\timid\\OneDrive\\Desktop\\PERSONAL\\JAVA\\text.txt";
        String str = "";
        try {
            Scanner sc = new Scanner(new File(path));
            sc.useDelimiter(Pattern.compile("^(?!.*(\\u0001\\n)).*\\n$"));
            while (sc.hasNext()) {
                str+=sc.next();
            }
            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return str;
    }
    private void SaveFile(String str,String path){
        GUI.Log("Saving file at "+ savepath);
        //String path = //"C:\\Users\\timid\\OneDrive\\Desktop\\PERSONAL\\JAVA\\textUncompressed.txt";
        try (
                PrintWriter out = new PrintWriter(path);
        ) {
            out.println(str);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void ProgressBar(int i,int len){
        float progress = ((float)i/(float)len)*100f;
        //System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
        //System.out.print(Math.round(progress*100f)/100f + "%");
        GUI.barProgress = progress;
        //System.out.print("|"+progress);
    }

    private void EncodeData(Node tree, String data,Map<Character,Integer> _charDict){
        GUI.Log("Encoding...");
        long startTime = System.nanoTime();
        String binaryString = "";
        String debugString = "";
        List<String> encodingData= new ArrayList<String>();;
        for (int i = 0; i < data.length(); i++){
            ProgressBar(i,data.length());
            char c = data.charAt(i);
            String currentCode = "";
            //Process char
            Node currentNode = charNodeRefDict.get(c);
            while(true){
                Node parentNode = currentNode.parentNode;
                if (parentNode == null){
                    //encodingData.add(currentCode);
                    //currentCode="";
                    break;
                }
                if (parentNode.rightNode == currentNode) {
                    binaryString+="1";
                    currentCode+="1";
                }if (parentNode.leftNode == currentNode) {
                    binaryString+="0";
                    currentCode+="0";
                }
                currentNode = parentNode;
            }
            debugString+="|"+c+":"+currentCode+"|";
        }

        //Split binaryString into 8 bit chunks
        int binaryLength=0;
        int byteLength = 8;
        String tempByteString = "";
        for (int i = 0; i < binaryString.length(); i++) {
           // System.out.println(i);
            if (binaryLength<=byteLength-1){
                tempByteString += binaryString.charAt(i);
                binaryLength++;
            }else {
                encodingData.add(tempByteString);
                tempByteString="";
                tempByteString+=binaryString.charAt(i);
                binaryLength=1;
            }
        }

        //add extra bits
        for (int i = tempByteString.length(); i < byteLength; i++) {
            tempByteString +='0';
        }
        encodingData.add(tempByteString);

        GUI.Log("Successfully Encoded data. Took " + (System.nanoTime() - startTime)/1000000/1000  +" seconds.");

        //Creating a File object
        //String path = "C:\\Users\\timid\\OneDrive\\Desktop\\PERSONAL\\JAVA\\test.box";
        File file = new File(savepath);
        try (
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
        ) {
            byte[] byteRead =  new byte[encodingData.size()];
            String byteString = "";
            //create byte string
            int count = 0;
            for (int _str = 0; _str < encodingData.size(); _str++) {
                count++;
                byteString = encodingData.get(_str);
                //System.out.println(byteString);

                //Write 8 bit chunks at a time
                int _int = Integer.parseInt(byteString, 2);
                byteRead[_str] = (byte)(_int-128);

            }

            //Write header
            int headerAllocationBytes = 4;
            byte headerSize = (byte) headerAllocationBytes;
            ByteBuffer header = ByteBuffer.allocate(headerAllocationBytes);
            header.putInt(encodingData.size());
            byte[] headerBytes = header.array();

            fileOutputStream.write(headerSize);//write size of header
            fileOutputStream.write(headerBytes);//write actual header

            //Write encoded data
            fileOutputStream.write(byteRead);
            fileOutputStream.flush();

            //Write character mappings to file
            for (Character c :_charDict.keySet()){
                dataOutputStream.writeInt((int)c);
                dataOutputStream.writeInt(_charDict.get(c));
                //System.out.println(c+"|"+_charDict.get(c));
            }

            fileOutputStream.flush();
            dataOutputStream.flush();

            GUI.Log("Wrote "+count+" bytes");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        GUI.Log("Successfully wrote file");
        GUI.FinishTask();
    }

    private void DecodeData(){
        String data="";
        //Creating a File object
        //String path = "C:\\Users\\timid\\OneDrive\\Desktop\\PERSONAL\\JAVA\\test.box";
        File file = new File(loadpath);
        GUI.Log("Reading File...");

        Node treeNode = new Node(null,null,null,0,' ');

        try (
                FileInputStream fileInputStream = new FileInputStream(file);
                DataInputStream dataInputStream = new DataInputStream(fileInputStream);

        ) {
            //get header size
            int headerSize = fileInputStream.read();
            //Write header into byte array
            byte[] headerBytes = new byte[headerSize];
            fileInputStream.read(headerBytes,0,headerSize);
            //convert header into integer
            ByteBuffer wrapped = ByteBuffer.wrap(headerBytes); // big-endian by default
            int headerValue = wrapped.getInt();
            int dataPortionSize = headerValue;

            byte[] bytes = new byte[dataPortionSize];

            //Read all data bytes
            fileInputStream.read(bytes,0,dataPortionSize);

            int count = 0;
            for (int a = 0; a < bytes.length; a++) {

                ProgressBar(count,(int)file.length());
                count++;
                int raw_data = bytes[a];
                raw_data+=128;//Binary stored in bytes so shift needed as bytes go fro -128 to 128

                //Build binary string of huffman codes byte by byte
                String datString = Integer.toBinaryString(raw_data);
                //Add lost bits back
                for (int i = datString.length(); i < 8; i++) {
                    StringBuilder sb = new StringBuilder(datString);
                    sb.insert(0, '0');
                    datString = sb.toString();
                }
                data+=datString;
            }

            int is;
            //Recreate charDict
            Map<Character,Integer> newCharDict = new HashMap<>();
            boolean alternate = true;
            int lastEntry = 0;
            //Load dictionary from file
            try {
                while (dataInputStream.available() > 0){
                    is = dataInputStream.readInt();
                    if (alternate){
                        lastEntry = is;
                        alternate=false;
                    }else {
                        newCharDict.put((char)lastEntry,is);
                        alternate=true;
                    }
                }
            }catch (IOException ex){
                ex.printStackTrace();
            }


            //Recreate tree from charDict
            int[] sorted_array = SortDict(newCharDict);
            treeNode = CreateTree(sorted_array,newCharDict);

            GUI.Log("Read "+count+" bytes");
        } catch (IOException ex) {
            ex.printStackTrace();
        }


        String decodedString = "";
        Node rootNode = treeNode;
        Node currentNode = rootNode;
        boolean dataStart = false;
        GUI.Log("Successfully read file");
        GUI.Log("Decoding...");
        long startTime = System.nanoTime();
        for (int i = data.length()-1; i > -1; i--) {
            ProgressBar(data.length()-i,data.length());
            char c = data.charAt(i);
            //If branch returns null and at rootnode, ignore bit
            if (currentNode==rootNode && c == '0' && !dataStart){
                continue;
            }else if(currentNode==rootNode){
                dataStart = true;
            }
            if (currentNode==null){
                currentNode = rootNode;
                continue;
            }
            //if reached end of tree, you have your character!
            if (currentNode.rightNode == null && currentNode.leftNode == null) {
                decodedString = currentNode.character + decodedString;
                currentNode = rootNode;
                //continue;
            }
            //take branches along tree
            if (c == '1'){
                currentNode = currentNode.rightNode;
            }else{
                currentNode = currentNode.leftNode;
            }
        }
        //Add last character
        if (currentNode != null) {
            decodedString = currentNode.character + decodedString;
        }
        SaveFile(decodedString,savepath);
        GUI.Log("Successfully Decoded data. Took " + (System.nanoTime() - startTime)/1000000/1000  +" seconds.");
        GUI.Log("File saved at " + savepath);
        GUI.FinishTask();
    }
}
