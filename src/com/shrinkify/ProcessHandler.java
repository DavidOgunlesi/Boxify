package com.shrinkify;

import java.util.ArrayList;
import java.util.List;

public class ProcessHandler {

    public List<ProcessTask> processTasks = new ArrayList<>();
    public List<ProcessTask> aliveProcessTasks = new ArrayList<>();

    public String[] DivideDataForProcesses(StringBuilder data) {
        //Split text data into equal chunks and pass onto threads and processed in parallel
        return DivideDataForProcesses(data,Runtime.getRuntime().availableProcessors());
    }

    public String[] DivideDataForProcesses(StringBuilder data,int threadCount){
        //Stores the length of the string
        int len = data.length();
        int temp = 0;
        int chunkCount = len/(threadCount-1);

        //Stores the array of string
        String[] chunkArr = new String [threadCount];

        for(int i = 0; i < len; i +=chunkCount) {
            //Dividing string in n equal part using substring()
            String part;
            if (i+chunkCount<len) {
                part = data.substring(i, i+chunkCount);
            }else {
                part = data.substring(i, len);
            }
            chunkArr[temp] = part;
            temp++;
        }
        return  chunkArr;
    }

    public byte[][] DivideDataForProcesses(byte[] array) {
        //Split text data into equal chunks and pass onto threads and processed in parallel
        return DivideDataForProcesses(array,Runtime.getRuntime().availableProcessors());
    }

    //Thanks to lesleh https://gist.github.com/lesleh/7724554 for base, slightly modified to work with byte arrays
    public byte[][] DivideDataForProcesses(byte[] array ,int threadCount){
        // Example usage:
        //
        // int[] numbers = {1, 2, 3, 4, 5, 6, 7};
        // int[][] chunks = chunkArray(numbers, 3);
        //
        // chunks now contains [
        //                         [1, 2, 3],
        //                         [4, 5, 6],
        //                         [7]
        //
        int len = array.length;
        int chunkSize =  len/(threadCount-1);

        int numOfChunks = (int)Math.ceil((double)array.length / chunkSize);
        byte[][] output = new byte[numOfChunks][];

        for(int i = 0; i < numOfChunks; ++i) {
            int start = i * chunkSize;
            int length = Math.min(array.length - start, chunkSize);

            byte[] temp = new byte[length];
            System.arraycopy(array, start, temp, 0, length);
            output[i] = temp;
        }
        return output;
    }


    public void CreateProcess(Runnable target, int id) throws InterruptedException {
        ProcessTask newProcessTask = new ProcessTask(id,this);
        Thread t = new Thread(target);
        newProcessTask.CreateProcess(t);
    }

    public ProcessTask getProcessByID(int id){
        return processTasks.get(id);
    }

    public void FinishTask(ProcessTask p){
        aliveProcessTasks.remove(p);
    }

    public void flush(){
        processTasks.clear();
    }

    public void Wait() throws InterruptedException {
        while (aliveProcessTasks.size()>0){
            //wait on threads
            Thread.sleep(1000);
        }
    }
}
