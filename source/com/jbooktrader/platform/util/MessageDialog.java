package com.jbooktrader.platform.util;

import com.jbooktrader.platform.startup.JBookTrader;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class to display message and error dialogs.
 */
public class MessageDialog {

    public static void showMessage(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, JBookTrader.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, JBookTrader.APP_NAME, JOptionPane.ERROR_MESSAGE);
    }

}
