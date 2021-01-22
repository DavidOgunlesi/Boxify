package com.shrinkify.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArrayUtil {

    /* low  --> Starting index,  high  --> Ending index */
    public static void QuickSort(int[] _array, int _low, int _high){
        /*Credit to https://www.geeksforgeeks.org/quick-sort/ for the pseudocode*/
        /* pi is partitioning index, arr[pi] is now
           at right place */
        if (_low < _high){
            int pivot_loc = Partition(_array,_low,_high);
            QuickSort(_array,_low,pivot_loc - 1);
            QuickSort(_array,pivot_loc + 1,_high);
        }
    }

    /* This function takes last element as pivot, places
       the pivot element at its correct position in sorted
        array, and places all smaller (smaller than pivot)
       to left of pivot and all greater elements to right
       of pivot */
    public static int Partition(int[] _array, int _low, int _high){
        /*Credit to https://www.geeksforgeeks.org/quick-sort/ for the pseudocode*/
        // pivot (Element to be placed at right position)
        int pivot = _array[_high];

        int i = (_low - 1);  // Index of smaller element

        for (int j = _low; j <= _high- 1; j++)
        {
            // If current element is smaller than the pivot
            if (_array[j] < pivot)
            {
                i++;    // increment index of smaller element
                Swap(_array,i,j);
            }
        }
        Swap(_array,i+1,_high);
        return (i + 1);
    }


    public static  void Swap(int[] _array, int i, int j){
        int t = _array[i];
        _array[i] = _array[j];
        _array[j] = t;
    }

    public static List<String> BinaryStringToByteStringList(String binaryString, int byteLength){
        //Split binaryString into 8 bit chunks
        List<String> data = new ArrayList<String>();
        int binaryLength=0;
        String tempByteString = "";
        for (int i = 0; i < binaryString.length(); i++) {
            // System.out.println(i);
            if (binaryLength<=byteLength-1){
                tempByteString += binaryString.charAt(i);
                binaryLength++;
            }else {
                data.add(tempByteString);
                tempByteString="";
                tempByteString+=binaryString.charAt(i);
                binaryLength=1;
            }
        }
        return data;
    }

    public static Map<Character,Integer> CopyDict(Map<Character,Integer> dict){
        Map<Character,Integer> savedDict = new HashMap<>();
        for (Character _char : dict.keySet())
        {
            int val = dict.get(_char);
            savedDict.put(_char,val);
        }
        return savedDict;
    }
}
