package com.shrinkify;

public class Main {

    public static void main(String[] args) {
	// write your code here
        CompressionHandler compHandle = new CompressionHandler();
        compHandle.RunEncode();
        compHandle.RunDecode();
    }
}
