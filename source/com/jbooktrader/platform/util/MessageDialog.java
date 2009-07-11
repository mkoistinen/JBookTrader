package com.jbooktrader.platform.util;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.startup.*;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class to display message and error dialogs.
 */
public class MessageDialog {

    public static void showMessage(final Component parent, final String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(parent, msg, JBookTrader.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public static void showError(final Component parent, final String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(parent, msg, JBookTrader.APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public static void showError(final Component parent, final Throwable t) {
        Dispatcher.getReporter().report(t);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(parent, t.toString(), JBookTrader.APP_NAME, JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
