package com.shrinkify;

import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

    public static void main(String[] args) throws Exception {
        GUI gui = new GUI();
        Timer timer = new Timer();
        timer.schedule(new UpdateProgress(), 0, 1);

    }
    public static class UpdateProgress extends TimerTask {
        int count = 0;
        public void run() {
            GUI.bar.updateBar(GUI.barProgress);
            count++;
        }
    }
}
