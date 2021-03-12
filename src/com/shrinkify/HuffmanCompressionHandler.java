package com.shrinkify;

import com.shrinkify.util.ArrayUtil;
import com.shrinkify.util.FileUtil;

import java.nio.ByteBuffer;
import java.util.*;
import java.io.*;

public class HuffmanCompressionHandler implements Runnable{

    public static String loadpath, savepath;


    public enum ProcessType{
        Encode,
        Decode
    }
    public static ProcessType type;


    public void run() {
        switch (type){
            case Encode:
                try {
                    RunEncode();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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

        public boolean isLeafNode = false;

        public List<Node> nodesToEncode = new ArrayList<>();
        public String encoding = "";

        public Node(Node parentNode,Node leftNode,Node rightNode,int frequency,char character) {
            this.parentNode = parentNode;
            this.leftNode = leftNode;
            this.rightNode = rightNode;
            this.frequency = frequency;
            this.character = character;

        }
    }

    public void RunEncode() throws InterruptedException {
        StringBuilder str = FileUtil.LoadFile(loadpath);
        Map<Character,Integer> dict = CreateDict(str);
        Map<Character,Integer> savedDict = ArrayUtil.CopyDict(dict);
        int[] sorted_array = SortDict(dict);
        Node tree = CreateTree(sorted_array,dict);
        EncodeData(str,savedDict);
    }
    public void RunDecode(){
        DecodeData();
    }

    private Map<Character,Integer> CreateDict(StringBuilder data) {
        GUI.Log("Creating character Dictionary...");
        long startTime = System.nanoTime();

        Map<Character, Integer> charDict = new HashMap<>();
        int time = 0;

        GUI.Log("Loading file at " + loadpath);
        String path = loadpath;

        for (int i = 0; i < data.length(); i++) //charArray would look something like [h,e,l,l,o]
            {
                /*if (data.length() == 0) {
                    break;
                }*/
                char _char = data.charAt(i);
                if (charDict.containsKey(_char)) {
                    int val = charDict.get(_char);
                    val = val + 1;
                    charDict.put(_char, val);
                } else {
                    charDict.put(_char, 1);
                }
                time++;
            }
            GUI.Log("Character Dictionary successfully created. Took " + (System.nanoTime() - startTime) / 1000000 + " milliseconds.");
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
        GUI.Log("Sorted Array size: "+ array.length);
        GUI.Log("Character Dictionary size: "+ charDict.size());
        long startTime = System.nanoTime();
        try {
            List<Character> charDeleteBuffer = new ArrayList<>();
            List<Node> nodePool = new ArrayList<>();
            //Covert array into list of nodes
            int time = 0;

            for (int _int : array) {
                ProgressBar(time, array.length);
                for (Character _char : charDict.keySet()) {
                    int frequency = charDict.get(_char);
                    if (frequency == _int) {
                        char character = _char;
                        //Create node object
                        Node newNode = new Node(null, null, null, frequency, character);
                        //Make sure it knows its a leaf node
                        newNode.isLeafNode = true;
                        newNode.nodesToEncode.add(newNode);
                        //Add node object to object pool, don't forget swimming trunks!
                        nodePool.add(newNode);
                        charDeleteBuffer.add(_char);
                        //save reference for quick searching later
                        charNodeRefDict.put(character, newNode);
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
            while (nodePool.size() > 1) {
                ProgressBar(time, nodePool.size());
                //Create tree with every pair of values
                //get the 2 least frequent nodes from nodepool sorted by lowest frequency to highest
                Node childNode1 = nodePool.get(0);
                Node childNode2 = nodePool.get(1);
                int freq = childNode1.frequency + childNode2.frequency;
                //doesn't matter what char we pass in here as nodes with no children are the only chars we care about
                Node parentNode = new Node(null, childNode1, childNode2, freq, 'n');


                //bake encoding
                for (Node n: childNode1.nodesToEncode) {
                    n.encoding+=0;
                }
                for (Node n: childNode2.nodesToEncode) {
                    n.encoding+=1;
                }
                parentNode.nodesToEncode.addAll(childNode1.nodesToEncode);
                parentNode.nodesToEncode.addAll(childNode2.nodesToEncode);

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
                    } else {
                        pos = nodePool.size() - 1;
                    }
                }
                if (nodePool.size() > 0) {
                    nodePool.add(pos + 1, parentNode);
                } else {
                    nodePool.add(pos, parentNode);
                }
                time++;
            }
            Node rootNode = nodePool.get(0);
            //Add extra parent so all codes start with 1
            //Node superNode = new Node(null, null, rootNode, 0, 'n');
            //update children parent
            //rootNode.parentNode = superNode;
            //nodePool.remove(rootNode);
            //nodePool.add(superNode);
            GUI.Log("Binary Tree successfully Initialised. Took " + (System.nanoTime() - startTime) / 1000000 + " milliseconds.");
            return rootNode;
        }catch (Exception e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            GUI.LogError("Error in reading file, file may be corrupted");
            GUI.Log(sw.toString());
        }
        return null;

    }

    private void ProgressBar(float i,float len){
        //System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
        //System.out.print(Math.round(progress*100f)/100f + "%");
        GUI.barProgress = (i/len)*100f;
        //System.out.println("|"+progress);
    }

    /*void CalculateProcessProgress(){
        while (ProcessWork < ProcessTotalWork) {
            ProgressBar(ProcessWork, ProcessTotalWork);
        }
    }*/

    void EncodeProcess(String data,ProcessHandler processHandler,int id) {
        //String binaryString = "";
        StringBuilder binaryString = new StringBuilder();

        int len = data.length();
        for (int i = 0; i < data.length(); i++) {
            //System.out.print(i);
            char c = data.charAt(i);
            //String currentCode = "";
            //Process char
            Node currentNode = charNodeRefDict.get(c);
            binaryString.append(currentNode.encoding);

            ProgressBar(i, len);
        }
        ProcessTask p = processHandler.getProcessByID(id);
        p.result = binaryString;
        processHandler.FinishTask(p);
    }

    private void EncodeData(StringBuilder data,Map<Character,Integer> _charDict) throws InterruptedException {
        GUI.Log("Encoding...");
        long startTime = System.nanoTime();

        ProcessHandler processHandler = new ProcessHandler();
        //Split text data into equal chunks and pass onto threads and processed in parallel
        String[] chunkArr = processHandler.DivideDataForProcesses(new StringBuilder(data));

        //Create threads
        for(int i = 0; i < chunkArr.length; i++) {
            //System.out.println(chunkArr[i]);
            int id = i;
            String str = chunkArr[id];
            processHandler.CreateProcess(() -> EncodeProcess(str,processHandler,id),id);
        }

        GUI.Log("Started "+processHandler.aliveProcessTasks.size()+" Threads");

        //wait on threads to finish
        processHandler.Wait();

        GUI.Log("All threads successfully executed, processing results.");

        //StringBuilder binaryString =  new StringBuilder();
        //Process results and write to file
        File file = new File(savepath);
        try (
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream)
        ) {

            //Get result data size
            int dataSize = 0;
            for (ProcessTask p: processHandler.processTasks) {
                dataSize += p.result.length();
            }
            //account for non perfect byte division
            if (dataSize%8!=0){
                dataSize= (dataSize/8)+1;
            }else {
                dataSize= (dataSize/8);
            }


            //Write header
            int headerAllocationBytes = 4;
            byte headerSize = (byte) headerAllocationBytes;
            ByteBuffer header = ByteBuffer.allocate(headerAllocationBytes);
            header.putInt(dataSize);
            byte[] headerBytes = header.array();

            fileOutputStream.write(headerSize);//write size of header
            fileOutputStream.write(headerBytes);//write actual header



            String leftOver = "";
            for (ProcessTask p: processHandler.processTasks) {
                //List<String> encodingData= new ArrayList<>();
                StringBuilder binaryString = new StringBuilder(leftOver);
                binaryString.append(p.result);

                //Split binaryString into 8 bit chunks
                int binaryStringLen = binaryString.length();
                int processesLength = binaryStringLen*processHandler.processTasks.size();
                int processCumulativeWork = p.id*binaryStringLen;
                int binaryLength = 0;
                int byteLength = 8;
                StringBuilder tempByteString = new StringBuilder();

                for (int i = 0; i < binaryStringLen; i++) {
                    ProgressBar( processCumulativeWork+i,processesLength);

                    if (binaryLength <= byteLength - 1) {
                        tempByteString.append(binaryString.charAt(i));
                        binaryLength++;
                    } else {
                        int _int = Integer.parseInt(tempByteString.toString(), 2);
                        byte byteRead = (byte)(_int-128);
                        //Write encoded data
                        fileOutputStream.write(byteRead);
                        tempByteString = new StringBuilder();
                        tempByteString.append(binaryString.charAt(i));
                        binaryLength = 1;
                    }
                }
                //if not last process, add overflow to next processes data else handle overflow
                if (p.id != processHandler.processTasks.size()-1){
                    leftOver = tempByteString.toString();
                }else{
                    //add extra bits
                    for (int i = tempByteString.length(); i < byteLength; i++) {
                        tempByteString.append('0');
                    }

                    int _int = Integer.parseInt(tempByteString.toString(), 2);
                    byte byteRead = (byte)(_int-128);
                    //Write encoded data
                    fileOutputStream.write(byteRead);
                    fileOutputStream.flush();
                }
            }
            processHandler.flush();
            //Write character mappings to file
            for (Character c :_charDict.keySet()){
                dataOutputStream.writeInt((int)c);
                dataOutputStream.writeInt(_charDict.get(c));
            }

            fileOutputStream.flush();
            dataOutputStream.flush();

            //GUI.Log("Wrote "+count+" bytes");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //System.out.println(count);
        GUI.Log("Successfully Encoded data. Took " + (System.nanoTime() - startTime) / 1000000 / 1000 + " seconds.");

        GUI.Log("Successfully wrote file");
        GUI.FinishTask();
    }

    private void DecodeReadProcess(byte[] bytes,ProcessHandler processHandler,int id){
        int count = 0;
        int len = bytes.length;
        ProcessTask p = processHandler.getProcessByID(id);
        p.result = new StringBuilder();
        for (byte aByte : bytes) {

            ProgressBar(count, len);
            count++;
            int raw_data = aByte;
            raw_data += 128;//Binary stored in bytes so shift needed as bytes go from -128 to 128

            //Build binary string of huffman codes byte by byte
            String datString = Integer.toBinaryString(raw_data);
            //Add lost bits back
            for (int i = datString.length(); i < 8; i++) {
                StringBuilder sb = new StringBuilder(datString);
                sb.insert(0, '0');
                datString = sb.toString();
            }
            p.result.append(datString);
        }
        processHandler.FinishTask(p);
    }

    private void DecodeData(){
        //StringBuilder data= new StringBuilder();
        //Creating a File object
        //String path = "C:\\Users\\timid\\OneDrive\\Desktop\\PERSONAL\\JAVA\\test.box";
        File file = new File(loadpath);
        GUI.Log("Reading File...");

        Node treeNode = new Node(null,null,null,0,' ');
        ProcessHandler processHandler = new ProcessHandler();
        Map<String,Character> cachedEncodings = new HashMap<>();
        try (
                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                DataInputStream dataInputStream = new DataInputStream(fileInputStream)

        ) {
            //get header size
            int headerSize = fileInputStream.read();
            //Write header into byte array
            byte[] headerBytes = new byte[headerSize];
            fileInputStream.read(headerBytes,0,headerSize);
            //convert header into integer
            ByteBuffer wrapped = ByteBuffer.wrap(headerBytes); // big-endian by default

            int headerValue = wrapped.getInt();

            byte[] bytes = new byte[headerValue];

            //Read all data bytes
            bufferedInputStream.read(bytes,0, headerValue);
            int count = 0;

            //Split text data into equal chunks and pass onto threads and processed in parallel
            byte[][] chunkArr = processHandler.DivideDataForProcesses(bytes);

            //Create threads
            for(int i = 0; i < chunkArr.length; i++) {
                //System.out.println(chunkArr[i]);
                int id = i;
                byte[] b = chunkArr[id];
                processHandler.CreateProcess(() -> DecodeReadProcess(b,processHandler,id),id);
            }

            GUI.Log("Started "+processHandler.aliveProcessTasks.size()+" Threads");

            //wait on threads to finish
            processHandler.Wait();

            GUI.Log("All threads successfully executed, processing results.");



            /*for (int a = 0; a < bytes.length; a++) {

                ProgressBar(count,(int)file.length());
                count++;
                int raw_data = bytes[a];
                raw_data+=128;//Binary stored in bytes so shift needed as bytes go from -128 to 128

                //Build binary string of huffman codes byte by byte
                String datString = Integer.toBinaryString(raw_data);
                //Add lost bits back
                for (int i = datString.length(); i < 8; i++) {
                    StringBuilder sb = new StringBuilder(datString);
                    sb.insert(0, '0');
                    datString = sb.toString();
                }
                data.append(datString);
            }*/


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
            bufferedInputStream.close();

            Map<Character,Integer> cachedNewCharDict = new HashMap<>(newCharDict);
            //Recreate tree from charDict
            int[] sorted_array = SortDict(newCharDict);
            treeNode = CreateTree(sorted_array,newCharDict);
            if (treeNode == null){
                return;
            }

            //store encodings in hashmap
            for (Character c:  cachedNewCharDict.keySet()) {
                Node node = charNodeRefDict.get(c);
                cachedEncodings.put(node.encoding,c);
            }

            GUI.Log("Read "+ headerValue +" bytes");
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }

        //process results

        StringBuilder decodedString = new StringBuilder();
        Node rootNode = treeNode;
        Node currentNode = rootNode;
        //boolean dataStart = false;
        GUI.Log("Successfully read file");
        GUI.Log("Decoding...");
        long startTime = System.nanoTime();


        //StringBuilder sequence = new StringBuilder(processHandler.processTasks.get(0).result.charAt(0));
        //populate results
        boolean dataStart = false;
        Map<String,Character> cache = new HashMap<>();
        for (int pnum = processHandler.processTasks.size()-1; pnum > -1; pnum--) {

            ProcessTask p = processHandler.processTasks.get(pnum);
            StringBuilder data = p.result;
            GUI.Log("Decode Process " + p.id + " start ready "+ data.length()/8 + " bytes.");
            int taskWorkLeft = processHandler.processTasks.size()- p.id;
            int totalWork = data.length()*processHandler.processTasks.size();

            int binaryStringLen = data.length();

            for (int i = data.length()-1; i > -1; i--) {
                ProgressBar(taskWorkLeft*(binaryStringLen-i),totalWork);
                char c = data.charAt(i);
                if (!dataStart) {
                    if (c == '0') {
                        continue;
                    } else {
                        dataStart = true;
                    }
                }

                if (currentNode == null) {
                    currentNode = rootNode;
                    continue;
                }
                //if reached end of tree, you have your character!
                if (currentNode.isLeafNode) {
                    char nodeChar = currentNode.character;
                    decodedString.insert(0, nodeChar);
                    currentNode = rootNode;
                }
                //take branches along tree
                if (c == '1') {
                    currentNode = currentNode.rightNode;
                } else {
                    currentNode = currentNode.leftNode;
                }
            }
        }
        //Add last character
        if (currentNode != null) {
            decodedString.insert(0, currentNode.character);
        }
        processHandler.flush();

        FileUtil.SaveFile(decodedString,savepath);
        GUI.Log("Successfully Decoded data. Took " + (System.nanoTime() - startTime)/1000000/1000  +" seconds.");
        GUI.Log("File saved at " + savepath);
        GUI.FinishTask();
    }
}
