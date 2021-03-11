package com.shrinkify;


import com.shrinkify.external.JFilePicker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GUI {

    //private int count = 0;
    private JFrame frame,new_frame;
    private JButton compButton, uncompButton,viewFileButton;
    private JButton logButton;
    private JPanel panel;
    private JLabel estLabel;
    private JLabel errorLabel;
    private JLabel new_logo;
    JScrollPane pane;
    private JFilePicker filePickerLoad;
    private  String loadPath,savePath;

    public static float barProgress;
    public static LoadingBar bar;
    public static JTextArea log;
    AdvancedMenu advancedMenu;
    JPopupMenu popupMenu;
    private int imageCount = 1;


    public boolean task =false;

    public static GUI instance;

    public enum CompressionType{
        Huffman,
        LZ77
    }
    public static CompressionType type = CompressionType.Huffman;

    public GUI() throws Exception {
        if (instance==null){
            instance = this;
        }else{
            throw new Exception("There can only be one GUI object");
        }




        JFrame.setDefaultLookAndFeelDecorated(false);
        JDialog.setDefaultLookAndFeelDecorated(false);
        frame = new JFrame();

        compButton = new JButton(new AbstractAction("Box") {
            @Override
            public void actionPerformed( ActionEvent e ) {
                try {
                    Compress();
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        });
        uncompButton = new JButton(new AbstractAction("Unbox") {
            @Override
            public void actionPerformed( ActionEvent e ) {
                Uncompress();
            }
        });
        viewFileButton = new JButton( new AbstractAction("View file") {
            @Override
            public void actionPerformed( ActionEvent e ) {
                try {
                    ViewFile();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        log = new JTextArea(5, 30);
        logButton = new JButton( new AbstractAction("Advanced") {
            @Override
            public void actionPerformed( ActionEvent e ) {
                ToggleAdvanced();
            }
        });
        errorLabel = new JLabel();
        errorLabel.setForeground(Color.red);

        estLabel = new JLabel("Estimated Time: ");

        ImageIcon icon = new ImageIcon("C:\\Users\\timid\\OneDrive\\Desktop\\PERSONAL\\JAVA\\Shrinkify\\src\\resources\\logo.png");
        Image image = icon.getImage(); // transform it
        Image newimg = image.getScaledInstance(180, 180,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
        icon = new ImageIcon(newimg);  // transform it back
        JLabel logo = new JLabel(icon);

        //Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JButton file_btn = new JButton("File");
        file_btn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                popupMenu.show(frame , e.getX(), e.getY());
            }
        });

        popupMenu = CreateFileDropdown();
        toolBar.add(file_btn);
        toolBar.addSeparator();
        JButton pref_btn = new JButton(new AbstractAction("Help") {
            @Override
            public void actionPerformed( ActionEvent e ) {
                new_frame = new JFrame();
                JPanel new_panel = new JPanel();
                ImageIcon icon = new ImageIcon("C:\\Users\\timid\\OneDrive\\Desktop\\PERSONAL\\JAVA\\Shrinkify\\src\\resources\\"+imageCount+".png");
                new_logo = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(Math.round(782/1.3f), Math.round(589/1.3f),  java.awt.Image.SCALE_SMOOTH)));
                new_panel.add(new_logo);

                JButton nextBtn = new JButton(new AbstractAction("Next Image") {
                    @Override
                    public void actionPerformed( ActionEvent e ) {
                        imageCount++;
                        if (imageCount>5){
                            imageCount = 1;
                        }
                        ImageIcon new_icon = new ImageIcon("C:\\Users\\timid\\OneDrive\\Desktop\\PERSONAL\\JAVA\\Shrinkify\\src\\resources\\"+imageCount+".png");
                        new_logo.setIcon(new ImageIcon(new_icon.getImage().getScaledInstance(Math.round(782/1.3f), Math.round(589/1.3f),  java.awt.Image.SCALE_SMOOTH)));
                        new_frame.pack();
                    }
                });
                new_panel.add(nextBtn);

                new_frame.add(new_panel,BorderLayout.CENTER);
                new_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                new_frame.setTitle("Boxify");
                new_frame.setPreferredSize(new Dimension(800, 500));
                new_frame.setResizable(false);
                ImageIcon smallIcon = new ImageIcon("C:\\Users\\timid\\OneDrive\\Desktop\\PERSONAL\\JAVA\\Shrinkify\\src\\resources\\small_logo.png");
                new_frame.setIconImage(smallIcon.getImage().getScaledInstance(782, 589,  java.awt.Image.SCALE_SMOOTH));
                new_frame.pack();
                new_frame.setVisible(true);
            }
        });

        toolBar.add(pref_btn);




        filePickerLoad = new JFilePicker("Pick a file", "Browse...");
        filePickerLoad.setMode(JFilePicker.MODE_OPEN);
        filePickerLoad.addFileTypeFilter(".txt", "Text Files");


        GridBagConstraints c = new GridBagConstraints();

        c.weightx = 1;
        c.weighty = .25;
        c.insets = new Insets(0, 30, 5, 30);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;

        GridBagConstraints c2 = new GridBagConstraints();
        c2.insets = new Insets(0, 0, 0, 0);
        c2.gridwidth = GridBagConstraints.REMAINDER;
        c2.fill = GridBagConstraints.BOTH;

        panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(0,0,30,0));
//        /panel.setLayout(new GridLayout(0,1));
        panel.setLayout(new GridBagLayout());


        panel.add(toolBar,c2);
        panel.add(logo,c);
        panel.add(filePickerLoad,c);
        bar = new LoadingBar(panel);
        panel.add(bar.pbar,c);
        panel.add(estLabel,c);
        panel.add(compButton,c);
        panel.add(uncompButton,c);
        panel.add(viewFileButton,c);
        panel.add(errorLabel,c);

        //panel.add(new JLabel("Log"));
        panel.add(logButton,c);
        //panel.add(log,c);
        pane = new JScrollPane(log);
        pane.setPreferredSize(new Dimension(690, 110));

        advancedMenu = new AdvancedMenu();
        advancedMenu.Add(pane,null);

        log.setPreferredSize(new Dimension(700,700));

        frame.add(panel,BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Boxify");
        frame.setPreferredSize(new Dimension(800, 500));
        frame.setResizable(false);
        ImageIcon smallIcon = new ImageIcon("C:\\Users\\timid\\OneDrive\\Desktop\\PERSONAL\\JAVA\\Shrinkify\\src\\resources\\small_logo.png");
        frame.setIconImage(smallIcon.getImage().getScaledInstance(480, 480,  java.awt.Image.SCALE_SMOOTH));

        frame.pack();
        frame.setVisible(true);
        compButton.setVisible(false);
        uncompButton.setVisible(false);
        viewFileButton.setVisible(false);
        estLabel.setVisible(false);
        bar.pbar.setVisible(false);
        Timer timer = new Timer();
        timer.schedule(new UpdatePath(), 0, 1);
    }

    public class AdvancedMenu{

        List<Component> components = new ArrayList<>();
        public boolean isVisible = false;

        public void Add(Component component, Object constraints){
            if ( constraints == null){
                panel.add(component);
            }else {
                panel.add(component, constraints);
            }
            components.add(component);
            component.setVisible(false);
        }
        public void toggleVisible(){
            for (Component c: components) {
                c.setVisible(!isVisible);
            }
            isVisible = !isVisible;
        }

    }

    public class UpdatePath extends TimerTask {
        public void run() {
            if (filePickerLoad.getFileChooser().isShowing()){
                task=false;
                GUI.instance.frame.setPreferredSize(new Dimension(800, 600));
                GUI.instance.frame.pack();
            }
            if (!task) {
                loadPath = filePickerLoad.getSelectedFilePath();
            }
            try {
                File file = new File(loadPath);
                int index = file.getName().lastIndexOf(".");
                    if (file.getName().substring(index).equalsIgnoreCase(".txt")) {
                        GUI.SetEstimatedTime(Math.round((float) file.length()/1000/292.8478291f));
                        estLabel.setVisible(true);
                        GUI.instance.viewFileButton.setVisible(false);
                        compButton.setVisible(true);
                        compButton.setText("Box");
                        uncompButton.setVisible(false);
                        //errorLabel.setText("");

                    } else if (file.getName().substring(index).equalsIgnoreCase(".box")) {
                        GUI.SetEstimatedTime(Math.round((float) file.length()/1000/292.8478291f));
                        GUI.instance.viewFileButton.setVisible(false);
                        //estLabel.setVisible(true);
                        compButton.setVisible(false);
                        uncompButton.setVisible(true);
                        uncompButton.setText("Unbox");
                        //errorLabel.setText("");
                    }else{
                        errorLabel.setText("Wrong File type, .txt or .box only.");
                    }

            }catch (Exception e){
                //Do nothing
            }
        }
    }

    public static void Log(String msg){
        log.append(msg+"\n");
        System.out.println(msg);
    }
    public static void LogError(String msg){
        log.append(msg+"\n");
        GUI.instance.errorLabel.setText(msg);
    }

    public static void SetEstimatedTime(int timeInSecs){
        GUI.instance.estLabel.setText("Estimated Time : " + timeInSecs + " seconds");
    }


    public static void FinishTask(){
        GUI.instance.loadPath = "";
        GUI.instance.uncompButton.setText("Unbox again");//.setVisible(false);
        GUI.instance.compButton.setText("Box  again");
        GUI.instance.viewFileButton.setVisible(true);
        GUI.bar.updateBar(0);
        GUI.instance.frame.setPreferredSize(new Dimension(800, 700));
        GUI.instance.frame.pack();
    }

    public JPopupMenu CreateFileDropdown(){
        //File Popup
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem menuItemCreateOpen = new JMenuItem(new AbstractAction("Open") {
            @Override
            public void actionPerformed( ActionEvent e ) {
                filePickerLoad.button.doClick();
            }
        });
        popupMenu.add(menuItemCreateOpen);

        JMenuItem menuItemCreatePreferences = new JMenuItem(new AbstractAction("Preferences"){
            @Override
            public void actionPerformed( ActionEvent e ) {
                //open pref menu
            }
        });
        popupMenu.add(menuItemCreatePreferences);

        JMenuItem menuItemCreateExit = new JMenuItem(new AbstractAction("Exit"){
            @Override
            public void actionPerformed( ActionEvent e ) {
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        });
        popupMenu.add(menuItemCreateExit);
        return popupMenu;
    }

    public void ViewFile() throws IOException {
        //Runtime.getRuntime().exec("explorer.exe /select," + savePath);
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(new File(new File(savePath).getParent()));
        }else{
            errorLabel.setText("Error retrieving native browser.");
        }
    }
    public void ToggleAdvanced(){
        advancedMenu.toggleVisible();
        if (advancedMenu.isVisible){
            logButton.setText("Simple");
            frame.setPreferredSize(new Dimension(800, 600));
            //frame.setResizable(false);
            frame.pack();
        }else{
            logButton.setText("Advanced");
            frame.setPreferredSize(new Dimension(800, 500));
            //frame.setResizable(false);
            frame.pack();
        }
    }

    public void Compress() throws InterruptedException {
        errorLabel.setText("");
        task=true;
        loadPath = filePickerLoad.getSelectedFilePath();
        bar.pbar.setVisible(true);
        estLabel.setVisible(true);
        File file = new File(loadPath);
        int index = file.getName().lastIndexOf(".");
        String compressedFilename = file.getName().substring(0, index)+".box";

        JFileChooser chooser = CreateSaveDirectoryChooser();
        if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
            Log("////////////////////////NEW TASK////////////////////////");
            Log("Target Directory:"+  chooser.getSelectedFile() +"\\"+compressedFilename);
            Log("Task Type: Compress");
            Log("////////////////////////////////////////////////////////");
            savePath = chooser.getSelectedFile() +"\\"+ compressedFilename;

            if (type == CompressionType.Huffman) {

                HuffmanCompressionHandler.type = HuffmanCompressionHandler.ProcessType.Encode;
                HuffmanCompressionHandler.savepath = savePath;
                HuffmanCompressionHandler.loadpath = loadPath;

                Thread t = new Thread(new HuffmanCompressionHandler());
                t.start();
            }else{
                LZ77CompressionHandler lz77CompressionHandler = new LZ77CompressionHandler();
                lz77CompressionHandler.RunEncode();
                Thread t = new Thread(new LZ77CompressionHandler());
                t.start();
            }
        }
    }
    public void Uncompress() {
        errorLabel.setText("");
        task=true;
        bar.pbar.setVisible(true);
        estLabel.setVisible(true);
        loadPath = filePickerLoad.getSelectedFilePath();
        File file = new File(loadPath);
        int index = file.getName().lastIndexOf(".");
        String uncompressedFilename = file.getName().substring(0, index)+"-unboxed.txt";

        JFileChooser chooser = CreateSaveDirectoryChooser();
        if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
            Log("////////////////////////NEW TASK////////////////////////");
            Log("Target Directory:"+  chooser.getSelectedFile() +"\\"+uncompressedFilename);
            Log("Task Type: Uncompress");
            Log("////////////////////////////////////////////////////////");
            savePath = chooser.getSelectedFile() +"\\"+ uncompressedFilename;

            if (type == CompressionType.Huffman) {
                HuffmanCompressionHandler.type = HuffmanCompressionHandler.ProcessType.Decode;
                HuffmanCompressionHandler.savepath = savePath;
                HuffmanCompressionHandler.loadpath = loadPath;

                Thread t = new Thread(new HuffmanCompressionHandler());
                t.start();
            }else{
                LZ77CompressionHandler lz77CompressionHandler = new LZ77CompressionHandler();
                lz77CompressionHandler.RunDecode();
                Thread t = new Thread(new LZ77CompressionHandler());
                t.start();
            }
        }
    }

    private JFileChooser CreateSaveDirectoryChooser(){
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Pick save location");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        // disable the "All files" option.
        chooser.setAcceptAllFileFilterUsed(false);
        return  chooser;
    }

    public class LoadingBar{
        private final int MIN = 0;
        private final int MAX = 100;
        private JProgressBar pbar;

        public LoadingBar(JPanel panel){
            // initialize Progress Bar
            pbar = new JProgressBar();
            pbar.setMinimum(MIN);
            pbar.setMaximum(MAX);
            pbar.setStringPainted(true);
        }

        public void updateBar(float newValue) {
            pbar.setValue(Math.round(newValue));
        }

    }
}
