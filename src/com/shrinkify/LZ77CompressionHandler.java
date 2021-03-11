package com.shrinkify;

import com.shrinkify.util.ArrayUtil;
import com.shrinkify.util.FileUtil;
import javafx.scene.control.ProgressBar;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

//Lemepel Ziv Compression
public class LZ77CompressionHandler implements Runnable{

    public static String loadpath, savepath;
    int wordLength = 1;
    //String filename = "Alice.txt";
    String fileEnding = ".txt";
    public enum ProcessType{
        Encode,
        Decode
    }
    public static ProcessType type;

    public void run() {
        try {
            RunEncode();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /*switch (type){
            case Encode:
                RunEncode();
                break;
            case Decode:
                RunDecode();
                break;
        }*/
    }

    public void RunEncode() throws InterruptedException {
        //String str = FileUtil.LoadFile( "C:\\Users\\timid\\OneDrive\\Desktop\\PERSONAL\\JAVA\\test"+fileEnding);
        String str = FileUtil.LoadFile(loadpath);
        Encode(str);
        Decode();

    }
    public void RunDecode(){
    }

    private void Encode(String data){
        //System.out.println(data);
        Map<String,Integer> encoding = new HashMap<>();
        List<Pair<Integer,String>> compressedBinaryData = new ArrayList<>();
        System.out.println("start: "+data.length());
        String sequence = "";
        //int wordLength = 8;
        for (int i = 0; i < data.length(); i+=wordLength) {
            float progress = ((float)i/(float)data.length())*100f;
            GUI.barProgress = progress;
            String c = "";
            if (i+wordLength <= data.length()){
                String binary = "";
                for (int j = 0; j < wordLength; j++) {
                    binary+= data.charAt(i+j);
                }
                c+=binary;
                //System.out.println(i+" | "+binary);
            }else{
                String binary = "";
                for (int j = 0; j < data.length()-i; j++) {
                    binary += data.charAt(i+j);
                }
                //Add extra bits
                String extraBits = "";
                for (int n = binary.length(); n < wordLength; n++) {
                    extraBits +='0';
                }
                //System.out.println(i+" | "+binary+extraBits);
                c+=binary+extraBits;
            }
            sequence+= c;
            //System.out.println(c);
            //System.out.println(i + "/" + data.length());
            if (encoding.get(sequence)!=null){
                //System.out.println(sequence+ " in dict");
                continue;
            }else{
                //System.out.println(sequence+ " not in dict > ");
                encoding.put(sequence,encoding.size());
                //System.out.println(i);
                if (sequence.length() > wordLength) {
                    //remove last letter and get number corresponding to resulting string
                    int compressedString = encoding.get(sequence.substring(0, sequence.length()- wordLength));
                    //Add the number and the last letter to the sequence to compressed data
                    String substring = sequence.substring(sequence.length()- wordLength);
                    compressedBinaryData.add(new Pair<>(compressedString, substring));
                    //System.out.println(g+" "+i+" adding");
                   //System.out.println(compressedString+" -|- "+ substring);
                }else {
                    compressedBinaryData.add(new Pair<>(-1, sequence));//-1 represents first character
                   // System.out.println(g+" "+i+" adding");
                  //System.out.println(-1+" | "+ sequence);

                }
                sequence = "";
            }
        }
        //Add the final bit
        if (encoding.get(sequence)!=null) {
            compressedBinaryData.add(new Pair<>(-1, sequence));
        }
        //Write data to file
        File file = new File("C:\\Users\\timid\\OneDrive\\Desktop\\PERSONAL\\JAVA\\test.box");
        try (
                FileOutputStream fileOutputStream = new FileOutputStream(file);

        ) {
            for (Pair<Integer,String> pair :compressedBinaryData) {
                //Write header for first time (containing overflow number)
                if (pair.getKey() == -1){
                    fileOutputStream.write(1);
                }else {
                    fileOutputStream.write(0);
                }
                //Write header for index (containing overflow number)
                fileOutputStream.write(pair.getKey()/256);
                //Write index
                fileOutputStream.write(pair.getKey());
                //Write header for data (containing overflow number)
                int byteData = Integer.parseInt(pair.getValue(),2);
                //fileOutputStream.write(byteData/256);
                //Write data
                fileOutputStream.write(byteData);
                //System.out.println("WROTE >" +pair.getKey()/256+"  "+pair.getKey()+"  "+ Integer.parseInt(pair.getValue(),2));
                fileOutputStream.flush();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void Decode(){
        //String str = FileUtil.LoadFileBinary( "C:\\Users\\timid\\OneDrive\\Desktop\\PERSONAL\\JAVA\\test.box");
        //System.out.println("//////////////////////////////////");
        File file = new File("C:\\Users\\timid\\OneDrive\\Desktop\\PERSONAL\\JAVA\\test.box");
        //int wordLength = 8;
        Map<Integer,String> compressedBinaryData = new HashMap<>();
        String uncompressedBinary = "";
        try (
                FileInputStream fileInputStream = new FileInputStream(file);
                //DataInputStream dataInputStream = new DataInputStream(fileInputStream);

        ) {

            while (fileInputStream.available() > 0){
                int special = fileInputStream.read();
                int i_overflowCount = fileInputStream.read();//header
                int i = fileInputStream.read();
                //int data_overflowCount = fileInputStream.read();
                int data =  fileInputStream.read();

                //Restore index and data using overflow nums
                i += (256*i_overflowCount);
                //data += (256*data_overflowCount);

                String binaryString = Integer.toBinaryString(data);//= Long.toBinaryString(data);
                String extraBits = "";
                for (int n = binaryString.length(); n < wordLength; n++) {
                    extraBits +='0';
                }
                binaryString = extraBits+binaryString;

                //System.out.println(binaryString);
                //System.out.println("READ: "+overflowCount+" | "+i +" | "+data+" | "+binaryString);
                //System.out.println("READ: "+i +" | "+data);
                if (special == 1){
                    uncompressedBinary+=binaryString;
                    compressedBinaryData.put(compressedBinaryData.size(), binaryString);
                }else{
                    uncompressedBinary+=compressedBinaryData.get(i) +  binaryString;
                    compressedBinaryData.put(compressedBinaryData.size(), compressedBinaryData.get(i) +  binaryString);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("end: "+uncompressedBinary.length());
        //Write data to file
        File newfile = new File("C:\\Users\\timid\\OneDrive\\Desktop\\PERSONAL\\JAVA\\test_unboxed"+fileEnding);
        try (
                FileOutputStream fileOutputStream = new FileOutputStream(newfile);

        ) {
            for (int i = 0; i < uncompressedBinary.length(); i+=8) {
                String binary = "";
                //System.out.println(uncompressedBinary);
                //System.out.println(i+"/"+uncompressedBinary.length());
                for (int j = i; j < i+8; j++) {
                    binary+=uncompressedBinary.charAt(j);
                }
                //System.out.println("WROTE:" + binary);
                fileOutputStream.write(Integer.parseInt(binary,2));
            }
            fileOutputStream.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("done");

    }

}
