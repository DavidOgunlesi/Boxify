package com.shrinkify;

public class ProcessTask {

    private ProcessHandler processHandler;
    public StringBuilder result;
    public int id;

    public ProcessTask(int id,ProcessHandler processHandler){
        this.id = id;
        this.processHandler = processHandler;
        processHandler.processTasks.add(this);
    }

    public void CreateProcess(Thread t){
        t.start();
        processHandler.aliveProcessTasks.add(this);
    }
}
