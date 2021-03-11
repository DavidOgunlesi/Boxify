package com.shrinkify.util;

import com.shrinkify.GUI;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

public class FileUtil {


    public static StringBuilder LoadFile(String loadpath){
        GUI.Log("Loading file at "+ loadpath);
        StringBuilder str = new StringBuilder();

        try {
            FileReader file = new FileReader(loadpath);
            Scanner sc = new Scanner(file);
            long filelength = new File(loadpath).length();
            sc.useDelimiter(Pattern.compile("^(?!.*(\\u0001\\n)).*\\n$"));
            int count = 0;
            while (sc.hasNext()) {
                String line = sc.next();
                GUI.barProgress = ((float)count/(float)filelength)*100f;
                str.append(line);
            }
            // note that Scanner suppresses exceptions
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static void SaveFile(StringBuilder str,String path){
        GUI.Log("Saving file at "+ path);
        //String path = //"C:\\Users\\timid\\OneDrive\\Desktop\\PERSONAL\\JAVA\\textUncompressed.txt";
        try (
                PrintWriter out = new PrintWriter(path)
        ) {
            out.println(str);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static StringBuilder LoadFileBinary(String loadpath){
        File file = new File(loadpath);
        StringBuilder binaryData = new StringBuilder();
        try (
                FileInputStream fileInputStream = new FileInputStream(file);
                DataInputStream dataInputStream = new DataInputStream(fileInputStream)

        ) {
            int is;
            int count = 0;
            while (dataInputStream.available() > 0){
                GUI.barProgress = ((float)count/(float)file.length())*100f;
                String data = Integer.toBinaryString(dataInputStream.read());
                //add extra bits
                StringBuilder extraBits = new StringBuilder();
                for (int i = data.length(); i < 8; i++) {
                    extraBits.append('0');
                }
                binaryData.append(extraBits.append(data));
                count++;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return binaryData;
    }
}
