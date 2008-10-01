package com.jbooktrader.platform.util;

import com.jbooktrader.platform.startup.*;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class to display message and error dialogs.
 */
public class MessageDialog {

    private static boolean consoleMode = false;
    
    public static void showMessage(Component parent, String msg) {
        if(consoleMode) {
            System.err.println("ERROR: "+msg);
        } else {
            JOptionPane.showMessageDialog(parent, msg, JBookTrader.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static void showError(Component parent, String msg) {
        if(consoleMode) {
            System.err.println("ERROR: "+msg);            
        } else {
            JOptionPane.showMessageDialog(parent, msg, JBookTrader.APP_NAME, JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void setConsoleMode(boolean activated) {
        consoleMode = activated;       
    }

}
