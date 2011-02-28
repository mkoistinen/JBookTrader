package com.jbooktrader.platform.util;

import com.jbooktrader.platform.startup.*;

import javax.swing.*;

/**
 * Utility class to display message and error dialogs.
 */
public class MessageDialog {

    public static void showMessage(String msg) {
        JOptionPane.showMessageDialog(null, msg, JBookTrader.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(String msg) {
        JOptionPane.showMessageDialog(null, msg, JBookTrader.APP_NAME, JOptionPane.ERROR_MESSAGE);
    }

    public static void showError(Throwable t) {
        showError(t.getMessage());
    }
}
