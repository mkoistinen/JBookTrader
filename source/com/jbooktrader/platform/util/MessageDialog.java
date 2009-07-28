package com.jbooktrader.platform.util;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.startup.*;

import javax.swing.*;

/**
 * Utility class to display message and error dialogs.
 */
public class MessageDialog {

    public static void showMessage(final String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(null, msg, JBookTrader.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public static void showError(final String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(null, msg, JBookTrader.APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public static void showError(final Throwable t) {
        Dispatcher.getReporter().report(t);
        showError(t.getMessage());
    }
}
