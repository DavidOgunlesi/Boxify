package com.shrinkify.util;

import com.shrinkify.GUI;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

public class FileUtil {


    public static String LoadFile(String loadpath){
        GUI.Log("Loading file at "+ loadpath);
        String path = loadpath;//"C:\\Users\\timid\\OneDrive\\Desktop\\PERSONAL\\JAVA\\text.txt";
        String str = "";

        try {
            FileReader file = new FileReader(path);
            Scanner sc = new Scanner(file);
            long filelength = new File(path).length();
            sc.useDelimiter(Pattern.compile("^(?!.*(\\u0001\\n)).*\\n$"));
            int count = 0;
            while (sc.hasNext()) {
                String line = sc.next();
                float progress = ((float)count/(float)filelength)*100f;
                GUI.barProgress = progress;
                str += line;
            }
            // note that Scanner suppresses exceptions
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static void SaveFile(String str,String path){
        GUI.Log("Saving file at "+ path);
        //String path = //"C:\\Users\\timid\\OneDrive\\Desktop\\PERSONAL\\JAVA\\textUncompressed.txt";
        try (
                PrintWriter out = new PrintWriter(path);
        ) {
            out.println(str);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String LoadFileBinary(String loadpath){
        File file = new File(loadpath);
        String binaryData = "";
        try (
                FileInputStream fileInputStream = new FileInputStream(file);
                DataInputStream dataInputStream = new DataInputStream(fileInputStream);

        ) {
            int is;
            int count = 0;
            while (dataInputStream.available() > 0){
                float progress = ((float)count/(float)file.length())*100f;
                GUI.barProgress = progress;
                String data = Integer.toBinaryString(dataInputStream.read());
                //add extra bits
                String extraBits = "";
                for (int i = data.length(); i < 8; i++) {
                    extraBits +='0';
                }
                binaryData+=extraBits+data;
                count++;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return binaryData;
    }
}
