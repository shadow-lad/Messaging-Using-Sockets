package org.shardav.client;

import javax.swing.*;
import java.awt.*;

public class Client extends JFrame implements Runnable {

    //TODO: Make a GUI client
    private Client(){
        initUI();
    }

    private void initUI(){
        //TODO: Build the UI for the client
        // - Ask for username
        // - Show possible users they can connect to
        // - Global chat
        // - (OPTIONAL) Chat Groups
    }

    public static void main(String[] args) {

        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            EventQueue.invokeLater(()->{
                Thread t = new Thread(new Client());
                t.start();
            });
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex){
            JOptionPane.showConfirmDialog(null, "Cannot imitate the UI of your system\n\n" +
                    "Rolling back to the default UI ","UI Message",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void run() {
        //TODO: Implement userName in a new JFrame here and run setVisible = true
        // when username is entered and the username is not already logged in.
        setVisible(true);
    }
}